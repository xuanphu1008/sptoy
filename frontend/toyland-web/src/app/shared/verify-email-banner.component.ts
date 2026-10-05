import { Component, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { MatIconModule } from '@angular/material/icon';
import { NavigationEnd, Router, RouterLink } from '@angular/router';
import { filter, map } from 'rxjs';
import { AuthService } from '../core/auth.service';
import { NotifyService } from '../core/notify.service';

/** Dai nhac duoi header khi khach hang chua xac nhan email (giong "verified" middleware cua Laravel). */
@Component({
  selector: 'tl-verify-email-banner',
  imports: [RouterLink, MatIconModule],
  template: `
    @if (auth.needsEmailVerification() && !onVerifyPage()) {
      <div class="vb" role="status">
        <div class="container row">
          <mat-icon>mark_email_unread</mat-icon>
          <span class="grow">Email <b>{{ auth.user()?.email }}</b> chưa được xác nhận — bạn chưa thể đặt hàng và đánh giá.
            Mở liên kết trong hộp thư để hoàn tất.</span>
          <button type="button" class="vb-btn" (click)="resend()" [disabled]="sending()">{{ sending() ? 'Đang gửi…' : 'Gửi lại email' }}</button>
          <a routerLink="/verify-email" class="vb-link">Chi tiết</a>
        </div>
      </div>
    }
  `,
  styles: `
    .vb { background: var(--tl-yellow-soft); color: var(--tl-text); font-size: 14px; border-bottom: 1px solid var(--tl-border); }
    .vb .container { padding-top: 8px; padding-bottom: 8px; flex-wrap: wrap; }
    .vb mat-icon { color: var(--tl-orange); flex: none; }
    .vb-btn { border: 0; border-radius: 999px; padding: 6px 14px; font: inherit; font-weight: 700; cursor: pointer;
      background: var(--tl-card); color: var(--tl-red-dark); box-shadow: var(--clay-sm); }
    .vb-btn:disabled { opacity: .6; cursor: default; }
    .vb-link { color: var(--tl-red-dark); font-weight: 700; }
  `,
})
export class VerifyEmailBannerComponent {
  auth = inject(AuthService);
  private notify = inject(NotifyService);
  private router = inject(Router);
  sending = signal(false);
  onVerifyPage = toSignal(
    this.router.events.pipe(filter((e) => e instanceof NavigationEnd), map(() => this.router.url.startsWith('/verify-email'))),
    { initialValue: this.router.url.startsWith('/verify-email') },
  );

  resend(): void {
    this.sending.set(true);
    this.auth.resendVerification().subscribe({
      next: (r) => {
        this.sending.set(false);
        this.notify.success(r.message);
      },
      error: (e) => {
        this.sending.set(false);
        this.notify.error(e);
      },
    });
  }
}
