import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, input, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { RouterLink } from '@angular/router';
import { ShopApi } from '../../core/api.service';
import { PAYMENT_METHOD, PAYMENT_STATUS } from '../../core/labels';
import { Order } from '../../core/models';
import { NotifyService } from '../../core/notify.service';
import { VndPipe } from '../../core/vnd.pipe';

@Component({
  selector: 'tl-order-success',
  imports: [RouterLink, MatButtonModule, MatIconModule, VndPipe, DatePipe],
  template: `
    <div class="container page">
      <div class="card box">
        @if (order(); as o) {
          @if (o.paymentMethod === 'MOMO' && o.paymentStatus !== 'PAID') {
            <mat-icon class="big warn">hourglass_top</mat-icon>
            <h1>Đơn hàng đang chờ thanh toán</h1>
            <p>Vui lòng hoàn tất thanh toán MoMo trước <b>{{ o.paymentDeadline | date: 'HH:mm dd/MM/yyyy' }}</b>, quá hạn đơn sẽ tự huỷ.</p>
            <button mat-flat-button (click)="pay(o.id)"><mat-icon>payment</mat-icon>Thanh toán ngay</button>
          } @else {
            <mat-icon class="big ok">check_circle</mat-icon>
            <h1>Đặt hàng thành công!</h1>
            <p>Cảm ơn bạn đã mua sắm tại ToyLand. Chúng tôi đã gửi email xác nhận đơn hàng tới <b>{{ o.customer.email }}</b>.</p>
          }
          <div class="info">
            <div class="sum-row"><span>Mã đơn hàng</span><b>{{ o.orderCode }}</b></div>
            <div class="sum-row"><span>Tổng thanh toán</span><b class="price">{{ o.totalAmount | vnd }}</b></div>
            <div class="sum-row"><span>Phương thức</span><span>{{ pm[o.paymentMethod] }}</span></div>
            <div class="sum-row"><span>Trạng thái thanh toán</span><span>{{ ps[o.paymentStatus] }}</span></div>
            <div class="sum-row"><span>Giao tới</span><span class="right">{{ o.receiverName }} · {{ o.phone }}<br />{{ o.fullAddress }}</span></div>
          </div>
          <div class="row center-row">
            <a mat-stroked-button [routerLink]="['/account/orders', o.id]">Xem chi tiết đơn</a>
            <a mat-flat-button routerLink="/products">Tiếp tục mua sắm</a>
          </div>
        }
      </div>
    </div>
  `,
  styles: `
    .box { max-width: 640px; margin: 0 auto; text-align: center; padding: 32px; }
    .big { font-size: 80px; width: 80px; height: 80px; }
    .ok { color: var(--tl-green); } .warn { color: var(--tl-orange); }
    .info { text-align: left; background: #fafafa; border-radius: 12px; padding: 12px 16px; margin: 20px 0; }
    .center-row { justify-content: center; flex-wrap: wrap; }
  `,
})
export class OrderSuccessComponent implements OnInit {
  id = input.required<string>();
  private api = inject(ShopApi);
  private notify = inject(NotifyService);
  order = signal<Order | null>(null);
  pm = PAYMENT_METHOD;
  ps = PAYMENT_STATUS;

  ngOnInit(): void {
    this.api.order(+this.id()).subscribe((o) => this.order.set(o));
  }

  pay(id: number): void {
    this.api.payOrder(id).subscribe({ next: (r) => (window.location.href = r.paymentUrl), error: (e) => this.notify.error(e) });
  }
}
