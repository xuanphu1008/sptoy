import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatRadioModule } from '@angular/material/radio';
import { AdminApi } from '../core/admin-api.service';
import { PaymentMethod } from '../core/models';
import { NotifyService, errorMessage } from '../core/notify.service';
import { VndPipe } from '../core/vnd.pipe';

export interface PaymentActionData {
  mode: 'collect' | 'refund';
  orderId: number;
  orderCode: string;
  customerName: string;
  /** collect: tong tien don · refund: so con phai hoan */
  amount: number;
  paymentMethod: PaymentMethod;
}

/** Hop thoai nhan vien ghi nhan "Da thu tien" (don COD) hoac "Da hoan tien" (don cho hoan tien). */
@Component({
  selector: 'tl-payment-action-dialog',
  imports: [FormsModule, MatDialogModule, MatButtonModule, MatFormFieldModule, MatInputModule, MatRadioModule, MatIconModule, VndPipe],
  template: `
    <h2 mat-dialog-title>{{ d.mode === 'refund' ? 'Ghi nhận đã hoàn tiền' : 'Ghi nhận đã thu tiền' }}</h2>
    <mat-dialog-content>
      <div class="head">
        <span class="ic" [class.out]="d.mode === 'refund'"><mat-icon>{{ d.mode === 'refund' ? 'currency_exchange' : 'savings' }}</mat-icon></span>
        <div><b>Đơn {{ d.orderCode }}</b><div class="small muted">{{ d.customerName }}</div></div>
        <div class="amt"><div class="small muted">{{ d.mode === 'refund' ? 'Còn phải hoàn' : 'Số tiền thu' }}</div><b>{{ d.amount | vnd }}</b></div>
      </div>

      <div class="lbl">Hình thức</div>
      <mat-radio-group [(ngModel)]="method" class="methods" aria-label="Hình thức">
        @if (d.mode === 'refund') {
          <mat-radio-button value="BANK_TRANSFER">Chuyển khoản</mat-radio-button>
          <mat-radio-button value="CASH">Tiền mặt</mat-radio-button>
          @if (d.paymentMethod === 'MOMO') { <mat-radio-button value="MOMO">Hoàn về ví MoMo (tự động)</mat-radio-button> }
        } @else {
          <mat-radio-button value="CASH">Tiền mặt tại cửa hàng</mat-radio-button>
          <mat-radio-button value="BANK_TRANSFER">Chuyển khoản</mat-radio-button>
        }
      </mat-radio-group>

      @if (d.mode === 'refund') {
        <mat-form-field class="full amt-field" subscriptSizing="dynamic"><mat-label>Số tiền hoàn</mat-label>
          <input matInput type="number" [(ngModel)]="amount" min="1000" [max]="d.amount" />
          <span matTextSuffix>đ</span>
          <mat-hint>Có thể hoàn nhiều lần; hoàn đủ thì đơn chuyển "Đã hoàn tiền"</mat-hint>
        </mat-form-field>
      }
      @if (method === 'MOMO') {
        <div class="info small">ToyLand gọi API hoàn tiền của MoMo, tiền về ví/thẻ khách đã dùng; mã giao dịch hoàn do MoMo cấp được ghi vào sổ thu – chi.</div>
      } @else if (method !== 'CASH') {
        <mat-form-field class="full"><mat-label>Mã giao dịch ngân hàng</mat-label>
          <input matInput [(ngModel)]="reference" maxlength="50" placeholder="vd: FT26271012345" /></mat-form-field>
      }
      <mat-form-field class="full"><mat-label>Ghi chú</mat-label>
        <textarea matInput rows="2" [(ngModel)]="note" maxlength="300"></textarea></mat-form-field>
      @if (error()) { <div class="err">{{ error() }}</div> }
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close>Huỷ</button>
      <button mat-flat-button (click)="save()" [disabled]="saving() || (d.mode === 'refund' && !(amount > 0))">
        {{ saving() ? 'Đang lưu…' : 'Xác nhận' }}</button>
    </mat-dialog-actions>
  `,
  styles: `
    mat-dialog-content { width: 460px; max-width: 100%; }
    .head { display: flex; align-items: center; gap: 12px; padding: 12px; border-radius: 16px; background: var(--tl-hover); margin-bottom: 14px; }
    .head > div:nth-child(2) { flex: 1; min-width: 0; }
    .ic { width: 42px; height: 42px; border-radius: 14px; display: grid; place-items: center; background: var(--tl-green-soft); color: var(--tl-green); flex: none; }
    .ic.out { background: var(--tl-orange-soft); color: var(--tl-orange); }
    .amt { text-align: right; } .amt b { font-size: 18px; font-variant-numeric: tabular-nums; }
    .lbl { font-weight: 700; font-size: 13px; margin-bottom: 4px; }
    .methods { display: flex; flex-wrap: wrap; gap: 4px 12px; margin-bottom: 12px; }
    .full { width: 100%; }
    .amt-field { margin-bottom: 14px; }
    .err { color: var(--tl-red-dark); font-weight: 600; font-size: 14px; }
    .info { background: var(--tl-hover); border-radius: 12px; padding: 10px 12px; margin-bottom: 14px; }
  `,
})
export class PaymentActionDialogComponent {
  d = inject<PaymentActionData>(MAT_DIALOG_DATA);
  private ref = inject(MatDialogRef<PaymentActionDialogComponent>);
  private api = inject(AdminApi);
  private notify = inject(NotifyService);
  method = this.d.mode === 'refund' ? (this.d.paymentMethod === 'MOMO' ? 'MOMO' : 'BANK_TRANSFER') : 'CASH';
  amount = this.d.amount;
  reference = '';
  note = '';
  saving = signal(false);
  error = signal<string | null>(null);

  save(): void {
    this.saving.set(true);
    this.error.set(null);
    const ref = this.method === 'CASH' || this.method === 'MOMO' ? null : this.reference.trim() || null;
    const note = this.note.trim() || null;
    const req = this.d.mode === 'refund'
      ? this.api.recordRefund(this.d.orderId, { amount: +this.amount, method: this.method, reference: ref, note })
      : this.api.collectPayment(this.d.orderId, { method: this.method, reference: ref, note });
    req.subscribe({
      next: (ledger) => {
        this.notify.success(this.d.mode === 'refund' ? `Đã ghi nhận hoàn tiền đơn ${this.d.orderCode}` : `Đã ghi nhận thu tiền đơn ${this.d.orderCode}`);
        this.ref.close(ledger);
      },
      error: (e) => {
        this.saving.set(false);
        this.error.set(errorMessage(e));
      },
    });
  }
}
