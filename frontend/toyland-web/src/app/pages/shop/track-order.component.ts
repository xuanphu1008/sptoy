import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { ActivatedRoute } from '@angular/router';
import { ShopApi } from '../../core/api.service';
import { PAYMENT_METHOD, PAYMENT_STATUS } from '../../core/labels';
import { Tracking } from '../../core/models';
import { NotifyService } from '../../core/notify.service';
import { VndPipe } from '../../core/vnd.pipe';
import { OrderTimelineComponent } from '../../shared/order-timeline.component';

/** Tra cuu don hang khong can dang nhap (ma don + so dien thoai) */
@Component({
  selector: 'tl-track-order',
  imports: [FormsModule, MatFormFieldModule, MatInputModule, MatButtonModule, MatIconModule, DatePipe, VndPipe, OrderTimelineComponent],
  template: `
    <div class="container page">
      <div class="card form">
        <h1><mat-icon>local_shipping</mat-icon> Tra cứu đơn hàng</h1>
        <p class="muted">Nhập mã đơn hàng (in trong email xác nhận) và số điện thoại người nhận.</p>
        <form class="row wrap" (ngSubmit)="search()">
          <mat-form-field class="grow"><mat-label>Mã đơn hàng</mat-label><input matInput name="code" [(ngModel)]="code" placeholder="TL2509..." required /></mat-form-field>
          <mat-form-field class="grow"><mat-label>Số điện thoại</mat-label><input matInput name="phone" [(ngModel)]="phone" required /></mat-form-field>
          <button mat-flat-button type="submit" class="btn">Tra cứu</button>
        </form>
      </div>
      @if (t(); as t) {
        <div class="card mt-16">
          <div class="row between wrap">
            <h2 class="mb-0">Đơn {{ t.orderCode }}</h2>
            <span class="chip st-{{ t.status }}">{{ t.statusLabel }}</span>
          </div>
          <p class="muted">Đặt lúc {{ t.createdAt | date: 'HH:mm dd/MM/yyyy' }} · Người nhận: {{ t.receiverName }} ·
            {{ pm[t.paymentMethod] }} ({{ ps[t.paymentStatus] }}) · Tổng: <b class="price">{{ t.totalAmount | vnd }}</b></p>
          <tl-order-timeline [status]="t.status" [history]="t.history" />
          <h4 class="mt-16">Sản phẩm</h4>
          <ul>@for (i of t.items; track $index) { <li>{{ i }}</li> }</ul>
        </div>
      }
    </div>
  `,
  styles: `
    .form { max-width: 760px; margin: 0 auto; }
    h1 { display: flex; align-items: center; gap: 8px; }
    h1 mat-icon { color: var(--tl-red); }
    .btn { height: 52px; margin-bottom: 20px; }
    .card.mt-16 { max-width: 760px; margin-left: auto; margin-right: auto; }
  `,
})
export class TrackOrderComponent implements OnInit {
  private api = inject(ShopApi);
  private notify = inject(NotifyService);
  private route = inject(ActivatedRoute);
  code = '';
  phone = '';
  t = signal<Tracking | null>(null);
  pm = PAYMENT_METHOD;
  ps = PAYMENT_STATUS;

  ngOnInit(): void {
    const q = this.route.snapshot.queryParams;
    if (q['code'] && q['phone']) {
      this.code = q['code'];
      this.phone = q['phone'];
      this.search();
    }
  }

  search(): void {
    if (!this.code.trim() || !this.phone.trim()) {
      this.notify.error('Vui lòng nhập mã đơn và số điện thoại');
      return;
    }
    this.t.set(null);
    this.api.track(this.code.trim(), this.phone.trim()).subscribe({ next: (r) => this.t.set(r), error: (e) => this.notify.error(e) });
  }
}
