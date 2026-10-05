import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { RouterLink } from '@angular/router';
import { AdminApi } from '../../core/admin-api.service';
import { Page, Receipt, Supplier } from '../../core/models';
import { NotifyService } from '../../core/notify.service';
import { VndPipe } from '../../core/vnd.pipe';
import { confirmDialog } from '../../shared/confirm-dialog.component';
import { openForm } from '../../shared/form-dialog.component';
import { EMAIL_PATTERN } from '../../core/validators';

/** Quan ly nha cung cap + lich su nhap hang theo tung nha cung cap. */
@Component({
  selector: 'tl-suppliers',
  imports: [FormsModule, DatePipe, RouterLink, MatTableModule, MatPaginatorModule, MatFormFieldModule, MatInputModule, MatButtonModule,
    MatButtonToggleModule, MatIconModule, MatTooltipModule, VndPipe],
  template: `
    <div class="page-title"><h1>Nhà cung cấp</h1><span class="spacer"></span>
      <a mat-stroked-button routerLink="/admin/inventory/receipts/new"><mat-icon>add_box</mat-icon>Tạo phiếu nhập</a>
      <button mat-flat-button (click)="edit(null)"><mat-icon>add_business</mat-icon>Thêm nhà cung cấp</button>
    </div>
    <div class="filters">
      <mat-form-field class="search-field" subscriptSizing="dynamic"><mat-icon matPrefix>search</mat-icon>
        <input matInput [(ngModel)]="keyword" (keyup.enter)="load(0)" placeholder="Tìm mã, tên, người liên hệ, SĐT…" aria-label="Tìm nhà cung cấp" />
        @if (keyword) { <button mat-icon-button matSuffix type="button" (click)="keyword = ''; load(0)" aria-label="Xoá từ khoá"><mat-icon>close</mat-icon></button> }
      </mat-form-field>
      <mat-button-toggle-group [(ngModel)]="active" (change)="load(0)" hideSingleSelectionIndicator aria-label="Lọc trạng thái">
        <mat-button-toggle [value]="null">Tất cả</mat-button-toggle>
        <mat-button-toggle [value]="true">Đang giao dịch</mat-button-toggle>
        <mat-button-toggle [value]="false">Ngừng giao dịch</mat-button-toggle>
      </mat-button-toggle-group>
    </div>

    <div class="layout" [class.with-detail]="selected()">
      <div class="table-wrap">
        <table mat-table [dataSource]="page()?.content ?? []">
          <ng-container matColumnDef="name"><th mat-header-cell *matHeaderCellDef>Nhà cung cấp</th>
            <td mat-cell *matCellDef="let s"><b>{{ s.name }}</b><div class="small muted">{{ s.code }}@if (s.taxCode) { · MST {{ s.taxCode }} }</div></td></ng-container>
          <ng-container matColumnDef="contact"><th mat-header-cell *matHeaderCellDef>Liên hệ</th>
            <td mat-cell *matCellDef="let s">{{ s.contactName || '—' }}<div class="small muted">{{ s.phone }}@if (s.email) { · {{ s.email }} }</div></td></ng-container>
          <ng-container matColumnDef="receipts"><th mat-header-cell *matHeaderCellDef class="r">Phiếu nhập</th>
            <td mat-cell *matCellDef="let s" class="r">{{ s.receiptCount }}</td></ng-container>
          <ng-container matColumnDef="total"><th mat-header-cell *matHeaderCellDef class="r">Tổng tiền nhập</th>
            <td mat-cell *matCellDef="let s" class="r bold">{{ s.totalImported | vnd }}
              @if (s.lastReceiptAt) { <div class="small muted">gần nhất {{ s.lastReceiptAt | date: 'dd/MM/yyyy' }}</div> }</td></ng-container>
          <ng-container matColumnDef="status"><th mat-header-cell *matHeaderCellDef>Trạng thái</th>
            <td mat-cell *matCellDef="let s"><span class="chip" [class.green]="s.active" [class.grey]="!s.active">{{ s.active ? 'Đang giao dịch' : 'Ngừng' }}</span></td></ng-container>
          <ng-container matColumnDef="actions"><th mat-header-cell *matHeaderCellDef></th>
            <td mat-cell *matCellDef="let s" class="nowrap" (click)="$event.stopPropagation()"><div class="cell-actions">
              <button mat-icon-button (click)="edit(s)" matTooltip="Sửa" aria-label="Sửa nhà cung cấp"><mat-icon>edit</mat-icon></button>
              <button mat-icon-button class="danger-icon" (click)="remove(s)" matTooltip="Xoá" aria-label="Xoá nhà cung cấp"><mat-icon>delete</mat-icon></button>
            </div></td></ng-container>
          <tr mat-header-row *matHeaderRowDef="cols"></tr>
          <tr mat-row *matRowDef="let row; columns: cols" class="clickable" [class.sel]="selected()?.id === row.id" (click)="select(row)"></tr>
        </table>
        @if (page()?.content?.length === 0) {
          <div class="empty-state"><mat-icon>local_shipping</mat-icon><b>Chưa có nhà cung cấp phù hợp</b><span>Thêm nhà cung cấp để chọn nhanh khi tạo phiếu nhập kho.</span></div>
        }
        <mat-paginator [length]="page()?.totalElements ?? 0" [pageIndex]="page()?.page ?? 0" [pageSize]="20" [hidePageSize]="true" (page)="go($event)" />
      </div>

      @if (selected(); as s) {
        <aside class="card detail">
          <div class="row between"><h3 class="mb-0">{{ s.name }}</h3>
            <button mat-icon-button (click)="selected.set(null)" aria-label="Đóng"><mat-icon>close</mat-icon></button></div>
          <dl>
            <dt>Mã</dt><dd>{{ s.code }}</dd>
            <dt>Người liên hệ</dt><dd>{{ s.contactName || '—' }}</dd>
            <dt>Điện thoại</dt><dd>{{ s.phone || '—' }}</dd>
            <dt>Email</dt><dd>{{ s.email || '—' }}</dd>
            <dt>Địa chỉ</dt><dd>{{ s.address || '—' }}</dd>
            @if (s.note) { <dt>Ghi chú</dt><dd>{{ s.note }}</dd> }
          </dl>
          <div class="kpis">
            <div><b>{{ s.receiptCount }}</b><span>phiếu nhập</span></div>
            <div><b>{{ s.totalImported | vnd }}</b><span>tổng tiền nhập</span></div>
          </div>
          <h4>Lịch sử nhập hàng</h4>
          @for (r of receipts()?.content ?? []; track r.id) {
            <div class="rc">
              <div><b>{{ r.code }}</b><div class="small muted">{{ r.createdAt | date: 'dd/MM/yyyy HH:mm' }} · {{ r.createdBy }}</div></div>
              <div class="r">{{ r.totalQuantity }} sp<div class="bold">{{ r.totalCost | vnd }}</div></div>
            </div>
          } @empty { <div class="muted small">Chưa có phiếu nhập từ nhà cung cấp này.</div> }
          @if ((receipts()?.totalPages ?? 0) > 1) {
            <mat-paginator [length]="receipts()?.totalElements ?? 0" [pageIndex]="receipts()?.page ?? 0" [pageSize]="8" [hidePageSize]="true"
                           (page)="loadReceipts(s.id, $event.pageIndex)" />
          }
        </aside>
      }
    </div>
  `,
  styles: `
    .layout { display: grid; grid-template-columns: minmax(0, 1fr); gap: 16px; align-items: start; }
    .layout.with-detail { grid-template-columns: minmax(0, 1fr) 360px; }
    .r { text-align: right !important; }
    tr.sel td { background: var(--tl-red-soft); }
    .detail { position: sticky; top: 84px; }
    dl { display: grid; grid-template-columns: 110px 1fr; gap: 6px 10px; margin: 8px 0 12px; font-size: 14px; }
    dt { color: var(--tl-muted); } dd { margin: 0; }
    .kpis { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; margin-bottom: 8px; }
    .kpis div { background: var(--tl-hover); border-radius: 14px; padding: 10px 12px; display: flex; flex-direction: column; }
    .kpis b { font-family: var(--font-display); font-size: 18px; } .kpis span { font-size: 12px; color: var(--tl-muted); }
    h4 { margin: 14px 0 6px; }
    .rc { display: flex; justify-content: space-between; gap: 8px; padding: 8px 0; border-bottom: 1px dashed var(--tl-border); font-size: 14px; }
    @media (max-width: 1100px) { .layout.with-detail { grid-template-columns: minmax(0, 1fr); } .detail { position: static; } }
  `,
})
export class SuppliersComponent implements OnInit {
  private api = inject(AdminApi);
  private dialog = inject(MatDialog);
  private notify = inject(NotifyService);
  cols = ['name', 'contact', 'receipts', 'total', 'status', 'actions'];
  page = signal<Page<Supplier> | null>(null);
  selected = signal<Supplier | null>(null);
  receipts = signal<Page<Receipt> | null>(null);
  keyword = '';
  active: boolean | null = null;
  private current = 0;

