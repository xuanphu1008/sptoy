import { Component, OnDestroy, OnInit, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth.service';
import { NotifyService, errorMessage } from '../../core/notify.service';

type State = 'loading' | 'success' | 'error' | 'notice';

/**
 * Trang xac nhan email (tuong tu trang "Verify Your Email Address" cua Laravel Breeze):
 *  - /verify-email?token=...  : khach bam link trong email -> goi API xac nhan.
 *  - /verify-email            : thong bao "hay kiem tra hop thu" + nut gui lai (sau khi dang ky).
 */
@Component({
  selector: 'tl-verify-email',
  imports: [RouterLink, MatButtonModule, MatIconModule, MatProgressSpinnerModule],
  template: `
    <div class="container page">
      <div class="auth-card card center-box">
        @switch (state()) {
          @case ('loading') {
            <mat-spinner diameter="44" /><p class="muted">Đang xác nhận email…</p>
          }
          @case ('success') {
            <div class="head"><mat-icon class="ok">mark_email_read</mat-icon><h1>Xác nhận thành công!</h1></div>
            <p>{{ message() }}</p>
            <p class="muted small">Email: <b>{{ email() }}</b></p>
            @if (auth.isLoggedIn()) {
              <a mat-flat-button class="full submit" [routerLink]="returnUrl">Tiếp tục mua sắm</a>
            } @else {
              <a mat-flat-button class="full submit" routerLink="/login">Đăng nhập</a>
            }
          }
          @case ('error') {
            <div class="head"><mat-icon class="bad">link_off</mat-icon><h1>Không thể xác nhận</h1></div>
            <div class="alert err">{{ message() }}</div>
            @if (auth.needsEmailVerification()) {
              <button mat-flat-button class="full submit" (click)="resend()" [disabled]="sending() || cooldown() > 0">
                {{ cooldown() > 0 ? 'Gửi lại sau ' + cooldown() + ' giây' : sending() ? 'Đang gửi…' : 'Gửi lại email xác nhận' }}</button>
            } @else if (!auth.isLoggedIn()) {
              <p class="muted small">Đăng nhập để gửi lại liên kết xác nhận mới.</p>
              <a mat-flat-button class="full submit" routerLink="/login" [queryParams]="{ returnUrl: '/verify-email' }">Đăng nhập</a>
            } @else {
              <a mat-stroked-button class="full" routerLink="/">Về trang chủ</a>
            }
          }
          @case ('notice') {
            @if (!auth.isLoggedIn()) {
              <div class="head"><mat-icon>mail</mat-icon><h1>Xác nhận email</h1></div>
              <p>Vui lòng mở liên kết xác nhận trong email chúng tôi đã gửi, hoặc đăng nhập để gửi lại.</p>
              <a mat-flat-button class="full submit" routerLink="/login" [queryParams]="{ returnUrl: '/verify-email' }">Đăng nhập</a>
            } @else if (!auth.needsEmailVerification()) {
              <div class="head"><mat-icon class="ok">verified</mat-icon><h1>Email đã được xác nhận</h1></div>
              <p>Tài khoản của bạn đã sẵn sàng để mua sắm.</p>
              <a mat-flat-button class="full submit" [routerLink]="returnUrl">Tiếp tục mua sắm</a>
            } @else {
              <div class="head"><mat-icon>forward_to_inbox</mat-icon><h1>Kiểm tra hộp thư của bạn</h1></div>
              <p>Cảm ơn bạn đã đăng ký! Chúng tôi vừa gửi liên kết xác nhận tới <b>{{ auth.user()?.email }}</b>.
                Bấm vào liên kết trong email để hoàn tất (hiệu lực 24 giờ).</p>
              <ul class="tips">
                <li>Không thấy email? Hãy kiểm tra thư mục <b>Spam / Quảng cáo</b>.</li>
                <li>Trong lúc chờ, bạn vẫn xem sản phẩm và thêm vào giỏ hàng được. <b>Đặt hàng và đánh giá</b> cần xác nhận email trước.</li>
              </ul>
              <button mat-flat-button class="full submit" (click)="resend()" [disabled]="sending() || cooldown() > 0">
                {{ cooldown() > 0 ? 'Gửi lại sau ' + cooldown() + ' giây' : sending() ? 'Đang gửi…' : 'Gửi lại email xác nhận' }}</button>
              <div class="row between links">
                <a routerLink="/account/profile" class="link">Nhập sai email? Đổi email</a>
                <a [routerLink]="returnUrl" class="link">Tiếp tục mua sắm →</a>
              </div>
            }
          }
        }
      </div>
    </div>
  `,
  styleUrl: './auth.scss',
  styles: `
    .center-box { text-align: center; }
    .center-box mat-spinner { margin: 24px auto 8px; }
    .head mat-icon.ok { background: linear-gradient(135deg, var(--tl-green), #7bd389); }
    .head mat-icon.bad { background: linear-gradient(135deg, var(--tl-subtle), var(--tl-muted)); }
    .tips { text-align: left; padding-left: 18px; color: var(--tl-muted); font-size: 14px; line-height: 1.6; }
    .links { margin-top: 4px; font-size: 14px; gap: 8px; flex-wrap: wrap; }
  `,
})
export class VerifyEmailComponent implements OnInit, OnDestroy {
  auth = inject(AuthService);
  private route = inject(ActivatedRoute);
  private notify = inject(NotifyService);
  state = signal<State>('loading');
  message = signal('');
  email = signal('');
  sending = signal(false);
  cooldown = signal(0);
  returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/';
  private timer?: ReturnType<typeof setInterval>;

  ngOnInit(): void {
    const token = this.route.snapshot.queryParams['token'];
    if (!token) {
      this.state.set('notice');
      this.auth.refreshMe(); // co the vua xac nhan o tab khac
      return;
    }
    this.auth.verifyEmail(token).subscribe({
      next: (r) => {
        this.email.set(r.email);
        this.message.set(r.message);
        this.state.set('success');
      },
      error: (e) => {
        this.message.set(errorMessage(e));
        this.state.set('error');
      },
    });
  }

  resend(): void {
    this.sending.set(true);
    this.auth.resendVerification().subscribe({
      next: (r) => {
        this.sending.set(false);
        this.notify.success(r.message);
        this.startCooldown(60);
      },
      error: (e) => {
        this.sending.set(false);
        const m = /(\d+) giây/.exec(errorMessage(e));
        if (m) this.startCooldown(+m[1]);
        this.notify.error(e);
      },
    });
  }

  private startCooldown(sec: number): void {
    clearInterval(this.timer);
    this.cooldown.set(sec);
    this.timer = setInterval(() => {
      this.cooldown.update((v) => v - 1);
      if (this.cooldown() <= 0) clearInterval(this.timer);
    }, 1000);
  }

  ngOnDestroy(): void {
    clearInterval(this.timer);
  }
}
