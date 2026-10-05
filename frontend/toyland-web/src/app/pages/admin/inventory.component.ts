import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatTableModule } from '@angular/material/table';
import { MatTabsModule } from '@angular/material/tabs';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { AdminApi } from '../../core/admin-api.service';
import { MOVEMENT_TYPE, PLACEHOLDER_IMG } from '../../core/labels';
import { Movement, MovementType, Page, Receipt, StockRow } from '../../core/models';
import { NotifyService } from '../../core/notify.service';
import { VndPipe } from '../../core/vnd.pipe';
import { confirmDialog } from '../../shared/confirm-dialog.component';

@Component({
  selector: 'tl-inventory',
  imports: [RouterLink, FormsModule, DatePipe, MatTabsModule, MatTableModule, MatPaginatorModule, MatFormFieldModule, MatInputModule, MatSelectModule,
    MatButtonModule, MatIconModule, MatSlideToggleModule, MatTooltipModule, VndPipe],
  template: `
    <div class="page-title"><h1>Quản lý kho</h1><span class="spacer"></span>
      <a mat-flat-button routerLink="/admin/inventory/receipts/new"><mat-icon>add_box</mat-icon>Tạo phiếu nhập kho</a></div>
    <div class="card">
      <mat-tab-group animationDuration="0ms" mat-stretch-tabs="false" (selectedIndexChange)="onTab($event)">
        <!-- ===== TON KHO ===== -->
        <mat-tab label="Tồn kho theo biến thể">
          <div class="filters mt-16">
            <mat-form-field class="search-field" subscriptSizing="dynamic"><mat-icon matPrefix>search</mat-icon><input matInput [(ngModel)]="keyword" (keyup.enter)="loadStock(0)" placeholder="Tìm tên sản phẩm hoặc SKU…" aria-label="Tìm tồn kho" />@if (keyword) { <button mat-icon-button matSuffix type="button" (click)="keyword = ''; loadStock(0)" aria-label="Xoá từ khoá"><mat-icon>close</mat-icon></button> }</mat-form-field>
            <mat-slide-toggle [(ngModel)]="lowStock" (change)="loadStock(0)">Chỉ hiện sắp hết (≤ 5)</mat-slide-toggle>
          </div>
          <table mat-table [dataSource]="stock()?.content ?? []" class="full">
            <ng-container matColumnDef="product"><th mat-header-cell *matHeaderCellDef>Sản phẩm / Biến thể</th>
              <td mat-cell *matCellDef="let s"><div class="row"><img [src]="s.imageUrl || ph" class="thumb" alt="" />
                <div><a [routerLink]="['/admin/products', s.productId]" class="bold">{{ s.productName }}</a><div class="small muted">{{ s.variantName || 'Mặc định' }}</div></div></div></td></ng-container>
            <ng-container matColumnDef="sku"><th mat-header-cell *matHeaderCellDef>SKU</th><td mat-cell *matCellDef="let s" class="small">{{ s.sku }}</td></ng-container>
            <ng-container matColumnDef="price"><th mat-header-cell *matHeaderCellDef>Giá bán / vốn</th>
              <td mat-cell *matCellDef="let s">{{ s.price | vnd }}<div class="small muted">{{ s.costPrice ? (s.costPrice | vnd) : '—' }}</div></td></ng-container>
            <ng-container matColumnDef="stock"><th mat-header-cell *matHeaderCellDef>Tồn</th>
              <td mat-cell *matCellDef="let s"><span class="chip" [class.red]="s.stock === 0" [class.orange]="s.lowStock && s.stock > 0" [class.green]="!s.lowStock">{{ s.stock }}</span></td></ng-container>
            <ng-container matColumnDef="actions"><th mat-header-cell *matHeaderCellDef></th>
              <td mat-cell *matCellDef="let s" class="nowrap"><div class="cell-actions">
                <button mat-stroked-button (click)="adjust(s)"><mat-icon>tune</mat-icon>Điều chỉnh</button>
                <button mat-icon-button (click)="history(s)" matTooltip="Lịch sử xuất / nhập" aria-label="Lịch sử xuất nhập"><mat-icon>history</mat-icon></button>
              </div></td></ng-container>
            <tr mat-header-row *matHeaderRowDef="stockCols"></tr>
            <tr mat-row *matRowDef="let row; columns: stockCols"></tr>
          </table>
          <mat-paginator [length]="stock()?.totalElements ?? 0" [pageIndex]="stock()?.page ?? 0" [pageSize]="20" [hidePageSize]="true" (page)="loadStock($event.pageIndex)" />
        </mat-tab>

        <!-- ===== PHIEU NHAP ===== -->
        <mat-tab label="Phiếu nhập kho">
          @for (r of receipts()?.content ?? []; track r.id) {
            <div class="receipt">
              <div class="row between wrap clickable" (click)="openReceipt(r)">
                <div><b>{{ r.code }}</b> · {{ r.supplier || 'Không rõ NCC' }}<div class="small muted">{{ r.createdAt | date: 'dd/MM/yyyy HH:mm' }} · bởi {{ r.createdBy }}</div></div>
                <div class="right">{{ r.totalQuantity }} sản phẩm<div class="bold">{{ r.totalCost | vnd }}</div></div>
              </div>
              @if (expanded() === r.id && detail(); as d) {
                <table class="simple">
                  <tr><th>SKU</th><th>Sản phẩm</th><th class="r">SL</th><th class="r">Đơn giá nhập</th><th class="r">Thành tiền</th></tr>
                  @for (i of d.items; track $index) {
                    <tr><td>{{ i.sku }}</td><td>{{ i.productName }}</td><td class="r">{{ i.quantity }}</td><td class="r">{{ i.unitCost | vnd }}</td><td class="r">{{ i.quantity * i.unitCost | vnd }}</td></tr>
                  }
                </table>
                @if (d.note) { <div class="small muted">Ghi chú: {{ d.note }}</div> }
              }
            </div>
          } @empty { <div class="empty">Chưa có phiếu nhập</div> }
          <mat-paginator [length]="receipts()?.totalElements ?? 0" [pageIndex]="receipts()?.page ?? 0" [pageSize]="20" [hidePageSize]="true" (page)="loadReceipts($event.pageIndex)" />
        </mat-tab>

        <!-- ===== LICH SU ===== -->
        <mat-tab label="Lịch sử xuất / nhập">
          <div class="filters mt-16">
            <mat-form-field class="search-field" subscriptSizing="dynamic"><mat-icon matPrefix>search</mat-icon><input matInput [(ngModel)]="mvKeyword" (keyup.enter)="loadMoves(0)" placeholder="Tìm SKU hoặc mã tham chiếu…" aria-label="Tìm lịch sử kho" />@if (mvKeyword) { <button mat-icon-button matSuffix type="button" (click)="mvKeyword = ''; loadMoves(0)" aria-label="Xoá từ khoá"><mat-icon>close</mat-icon></button> }</mat-form-field>
            <mat-form-field><mat-label>Loại</mat-label>
              <mat-select [(ngModel)]="mvType" (selectionChange)="loadMoves(0)"><mat-option [value]="null">Tất cả</mat-option>
                @for (t of types; track t[0]) { <mat-option [value]="t[0]">{{ t[1] }}</mat-option> }</mat-select></mat-form-field>
            @if (mvVariant) { <button mat-stroked-button (click)="mvVariant = null; loadMoves(0)">SKU {{ mvVariantSku }} ✕</button> }
          </div>
          <table mat-table [dataSource]="moves()?.content ?? []" class="full">
            <ng-container matColumnDef="time"><th mat-header-cell *matHeaderCellDef>Thời gian</th><td mat-cell *matCellDef="let m" class="small">{{ m.createdAt | date: 'dd/MM/yy HH:mm' }}</td></ng-container>
            <ng-container matColumnDef="sku"><th mat-header-cell *matHeaderCellDef>SKU</th><td mat-cell *matCellDef="let m">{{ m.sku }}</td></ng-container>
            <ng-container matColumnDef="type"><th mat-header-cell *matHeaderCellDef>Loại</th><td mat-cell *matCellDef="let m"><span class="chip">{{ mt[m.type] }}</span></td></ng-container>
            <ng-container matColumnDef="change"><th mat-header-cell *matHeaderCellDef>Thay đổi</th>
              <td mat-cell *matCellDef="let m" class="bold" [class.plus]="m.quantityChange > 0" [class.minus]="m.quantityChange < 0">{{ m.quantityChange > 0 ? '+' : '' }}{{ m.quantityChange }}</td></ng-container>
            <ng-container matColumnDef="balance"><th mat-header-cell *matHeaderCellDef>Tồn sau</th><td mat-cell *matCellDef="let m">{{ m.balanceAfter }}</td></ng-container>
            <ng-container matColumnDef="ref"><th mat-header-cell *matHeaderCellDef>Tham chiếu</th><td mat-cell *matCellDef="let m">{{ m.refCode }}<div class="small muted">{{ m.note }}</div></td></ng-container>
            <ng-container matColumnDef="by"><th mat-header-cell *matHeaderCellDef>Người thực hiện</th><td mat-cell *matCellDef="let m" class="small">{{ m.createdBy }}</td></ng-container>
            <tr mat-header-row *matHeaderRowDef="moveCols"></tr>
            <tr mat-row *matRowDef="let row; columns: moveCols"></tr>
          </table>
          <mat-paginator [length]="moves()?.totalElements ?? 0" [pageIndex]="moves()?.page ?? 0" [pageSize]="30" [hidePageSize]="true" (page)="loadMoves($event.pageIndex)" />
        </mat-tab>
      </mat-tab-group>
    </div>
  `,
  styles: `
    .receipt { border-bottom: 1px solid var(--tl-border); padding: 12px 4px; }
    .clickable { cursor: pointer; }
    .right { text-align: right; }
    table.simple { width: 100%; border-collapse: collapse; font-size: 13px; margin: 8px 0; background: var(--tl-hover); border-radius: 12px; overflow: hidden; }
    table.simple td, table.simple th { padding: 6px 8px; text-align: left; }
    .r { text-align: right !important; }
    .plus { color: var(--tl-green); } .minus { color: var(--tl-red); }
  `,
})
export class InventoryComponent implements OnInit {
  private api = inject(AdminApi);
  private dialog = inject(MatDialog);
  private notify = inject(NotifyService);
  private route = inject(ActivatedRoute);
  ph = PLACEHOLDER_IMG;
  mt: Record<string, string> = MOVEMENT_TYPE;
  types = Object.entries(MOVEMENT_TYPE) as [MovementType, string][];
  stockCols = ['product', 'sku', 'price', 'stock', 'actions'];
  moveCols = ['time', 'sku', 'type', 'change', 'balance', 'ref', 'by'];