  ngOnInit(): void {
    this.load(0);
  }

  load(p: number): void {
    this.current = p;
    this.api.suppliers({ keyword: this.keyword.trim(), active: this.active, page: p, size: 20 }).subscribe((r) => this.page.set(r));
  }

  go(e: PageEvent): void {
    this.load(e.pageIndex);
  }

  select(s: Supplier): void {
    this.selected.set(s);
    this.loadReceipts(s.id, 0);
  }

  loadReceipts(id: number, p: number): void {
    this.api.receipts({ supplierId: id, page: p, size: 8 }).subscribe((r) => this.receipts.set(r));
  }

  edit(s: Supplier | null): void {
    openForm(this.dialog, {
      title: s ? `Sửa nhà cung cấp: ${s.name}` : 'Thêm nhà cung cấp',
      value: (s ? { ...s } : { code: '', name: '', contactName: '', phone: '', email: '', address: '', taxCode: '', note: '', active: true }) as Record<string, unknown>,
      fields: [
        { key: 'name', label: 'Tên nhà cung cấp', required: true, wide: true },
        { key: 'code', label: 'Mã (để trống để tự tạo)', hint: 'vd: NCC-LEGO' },
        { key: 'taxCode', label: 'Mã số thuế' },
        { key: 'contactName', label: 'Người liên hệ' },
        { key: 'phone', label: 'Số điện thoại' },
        { key: 'email', label: 'Email', wide: true, pattern: EMAIL_PATTERN, patternError: 'Email không hợp lệ, ví dụ đúng: ten.ho2004@gmail.com' },
        { key: 'address', label: 'Địa chỉ', wide: true },
        { key: 'note', label: 'Ghi chú (điều khoản, chiết khấu…)', type: 'textarea' },
        { key: 'active', label: 'Đang giao dịch', type: 'toggle' },
      ],
      save: (v) => this.api.saveSupplier(s?.id ?? null, v),
    }).subscribe((r) => {
      if (!r) return;
      this.notify.success(s ? 'Đã cập nhật nhà cung cấp' : 'Đã thêm nhà cung cấp');
      this.load(this.current);
      if (s && this.selected()?.id === s.id) this.selected.set(r as Supplier);
    });
  }

  remove(s: Supplier): void {
    confirmDialog(this.dialog, {
      title: `Xoá nhà cung cấp "${s.name}"?`, danger: true, confirmText: 'Xoá',
      message: s.receiptCount > 0 ? 'Nhà cung cấp đã có phiếu nhập nên không thể xoá. Hãy sửa và tắt "Đang giao dịch".' : 'Thao tác không thể hoàn tác.',
    }).subscribe((ok) => ok && this.api.deleteSupplier(s.id).subscribe({
      next: (r) => { this.notify.success(r.message); this.selected.set(null); this.load(this.current); },
      error: (e) => this.notify.error(e),
    }));
  }
}
