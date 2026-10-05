import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, input, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { RouterLink } from '@angular/router';
import { AdminApi } from '../../core/admin-api.service';
import { ORDER_STATUS, PAYMENT_METHOD, PAYMENT_PROVIDER, PAYMENT_STATUS, PLACEHOLDER_IMG, RETURN_STATUS } from '../../core/labels';
import { Order, OrderLedger, OrderStatus } from '../../core/models';
import { NotifyService } from '../../core/notify.service';
import { VndPipe } from '../../core/vnd.pipe';
import { confirmDialog } from '../../shared/confirm-dialog.component';
import { OrderTimelineComponent } from '../../shared/order-timeline.component';
import { PaymentActionDialogComponent } from '../../shared/payment-action-dialog.component';

@Component({
  selector: 'tl-admin-order-detail',
  imports: [RouterLink, FormsModule, DatePipe, MatButtonModule, MatIconModule, MatFormFieldModule, MatInputModule, VndPipe, OrderTimelineComponent],
  template: `
    <div class="row no-print"><a mat-button routerLink="/admin/orders"><mat-icon>arrow_back</mat-icon>Danh sách đơn</a><span class="spacer"></span>
      <button mat-stroked-button (click)="print()"><mat-icon>print</mat-icon>In phiếu giao hàng</button></div>
    @if (o(); as o) {
      <div class="page-title mt-8">
        <h1>Đơn {{ o.orderCode }}</h1>
        <span class="chip st-{{ o.status }}">{{ o.statusLabel }}</span>
        <span class="chip" [class.green]="o.paymentStatus === 'PAID'">{{ o.paymentMethod }} · {{ ps[o.paymentStatus] }}</span>
      </div>

      @if (o.actions.nextStatuses.length) {
        <div class="card actions no-print">
          <b>Xử lý đơn:</b>
          @for (s of o.actions.nextStatuses; track s) {
            <button mat-flat-button [class.danger]="s === 'CANCELLED' || s === 'DELIVERY_FAILED'" (click)="changeStatus(s)">
              <mat-icon>{{ label[s].icon }}</mat-icon>{{ label[s].label }}
            </button>
          }
        </div>
      }

      <div class="layout">
        <div>
          <div class="card">
            <h3>Tiến trình</h3>
            <tl-order-timeline [status]="o.status" [history]="o.history" [showActor]="true" />
          </div>

          @if (o.returnRequest; as r) {
            <div class="card ret">
              <h3>Yêu cầu trả hàng <span class="chip">{{ rs[r.status] }}</span></h3>
              <div><b>Lý do:</b> {{ r.reason }}</div>
              @if (r.description) { <p>{{ r.description }}</p> }
              @if (r.imageUrls.length) { <div class="imgs">@for (u of r.imageUrls; track u) { <a [href]="u" target="_blank"><img [src]="u" alt="" /></a> }</div> }
              @if (r.status === 'REQUESTED') {
                <div class="grid-2 mt-16">
                  <mat-form-field><mat-label>Số tiền hoàn</mat-label><input matInput type="number" [(ngModel)]="refund" /></mat-form-field>
                  <mat-form-field><mat-label>Ghi chú gửi khách</mat-label><input matInput [(ngModel)]="adminNote" /></mat-form-field>
                </div>
                <div class="row">
                  <button mat-flat-button (click)="approve(r.id)">Chấp nhận & nhập lại kho</button>
                  <button mat-stroked-button (click)="reject(r.id)">Từ chối</button>
                </div>
              } @else {
                @if (r.adminNote) { <div class="muted">Ghi chú: {{ r.adminNote }}</div> }
                @if (r.refundAmount) { <div>Đã hoàn: <b>{{ r.refundAmount | vnd }}</b></div> }
                <div class="small muted">Xử lý bởi {{ r.processedBy }} lúc {{ r.processedAt | date: 'HH:mm dd/MM/yyyy' }}</div>
              }
            </div>
          }

          <div class="card">
            <h3>Sản phẩm ({{ o.items.length }})</h3>
            <table class="items">
              <tr><th></th><th>Sản phẩm</th><th>SKU</th><th class="r">Đơn giá</th><th class="r">SL</th><th class="r">Thành tiền</th></tr>
              @for (i of o.items; track i.id) {
                <tr>
                  <td><img [src]="i.imageUrl || ph" class="thumb" alt="" /></td>
                  <td>@if (i.productId) { <a [routerLink]="['/admin/products', i.productId]">{{ i.productName }}</a> } @else { {{ i.productName }} }
                    @if (i.variantName) { <div class="small muted">{{ i.variantName }}</div> }</td>
                  <td class="small">{{ i.sku }}</td>
                  <td class="r">{{ i.unitPrice | vnd }}</td><td class="r">{{ i.quantity }}</td><td class="r bold">{{ i.lineTotal | vnd }}</td>
                </tr>
              }
            </table>
            <div class="sums">
              <div class="sum-row"><span>Tiền hàng</span><span>{{ o.subtotal | vnd }}</span></div>
              @if (o.discountAmount > 0) { <div class="sum-row"><span>Voucher {{ o.voucherCode }}</span><span class="discount">-{{ o.discountAmount | vnd }}</span></div> }
              @if (o.pointsDiscount > 0) { <div class="sum-row"><span>Dùng {{ o.pointsUsed }} điểm</span><span class="discount">-{{ o.pointsDiscount | vnd }}</span></div> }
              <div class="sum-row"><span>Phí ship</span><span>{{ o.shippingFee | vnd }}</span></div>
              @if (o.shippingDiscount > 0) { <div class="sum-row"><span>Giảm phí ship</span><span class="discount">-{{ o.shippingDiscount | vnd }}</span></div> }
              @if (o.giftWrapFee > 0) { <div class="sum-row"><span>Gói quà</span><span>{{ o.giftWrapFee | vnd }}</span></div> }
              <div class="sum-row total"><span>Tổng thu</span><span class="v">{{ o.totalAmount | vnd }}</span></div>
              @if (o.pointsEarned > 0) { <div class="sum-row small"><span>Khách được tích</span><span>+{{ o.pointsEarned }} điểm</span></div> }
            </div>
          </div>
        </div>

        <aside>
          <div class="card">
            <h3><mat-icon>person</mat-icon>Khách hàng</h3>
            <b>{{ o.customer.fullName }}</b> <span class="muted">(&#64;{{ o.customer.username }})</span>
            <div class="small">{{ o.customer.email }} · {{ o.customer.phone }}</div>
          </div>
          <div class="card">
            <h3><mat-icon>local_shipping</mat-icon>Giao hàng</h3>
            <b>{{ o.receiverName }}</b> · {{ o.phone }}
            <div>{{ o.fullAddress }}</div>
            @if (o.note) { <div class="note"><b>Khách ghi chú:</b> {{ o.note }}</div> }
            @if (o.giftWrap) { <div class="note gift"><mat-icon>card_giftcard</mat-icon> Gói quà @if (o.giftMessage) { — thiệp: "{{ o.giftMessage }}" }</div> }
          </div>
          <div class="card">
            <h3><mat-icon>payments</mat-icon>Thanh toán</h3>
            <div>{{ pm[o.paymentMethod] }}</div>
            <div>Trạng thái: <b>{{ ps[o.paymentStatus] }}</b></div>
            @if (o.paidAt) { <div class="small muted">Thanh toán lúc {{ o.paidAt | date: 'HH:mm dd/MM/yyyy' }}</div> }
            @if (o.paymentDeadline && o.status === 'PENDING_PAYMENT') { <div class="small muted">Hạn thanh toán {{ o.paymentDeadline | date: 'HH:mm dd/MM' }}</div> }
            @if (ledger(); as l) {
              <div class="ledger">
                <div class="lrow"><span>Đã thu</span><b>{{ l.paid | vnd }}</b></div>
                @if (l.refunded > 0) { <div class="lrow"><span>Đã hoàn</span><b class="neg">−{{ l.refunded | vnd }}</b></div> }
                @if (l.refundRemaining > 0) { <div class="lrow warn"><span>Còn phải hoàn</span><b>{{ l.refundRemaining | vnd }}</b></div> }
                @for (t of l.transactions; track t.id) {
                  <div class="txn" [class.failed]="t.status !== 'SUCCESS'">
                    <span class="dot" [class.out]="t.kind === 'REFUND'"></span>
                    <div class="grow"><b>{{ t.kind === 'REFUND' ? 'Hoàn' : 'Thu' }} · {{ prov[t.provider] || t.provider }}</b>
                      <div class="small muted">{{ t.createdAt | date: 'HH:mm dd/MM/yy' }}@if (t.transactionNo) { · {{ t.transactionNo }} }@if (t.status !== 'SUCCESS') { · {{ t.status === 'FAILED' ? 'thất bại' : 'đang chờ' }} }</div></div>
                    <span class="small bold">{{ t.kind === 'REFUND' ? '−' : '' }}{{ t.amount | vnd }}</span>
                  </div>
                }
                @if (l.canCollect) { <button mat-stroked-button class="full" (click)="payAction('collect')"><mat-icon>savings</mat-icon>Ghi nhận đã thu tiền</button> }
                @if (l.canRefund) { <button mat-flat-button class="full" (click)="payAction('refund')"><mat-icon>currency_exchange</mat-icon>Ghi nhận hoàn tiền</button> }
              </div>
            }
          </div>
          <div class="card no-print">
            <h3><mat-icon>sticky_note_2</mat-icon>Ghi chú nội bộ</h3>
            <mat-form-field class="full"><textarea matInput rows="3" [(ngModel)]="internalNote" placeholder="Chỉ nhân viên nhìn thấy"></textarea></mat-form-field>
            <button mat-stroked-button (click)="saveNote()">Lưu ghi chú</button>
          </div>
        </aside>
      </div>
    }
  `,
  styles: `
    .actions { display: flex; gap: 8px; align-items: center; flex-wrap: wrap; }
    .ledger { margin-top: 12px; padding-top: 12px; border-top: 1px dashed var(--tl-border); display: flex; flex-direction: column; gap: 6px; }
    .lrow { display: flex; justify-content: space-between; font-size: 14px; } .lrow.warn { color: var(--tl-red-dark); font-weight: 700; }
    .neg { color: var(--tl-red-dark); }
    .txn { display: flex; align-items: center; gap: 8px; padding: 6px 0; font-size: 13px; } .txn.failed { opacity: .55; }
    .txn .grow { flex: 1; min-width: 0; }
    .dot { width: 10px; height: 10px; border-radius: 50%; background: var(--tl-green); flex: none; } .dot.out { background: var(--tl-orange); }
    .ledger .full { width: 100%; margin-top: 6px; }
    .danger { --mdc-filled-button-container-color: #c62828; }
    .layout { display: grid; grid-template-columns: minmax(0, 1fr) 340px; gap: 16px; align-items: start; }
    .layout .card { margin-top: 16px; }
    h3 { display: flex; align-items: center; gap: 8px; } h3 mat-icon { color: var(--tl-red); }
    .ret { border: 1.5px solid #ffcc80; }
    .imgs { display: flex; gap: 6px; margin-top: 8px; } .imgs img { width: 80px; height: 80px; object-fit: cover; border-radius: 8px; }
    .items { width: 100%; border-collapse: collapse; font-size: 14px; }
    .items th, .items td { padding: 8px 6px; border-bottom: 1px solid var(--tl-border); text-align: left; }
    .items th { color: var(--tl-muted); font-weight: 600; }
    .r { text-align: right !important; }
    .sums { max-width: 360px; margin-left: auto; margin-top: 12px; }
    .note { background: var(--tl-hover); border-radius: 8px; padding: 8px 10px; margin-top: 8px; font-size: 14px; }
    .note.gift { background: var(--tl-yellow-soft); } .note mat-icon { font-size: 16px; width: 16px; height: 16px; vertical-align: -3px; }
    @media (max-width: 1000px) { .layout { grid-template-columns: minmax(0, 1fr); } }
    @media print { .no-print { display: none !important; } }
  `,
})
export class AdminOrderDetailComponent implements OnInit {
  id = input.required<string>();
  private api = inject(AdminApi);
  private dialog = inject(MatDialog);
  private notify = inject(NotifyService);
  o = signal<Order | null>(null);
  label = ORDER_STATUS;
  pm = PAYMENT_METHOD;
  ps = PAYMENT_STATUS;
  prov = PAYMENT_PROVIDER;
  ledger = signal<OrderLedger | null>(null);
  rs = RETURN_STATUS;
  ph = PLACEHOLDER_IMG;
  internalNote = '';
  refund: number | null = null;
  adminNote = '';

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.api.order(+this.id()).subscribe((o) => this.set(o));
  }

  private set(o: Order): void {
    this.o.set(o);
    this.api.orderLedger(o.id).subscribe({ next: (l) => this.ledger.set(l), error: () => this.ledger.set(null) });
    this.internalNote = o.internalNote ?? '';
    this.refund = o.returnRequest?.refundAmount ?? o.totalAmount;
  }

  changeStatus(s: OrderStatus): void {
    const danger = s === 'CANCELLED' || s === 'DELIVERY_FAILED';
    confirmDialog<string>(this.dialog, {
      title: `Chuyển đơn sang "${ORDER_STATUS[s].label}"?`, danger,
      message: s === 'CANCELLED' ? 'Tồn kho sẽ được hoàn lại và khách hàng nhận email thông báo.' : 'Khách hàng sẽ nhận email thông báo.',
      input: { label: danger ? 'Lý do (bắt buộc)' : 'Ghi chú (không bắt buộc)', required: danger },
    }).subscribe((note) => {
      if (note === undefined) return;
      this.api.orderStatus(this.o()!.id, s, note || null).subscribe({
        next: (o) => {
          this.set(o);
          this.notify.success(`Đã cập nhật: ${o.statusLabel}`);
        },
        error: (e) => this.notify.error(e),
      });
    });
  }

  saveNote(): void {
    this.api.orderNote(this.o()!.id, this.internalNote).subscribe({
      next: (o) => {
        this.set(o);
        this.notify.success('Đã lưu ghi chú');
      },
      error: (e) => this.notify.error(e),
    });
  }

  approve(returnId: number): void {
    this.api.approveReturn(returnId, { adminNote: this.adminNote || null, refundAmount: this.refund }).subscribe({
      next: (o) => {
        this.set(o);
        this.notify.success('Đã chấp nhận trả hàng');
      },
      error: (e) => this.notify.error(e),
    });
  }

  reject(returnId: number): void {
    if (!this.adminNote.trim()) {
      this.notify.error('Nhập ghi chú lý do từ chối gửi khách');
      return;
    }
    this.api.rejectReturn(returnId, { adminNote: this.adminNote }).subscribe({
      next: (o) => {
        this.set(o);
        this.notify.success('Đã từ chối yêu cầu trả hàng');
      },
      error: (e) => this.notify.error(e),
    });
  }

  /** Nhan vien ghi nhan da thu tien COD / da hoan tien cho khach */
  payAction(mode: 'collect' | 'refund'): void {
    const o = this.o()!;
    const l = this.ledger()!;
    this.dialog.open(PaymentActionDialogComponent, {
      autoFocus: false,
      data: { mode, orderId: o.id, orderCode: o.orderCode, customerName: o.receiverName,
        amount: mode === 'refund' ? l.refundRemaining : o.totalAmount, paymentMethod: o.paymentMethod },
    }).afterClosed().subscribe((res) => res && this.load());
  }

  print(): void {
    window.print();
  }
}
