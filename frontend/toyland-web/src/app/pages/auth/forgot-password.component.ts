import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth.service';
import { errorMessage } from '../../core/notify.service';
import { EMAIL_PATTERN } from '../../core/validators';

@Component({
  selector: 'tl-forgot-password',
  imports: [FormsModule, RouterLink, MatFormFieldModule, MatInputModule, MatButtonModule, MatIconModule],
  template: `
    <div class="container page">
      <div class="auth-card card">
        <div class="head"><mat-icon>lock_reset</mat-icon><h1>Quên mật khẩu</h1>
          <p class="muted">Nhập email đã đăng ký, chúng tôi sẽ gửi liên kết đặt lại mật khẩu (hiệu lực 15 phút).</p></div>
        @if (done()) {
          <div class="alert ok">{{ done() }}</div>
          <p class="small muted">Môi trường phát triển dùng Mailtrap: mở hộp thư Mailtrap (hoặc log backend nếu app.mail.enabled=false) để lấy liên kết.</p>
        } @else {
          @if (error()) { <div class="alert err">{{ error() }}</div> }
          <form (ngSubmit)="submit()">
            <mat-form-field class="full"><mat-label>Email</mat-label><input matInput type="email" name="email" [(ngModel)]="email" required autocomplete="email" #em="ngModel" [pattern]="emailPattern" />
              @if (em.invalid && em.touched) { <mat-error>Email không hợp lệ, ví dụ đúng: ten.ho2004&#64;gmail.com</mat-error> }</mat-form-field>
            <button mat-flat-button class="full submit" [disabled]="loading() || !email || em.invalid">Gửi liên kết</button>
          </form>
        }
        <p class="center"><a routerLink="/login" class="link">← Quay lại đăng nhập</a></p>
      </div>
    </div>
  `,
  styleUrl: './auth.scss',
})
export class ForgotPasswordComponent {
  private auth = inject(AuthService);
  email = '';
  emailPattern = EMAIL_PATTERN;
  loading = signal(false);
  done = signal<string | null>(null);
  error = signal<string | null>(null);

  submit(): void {
    this.loading.set(true);
    this.error.set(null);
    this.auth.forgotPassword(this.email.trim()).subscribe({
      next: (r) => this.done.set(r.message),
      error: (e) => {
        this.loading.set(false);
        this.error.set(errorMessage(e));
      },
    });
  }
}
