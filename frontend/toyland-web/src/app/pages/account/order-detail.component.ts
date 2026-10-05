import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, input, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { Router, RouterLink } from '@angular/router';
import { ShopApi } from '../../core/api.service';
import { CartStore } from '../../core/cart.store';
import { CANCEL_REASONS, PAYMENT_METHOD, PAYMENT_STATUS, PLACEHOLDER_IMG, RETURN_STATUS } from '../../core/labels';
import { Order, OrderItem } from '../../core/models';
import { NotifyService } from '../../core/notify.service';
import { VndPipe } from '../../core/vnd.pipe';
import { confirmDialog } from '../../shared/confirm-dialog.component';
import { CountdownComponent } from '../../shared/countdown.component';
import { OrderTimelineComponent } from '../../shared/order-timeline.component';
import { ReturnDialogComponent, ReviewDialogComponent } from './order-dialogs';

@Component({
  selector: 'tl-order-detail',
  imports: [RouterLink, DatePipe, MatButtonModule, MatIconModule, VndPipe, OrderTimelineComponent, CountdownComponent],
  template: `
    <a mat-button routerLink="/account/orders"><mat-icon>arrow_back</mat-icon>Đơn mua</a>
    @if (o(); as o) {
      <div class="card">
        <div class="row between wrap">
          <div><h2 class="mb-0">Đơn hàng {{ o.orderCode }}</h2><span class="muted small">Đặt lúc {{ o.createdAt | date: 'HH:mm dd/MM/yyyy' }}</span></div>
          <span class="chip st-{{ o.status }} big">{{ o.statusLabel }}</span>
        </div>
        @if (o.actions.canPay && o.paymentDeadline) {
          <div class="pay-box">
            <mat-icon>timer</mat-icon>
            <div class="grow">Vui lòng thanh toán trong <tl-countdown [end]="o.paymentDeadline" /> — quá hạn đơn sẽ tự động huỷ.</div>
            <button mat-flat-button (click)="pay()">Thanh toán MoMo</button>
          </div>
        }
        <div class="mt-16"><tl-order-timeline [status]="o.status" [history]="o.history" /></div>
        @if (o.cancelReason) { <div class="note red"><b>Lý do huỷ:</b> {{ o.cancelReason }}</div> }
      </div>

      @if (o.returnRequest; as r) {
        <div class="card">
          <h3>Yêu cầu trả hàng <span class="chip" [class.green]="r.status === 'APPROVED'" [class.red]="r.status === 'REJECTED'" [class.yellow]="r.status === 'REQUESTED'">{{ rs[r.status] }}</span></h3>
          <div><b>Lý do:</b> {{ r.reason }}</div>
          @if (r.description) { <div class="muted">{{ r.description }}</div> }
          @if (r.imageUrls.length) { <div class="imgs">@for (u of r.imageUrls; track u) { <img [src]="u" alt="" /> }</div> }
          @if (r.adminNote) { <div class="note"><b>Phản hồi từ ToyLand:</b> {{ r.adminNote }}</div> }
          @if (r.refundAmount) { <div class="mt-8">Số tiền hoàn: <b class="price">{{ r.refundAmount | vnd }}</b></div> }
        </div>
      }

      <div class="grid-2 mt-16">
        <div class="card mt-0">
          <h3><mat-icon>location_on</mat-icon> Địa chỉ nhận hàng</h3>
          <b>{{ o.receiverName }}</b> · {{ o.phone }}
          <div class="muted">{{ o.fullAddress }}</div>
          @if (o.note) { <div class="mt-8 small"><b>Ghi chú:</b> {{ o.note }}</div> }
          @if (o.giftWrap) { <div class="mt-8 small"><mat-icon class="i">card_giftcard</mat-icon> Gói quà @if (o.giftMessage) { — "{{ o.giftMessage }}" }</div> }
        </div>
        <div class="card mt-0">
          <h3><mat-icon>payments</mat-icon> Thanh toán</h3>
          <div>{{ pm[o.paymentMethod] }}</div>
          <div class="muted">Trạng thái: <b>{{ ps[o.paymentStatus] }}</b> @if (o.paidAt) { lúc {{ o.paidAt | date: 'HH:mm dd/MM/yyyy' }} }</div>
        </div>
      </div>

      <div class="card">
        <h3>Sản phẩm</h3>
        @for (i of o.items; track i.id) {
          <div class="line-item">
            <img [src]="i.imageUrl || ph" alt="" />
            <div class="info">
              @if (i.productSlug) { <a class="name" [routerLink]="['/products', i.productSlug]">{{ i.productName }}</a> } @else { <div class="name">{{ i.productName }}</div> }
              @if (i.variantName) { <div class="variant">Phân loại: {{ i.variantName }}</div> }
              <div class="small muted">x{{ i.quantity }} · SKU {{ i.sku }}</div>
            </div>
            <div class="right">
              @if (i.originalPrice && i.originalPrice > i.unitPrice) { <div class="price-old">{{ i.originalPrice | vnd }}</div> }
              <div>{{ i.unitPrice | vnd }}</div>
              @if (i.canReview) { <button mat-stroked-button class="mt-8" (click)="review(i)"><mat-icon>star</mat-icon>Đánh giá</button> }
              @else if (i.reviewed) { <span class="chip green mt-8">Đã đánh giá</span> }
            </div>
          </div>
        }
        <div class="sums">
          <div class="sum-row"><span>Tổng tiền hàng</span><span>{{ o.subtotal | vnd }}</span></div>
          @if (o.discountAmount > 0) { <div class="sum-row"><span>Voucher @if (o.voucherCode) { ({{ o.voucherCode }}) }</span><span class="discount">-{{ o.discountAmount | vnd }}</span></div> }
          @if (o.pointsDiscount > 0) { <div class="sum-row"><span>Dùng {{ o.pointsUsed }} điểm thưởng</span><span class="discount">-{{ o.pointsDiscount | vnd }}</span></div> }
          <div class="sum-row"><span>Phí vận chuyển</span><span>{{ o.shippingFee | vnd }}</span></div>
          @if (o.shippingDiscount > 0) { <div class="sum-row"><span>Giảm phí vận chuyển</span><span class="discount">-{{ o.shippingDiscount | vnd }}</span></div> }
          @if (o.giftWrapFee > 0) { <div class="sum-row"><span>Gói quà</span><span>{{ o.giftWrapFee | vnd }}</span></div> }
          <div class="sum-row total"><span>Thành tiền</span><span class="v">{{ o.totalAmount | vnd }}</span></div>
          @if (o.pointsEarned > 0) { <div class="sum-row"><span class="small">Điểm thưởng đã nhận</span><a class="small bold" routerLink="/account/points">+{{ o.pointsEarned }} điểm</a></div> }
        </div>
      </div>

      <div class="card row end wrap">
        @if (o.actions.canCancel) { <button mat-stroked-button (click)="cancel()">Huỷ đơn hàng</button> }
        @if (o.actions.canReturn) { <button mat-stroked-button (click)="requestReturn()">Trả hàng / Hoàn tiền</button> }
        @if (o.actions.canReorder) { <button mat-flat-button (click)="reorder()"><mat-icon>replay</mat-icon>Mua lại</button> }
        @if (o.actions.canPay) { <button mat-flat-button (click)="pay()">Thanh toán ngay</button> }
      </div>
    }
  `,
  styles: `
    .chip.big { font-size: 14px; padding: 6px 14px; }
    .pay-box { display: flex; gap: 12px; align-items: center; background: var(--tl-yellow-soft); border-radius: 12px; padding: 12px; margin-top: 12px; flex-wrap: wrap; }
    .pay-box > mat-icon { color: var(--tl-orange); }
    .note { background: #f5f5f5; border-radius: 10px; padding: 10px 12px; margin-top: 10px; }
    .note.red { background: var(--tl-red-soft); }
    h3 { display: flex; align-items: center; gap: 8px; }
    h3 mat-icon { color: var(--tl-red); }
    .i { font-size: 16px; width: 16px; height: 16px; vertical-align: -3px; }
    .imgs { display: flex; gap: 6px; margin-top: 8px; } .imgs img { width: 72px; height: 72px; object-fit: cover; border-radius: 8px; }
    .right { text-align: right; }
    .sums { max-width: 380px; margin-left: auto; margin-top: 12px; }
  `,
})
export class OrderDetailComponent implements OnInit {
  id = input.required<string>();
  private api = inject(ShopApi);
  private dialog = inject(MatDialog);
  private notify = inject(NotifyService);
  private cart = inject(CartStore);
  private router = inject(Router);
  o = signal<Order | null>(null);
  pm = PAYMENT_METHOD;
  ps = PAYMENT_STATUS;
  rs = RETURN_STATUS;
  ph = PLACEHOLDER_IMG;

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.api.order(+this.id()).subscribe((o) => this.o.set(o));
  }

  cancel(): void {
    confirmDialog<string>(this.dialog, {
      title: 'Huỷ đơn hàng', message: 'Vui lòng chọn lý do huỷ đơn:', confirmText: 'Huỷ đơn', danger: true,
      input: { label: 'Lý do huỷ', required: true, options: CANCEL_REASONS },
    }).subscribe((reason) => {
      if (!reason) return;
      this.api.cancelOrder(this.o()!.id, reason).subscribe({
        next: (o) => {
          this.o.set(o);
          this.notify.success('Đã huỷ đơn hàng');
        },
        error: (e) => this.notify.error(e),
      });
    });
  }

  pay(): void {
    this.api.payOrder(this.o()!.id).subscribe({ next: (r) => (window.location.href = r.paymentUrl), error: (e) => this.notify.error(e) });
  }

  review(i: OrderItem): void {
    this.dialog.open(ReviewDialogComponent, { data: i, width: '560px', maxWidth: '96vw' }).afterClosed().subscribe((ok) => ok && this.load());
  }

  requestReturn(): void {
    this.dialog.open(ReturnDialogComponent, { data: this.o(), width: '560px', maxWidth: '96vw' }).afterClosed().subscribe((o?: Order) => {
      if (o) this.o.set(o);
    });
  }

  reorder(): void {
    this.api.reorder(this.o()!.id).subscribe({
      next: (c) => {
        this.cart.set(c);
        this.notify.success('Đã thêm lại các sản phẩm vào giỏ hàng');
        this.router.navigate(['/cart']);
      },
      error: (e) => this.notify.error(e),
    });
  }
}