  stock = signal<Page<StockRow> | null>(null);
  receipts = signal<Page<Receipt> | null>(null);
  moves = signal<Page<Movement> | null>(null);
  expanded = signal<number | null>(null);
  detail = signal<Receipt | null>(null);
  keyword = '';
  lowStock = false;
  mvKeyword = '';
  mvType: MovementType | null = null;
  mvVariant: number | null = null;
  mvVariantSku = '';
  private stockPage = 0;

  ngOnInit(): void {
    this.lowStock = this.route.snapshot.queryParams['lowStock'] === 'true';
    this.loadStock(0);
  }

  onTab(i: number): void {
    if (i === 1 && !this.receipts()) this.loadReceipts(0);
    if (i === 2) this.loadMoves(0);
  }

  loadStock(page: number): void {
    this.stockPage = page;
    this.api.stock({ keyword: this.keyword.trim(), lowStock: this.lowStock, page, size: 20 }).subscribe((p) => this.stock.set(p));
  }

  loadReceipts(page: number): void {
    this.api.receipts({ page, size: 20 }).subscribe((p) => this.receipts.set(p));
  }

  loadMoves(page: number): void {
    this.api.movements({ variantId: this.mvVariant, type: this.mvType, keyword: this.mvKeyword.trim(), page, size: 30 }).subscribe((p) => this.moves.set(p));
  }

