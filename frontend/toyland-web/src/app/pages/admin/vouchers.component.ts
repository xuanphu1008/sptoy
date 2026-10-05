import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { AdminApi } from '../../core/admin-api.service';
import { VOUCHER_TYPE } from '../../core/labels';
import { Voucher } from '../../core/models';
import { NotifyService } from '../../core/notify.service';
import { confirmDialog } from '../../shared/confirm-dialog.component';
import { openForm } from '../../shared/form-dialog.component';

function localNow(offsetDays = 0): string {
  const d = new Date(Date.now() + offsetDays * 86400000);
  d.setMinutes(d.getMinutes() - d.getTimezoneOffset());
  return d.toISOString().slice(0, 16);
}

@Component({
  selector: 'tl-vouchers',
  imports: [DatePipe, MatTableModule, MatButtonModule, MatIconModule],
  template: `
    <div class="page-title"><h1>Mã giảm giá (Voucher)</h1><span class="spacer"></span>
      <button mat-flat-button (click)="edit(null)"><mat-icon>add</mat-icon>Tạo voucher</button></div>
    <div class="table-wrap">
      <table mat-table [dataSource]="list()">
        <ng-container matColumnDef="code"><th mat-header-cell *matHeaderCellDef>Mã</th>
          <td mat-cell *matCellDef="let v"><span class="code">{{ v.code }}</span><div class="small">{{ v.name }}</div></td></ng-container>
        <ng-container matColumnDef="type"><th mat-header-cell *matHeaderCellDef>Loại / Ưu đãi</th>
          <td mat-cell *matCellDef="let v"><span class="chip blue">{{ vt[v.type] }}</span><div class="small">{{ v.summary }}</div></td></ng-container>
        <ng-container matColumnDef="time"><th mat-header-cell *matHeaderCellDef>Thời gian</th>
          <td mat-cell *matCellDef="let v" class="small">{{ v.startAt | date: 'dd/MM/yy HH:mm' }}<br />→ {{ v.endAt | date: 'dd/MM/yy HH:mm' }}</td></ng-container>
        <ng-container matColumnDef="usage"><th mat-header-cell *matHeaderCellDef>Đã dùng</th>
          <td mat-cell *matCellDef="let v">{{ v.usedCount }} / {{ v.usageLimit ?? '∞' }}<div class="small muted">{{ v.perUserLimit }} lần/khách</div></td></ng-container>
        <ng-container matColumnDef="status"><th mat-header-cell *matHeaderCellDef>Trạng thái</th>
          <td mat-cell *matCellDef="let v">
            @if (!v.active) { <span class="chip grey">Tắt</span> }
            @else if (isExpired(v)) { <span class="chip red">Hết hạn</span> }
            @else if (isUpcoming(v)) { <span class="chip yellow">Sắp diễn ra</span> }
            @else { <span class="chip green">Đang chạy</span> }
          </td></ng-container>
        <ng-container matColumnDef="actions"><th mat-header-cell *matHeaderCellDef></th>
          <td mat-cell *matCellDef="let v" class="nowrap"><div class="cell-actions">
            <button mat-icon-button (click)="edit(v)" aria-label="Sửa"><mat-icon>edit</mat-icon></button>
            <button mat-icon-button class="danger-icon" (click)="remove(v)" aria-label="Xoá"><mat-icon>delete</mat-icon></button></div></td></ng-container>
        <tr mat-header-row *matHeaderRowDef="cols"></tr>
        <tr mat-row *matRowDef="let row; columns: cols"></tr>
      </table>
    </div>
  `,
  styles: `.code { font-family: monospace; font-weight: 800; background: var(--tl-red-soft); color: var(--tl-red); padding: 2px 8px; border-radius: 6px; }`,
})
export class VouchersComponent implements OnInit {
  private api = inject(AdminApi);
  private dialog = inject(MatDialog);
  private notify = inject(NotifyService);
  vt: Record<string, string> = VOUCHER_TYPE;
  cols = ['code', 'type', 'time', 'usage', 'status', 'actions'];
  list = signal<Voucher[]>([]);

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.api.vouchers().subscribe((l) => this.list.set(l));
  }

  isExpired(v: Voucher): boolean {
    return new Date(v.endAt).getTime() < Date.now();
  }

  isUpcoming(v: Voucher): boolean {
    return new Date(v.startAt).getTime() > Date.now();
  }

  edit(v: Voucher | null): void {
    const value = v ? { ...v } : {
      code: '', name: '', description: '', type: 'PERCENT', value: 10, maxDiscount: 50000, minOrderValue: 200000,
      startAt: localNow(), endAt: localNow(30), usageLimit: 100, perUserLimit: 1, active: true,
    };
    openForm(this.dialog, {
      title: v ? `Sửa voucher ${v.code}` : 'Tạo voucher mới',
      value: value as Record<string, unknown>,
      fields: [
        { key: 'code', label: 'Mã (3-30 ký tự)', required: true },
        { key: 'name', label: 'Tên chương trình', required: true },
        { key: 'type', label: 'Loại', type: 'select', options: Object.entries(VOUCHER_TYPE).map(([value, label]) => ({ value, label })) },
        { key: 'value', label: 'Giá trị (% hoặc ₫)', type: 'number', required: true, hint: 'PERCENT: %, FIXED: số tiền; FREESHIP: nhập 0' },
        { key: 'maxDiscount', label: 'Giảm tối đa (₫)', type: 'number', showIf: (x) => x['type'] !== 'FIXED', hint: 'FREESHIP: trần phí ship được giảm; trống = miễn toàn bộ' },
        { key: 'minOrderValue', label: 'Đơn tối thiểu (₫)', type: 'number', required: true },
        { key: 'startAt', label: 'Bắt đầu', type: 'datetime', required: true },
        { key: 'endAt', label: 'Kết thúc', type: 'datetime', required: true },
        { key: 'usageLimit', label: 'Tổng lượt dùng', type: 'number', hint: 'Để trống = không giới hạn' },
        { key: 'perUserLimit', label: 'Lượt / khách', type: 'number', required: true },
        { key: 'active', label: 'Kích hoạt', type: 'toggle' },
        { key: 'description', label: 'Mô tả / điều kiện', type: 'textarea' },
      ],
      save: (x) => this.api.saveVoucher(v?.id ?? null, { ...x, code: String(x['code']).toUpperCase() }),
    }).subscribe((r) => r && this.load());
  }

  remove(v: Voucher): void {
    confirmDialog(this.dialog, { title: `Xoá voucher ${v.code}?`, message: 'Voucher đã được dùng sẽ chỉ bị tắt.', danger: true, confirmText: 'Xoá' })
      .subscribe((ok) => ok && this.api.deleteVoucher(v.id).subscribe({ next: (r) => { this.notify.success(r.message); this.load(); }, error: (e) => this.notify.error(e) }));
  }
}
