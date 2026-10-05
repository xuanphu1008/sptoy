import { Component, OnInit, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { ShopApi } from '../../core/api.service';
import { AuthService } from '../../core/auth.service';
import { MomoResult } from '../../core/models';
import { errorMessage } from '../../core/notify.service';

/**
 * MoMo chuyen trinh duyet ve: http://localhost:4200/payment/momo-return?partnerCode=...&orderId=...&resultCode=...&signature=...
 * Trang nay gui toan bo tham so cho backend kiem tra chu ky (HMAC-SHA256) va cap nhat don hang
 * (chay localhost MoMo khong goi duoc IPN ve may, nen buoc nay la noi xac nhan thanh toan).
 */
@Component({
  selector: 'tl-momo-return',
  imports: [RouterLink, MatButtonModule, MatIconModule, MatProgressSpinnerModule],
  template: `
    <div class="container page">
      <div class="card box">
        @if (result(); as r) {
          <mat-icon class="big" [class.ok]="r.success" [class.fail]="!r.success">{{ r.success ? 'verified' : 'error' }}</mat-icon>
          <h1>{{ r.success ? 'Thanh toán thành công' : 'Thanh toán không thành công' }}</h1>
          <p>{{ r.message }}</p>
          @if (r.orderCode) { <p>Mã đơn hàng: <b>{{ r.orderCode }}</b></p> }
          @if (!r.validSignature) { <p class="small muted">Chữ ký không hợp lệ — dữ liệu trả về có thể đã bị thay đổi.</p> }
          <div class="row actions">
            @if (r.orderId && auth.isLoggedIn()) { <a mat-flat-button [routerLink]="['/account/orders', r.orderId]">Xem đơn hàng</a> }
            <a mat-stroked-button routerLink="/">Về trang chủ</a>
          </div>
        } @else if (error()) {
          <mat-icon class="big fail">error</mat-icon>
          <h1>Không xác minh được giao dịch</h1>
          <p>{{ error() }}</p>
          <a mat-stroked-button routerLink="/account/orders">Đơn hàng của tôi</a>
        } @else {
          <mat-spinner diameter="48" class="spin" />
          <p>Đang xác minh kết quả thanh toán MoMo…</p>
        }
      </div>
    </div>
  `,
  styles: `
    .box { max-width: 560px; margin: 0 auto; text-align: center; padding: 32px; }
    .big { font-size: 80px; width: 80px; height: 80px; }
    .ok { color: var(--tl-green); } .fail { color: var(--tl-red); }
    .actions { justify-content: center; }
    .spin { margin: 0 auto 16px; }
  `,
})
export class MomoReturnComponent implements OnInit {
  private api = inject(ShopApi);
  private route = inject(ActivatedRoute);
  auth = inject(AuthService);
  result = signal<MomoResult | null>(null);
  error = signal<string | null>(null);

  ngOnInit(): void {
    const params = this.route.snapshot.queryParams as Record<string, string>;
    if (!params['orderId'] || !params['signature']) {
      this.error.set('Thiếu thông tin giao dịch từ MoMo');
      return;
    }
    this.api.momoReturn(params).subscribe({
      next: (r) => this.result.set(r),
      error: (e) => this.error.set(errorMessage(e)),
    });
  }
}