  openReceipt(r: Receipt): void {
    if (this.expanded() === r.id) return this.expanded.set(null);
    this.expanded.set(r.id);
    this.detail.set(null);
    this.api.receipt(r.id).subscribe((d) => this.detail.set(d));
  }

  adjust(s: StockRow): void {
    confirmDialog<string>(this.dialog, {
      title: `Điều chỉnh tồn: ${s.sku}`, confirmText: 'Cập nhật',
      message: `Tồn hiện tại: ${s.stock}. Nhập số lượng thực tế sau kiểm kê.`, input: { label: 'Số lượng mới', required: true, value: String(s.stock) },
    }).subscribe((v) => {
      if (v === undefined) return;
      const q = Number(v);
      if (!Number.isInteger(q) || q < 0) return this.notify.error('Số lượng phải là số nguyên ≥ 0');
      this.api.adjust({ variantId: s.variantId, newQuantity: q, note: 'Kiểm kê điều chỉnh' }).subscribe({
        next: (r) => {
          this.notify.success(r.message);
          this.loadStock(this.stockPage);
        },
        error: (e) => this.notify.error(e),
      });
    });
  }

  history(s: StockRow): void {
    this.mvVariant = s.variantId;
    this.mvVariantSku = s.sku;
    this.api.movements({ variantId: s.variantId, page: 0, size: 30 }).subscribe((p) => this.moves.set(p));
    this.notify.info(`Mở tab "Lịch sử xuất / nhập" để xem lịch sử của ${s.sku}`);
  }
}
