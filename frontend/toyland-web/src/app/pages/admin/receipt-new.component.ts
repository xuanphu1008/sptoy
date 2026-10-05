import { Component, computed, inject, signal } from '@angular/core';
import { toObservable, toSignal } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { Router, RouterLink } from '@angular/router';
import { catchError, debounceTime, map, of, switchMap } from 'rxjs';
import { AdminApi } from '../../core/admin-api.service';
import { StockRow, SupplierOption } from '../../core/models';
import { NotifyService } from '../../core/notify.service';
import { VndPipe } from '../../core/vnd.pipe';

interface Line { row: StockRow; quantity: number; unitCost: number; }

@Component({
  selector: 'tl-receipt-new',
  imports: [RouterLink, FormsModule, MatAutocompleteModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatButtonModule, MatIconModule, VndPipe],
  template: `
    <div class="page-title"><a mat-icon-button routerLink="/admin/inventory"><mat-icon>arrow_back</mat-icon></a><h1>Tạo phiếu nhập kho</h1></div>
    <div class="card">
      <div class="grid-2">
        <mat-form-field><mat-label>Nhà cung cấp</mat-label>
          <mat-select [(ngModel)]="supplierId">
            <mat-option [value]="null">— Nhập lẻ / NCC khác —</mat-option>
            @for (s of suppliers(); track s.id) { <mat-option [value]="s.id">{{ s.name }} <span class="muted">({{ s.code }})</span></mat-option> }
          </mat-select>
          <mat-hint><a routerLink="/admin/suppliers">Quản lý nhà cung cấp</a></mat-hint>
        </mat-form-field>
        @if (supplierId === null) {
          <mat-form-field><mat-label>Tên nguồn hàng (tuỳ chọn)</mat-label><input matInput [(ngModel)]="supplier" /></mat-form-field>
        }
        <mat-form-field><mat-label>Ghi chú</mat-label><input matInput [(ngModel)]="note" /></mat-form-field>
      </div>
      <mat-form-field class="full search-field add-line" subscriptSizing="dynamic">
        <mat-icon matPrefix>search</mat-icon>
        <input matInput [ngModel]="q()" (ngModelChange)="q.set($event)" [matAutocomplete]="ac"
               placeholder="Tìm sản phẩm hoặc SKU để thêm vào phiếu…" aria-label="Tìm sản phẩm để thêm vào phiếu" />
      </mat-form-field>
      <mat-autocomplete #ac="matAutocomplete" (optionSelected)="add($event.option.value)">
        @for (r of results(); track r.variantId) {
          <mat-option [value]="r">{{ r.productName }} — {{ r.variantName || 'Mặc định' }} <span class="muted">({{ r.sku }}, tồn {{ r.stock }})</span></mat-option>
        }
      </mat-autocomplete>

      <table class="lines">
        <tr><th>SKU</th><th>Sản phẩm</th><th>Tồn hiện tại</th><th>SL nhập</th><th>Đơn giá nhập</th><th class="r">Thành tiền</th><th></th></tr>
        @for (l of lines(); track l.row.variantId; let i = $index) {
          <tr>
            <td>{{ l.row.sku }}</td><td>{{ l.row.productName }}<div class="small muted">{{ l.row.variantName }}</div></td>
            <td>{{ l.row.stock }}</td>
            <td><input type="number" min="1" [(ngModel)]="l.quantity" (ngModelChange)="touch()" /></td>
            <td><input type="number" min="0" [(ngModel)]="l.unitCost" (ngModelChange)="touch()" /></td>
            <td class="r">{{ l.quantity * l.unitCost | vnd }}</td>
            <td><button mat-icon-button (click)="remove(i)"><mat-icon>close</mat-icon></button></td>
          </tr>
        } @empty { <tr><td colspan="7" class="empty">Chưa có dòng nào — tìm sản phẩm ở ô phía trên</td></tr> }
      </table>
      <div class="row end mt-16">
        <span>Tổng: <b>{{ totalQty() }}</b> sản phẩm · <b class="price">{{ totalCost() | vnd }}</b></span>
        <button mat-flat-button (click)="save()" [disabled]="!lines().length || saving()">Lưu phiếu nhập</button>
      </div>
    </div>
  `,
  styles: `
    .lines { width: 100%; border-collapse: collapse; font-size: 14px; }
    .lines th, .lines td { padding: 8px 6px; border-bottom: 1px solid var(--tl-border); text-align: left; }
    .lines th { background: var(--tl-table-head); color: var(--tl-subtle); font-size: 12px; text-transform: uppercase; letter-spacing: .05em; }
    .lines input { background: var(--tl-card); color: var(--tl-text); }
    .lines input { width: 110px; border: 1px solid var(--tl-border); border-radius: 6px; padding: 6px; font: inherit; }
    .r { text-align: right !important; }
    .add-line { max-width: none; margin-bottom: 16px; }
  `,
})
export class ReceiptNewComponent {
  private api = inject(AdminApi);
  private notify = inject(NotifyService);
  private router = inject(Router);
  supplier = '';
  supplierId: number | null = null;
  suppliers = toSignal(this.api.supplierOptions().pipe(catchError(() => of([] as SupplierOption[]))), { initialValue: [] as SupplierOption[] });
  note = '';
  q = signal('');
  lines = signal<Line[]>([]);
  saving = signal(false);
  private version = signal(0);

  results = toSignal(
    toObservable(this.q).pipe(
      debounceTime(250),
      switchMap((q) => (typeof q !== 'string' || q.trim().length < 2 ? of([]) :
        this.api.stock({ keyword: q.trim(), page: 0, size: 10 }).pipe(map((p) => p.content), catchError(() => of([]))))),
    ),
    { initialValue: [] as StockRow[] },
  );
  totalQty = computed(() => (this.version(), this.lines().reduce((s, l) => s + (+l.quantity || 0), 0)));
  totalCost = computed(() => (this.version(), this.lines().reduce((s, l) => s + (+l.quantity || 0) * (+l.unitCost || 0), 0)));

  touch(): void {
    this.version.update((v) => v + 1);
  }

  add(r: StockRow): void {
    this.q.set('');
    if (this.lines().some((l) => l.row.variantId === r.variantId)) return this.notify.info('SKU đã có trong phiếu');
    this.lines.update((ls) => [...ls, { row: r, quantity: 10, unitCost: r.costPrice ?? Math.round(r.price * 0.6) }]);
  }

  remove(i: number): void {
    this.lines.update((ls) => ls.filter((_, idx) => idx !== i));
  }

  save(): void {
    const items = this.lines().map((l) => ({ variantId: l.row.variantId, quantity: +l.quantity, unitCost: +l.unitCost }));
    if (items.some((i) => !(i.quantity >= 1) || !(i.unitCost >= 0))) return this.notify.error('Số lượng ≥ 1 và đơn giá ≥ 0');
    this.saving.set(true);
    this.api.createReceipt({ supplierId: this.supplierId, supplier: this.supplierId ? null : this.supplier || null, note: this.note || null, items }).subscribe({
      next: (r) => {
        this.notify.success(`Đã tạo phiếu ${r.code}, tồn kho đã được cộng`);
        this.router.navigate(['/admin/inventory']);
      },
      error: (e) => {
        this.saving.set(false);
        this.notify.error(e);
      },
    });
  }
}
