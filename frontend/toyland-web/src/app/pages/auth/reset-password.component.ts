import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth.service';
import { NotifyService, errorMessage } from '../../core/notify.service';

@Component({
  selector: 'tl-reset-password',
  imports: [FormsModule, RouterLink, MatFormFieldModule, MatInputModule, MatButtonModule, MatIconModule],
  template: `
    <div class="container page">
      <div class="auth-card card">
        <div class="head"><mat-icon>password</mat-icon><h1>Đặt lại mật khẩu</h1></div>
        @if (!token) {
          <div class="alert err">Liên kết không hợp lệ. Vui lòng yêu cầu lại.</div>
          <a mat-stroked-button class="full" routerLink="/forgot-password">Yêu cầu liên kết mới</a>
        } @else {
          @if (error()) { <div class="alert err">{{ error() }}</div> }
          <form (ngSubmit)="submit()">
            <mat-form-field class="full"><mat-label>Mật khẩu mới</mat-label><input matInput type="password" name="p1" [(ngModel)]="p1" minlength="6" required /></mat-form-field>
            <mat-form-field class="full"><mat-label>Nhập lại mật khẩu mới</mat-label><input matInput type="password" name="p2" [(ngModel)]="p2" required /></mat-form-field>
            <button mat-flat-button class="full submit" [disabled]="loading()">Đổi mật khẩu</button>
          </form>
        }
      </div>
    </div>
  `,
  styleUrl: './auth.scss',
})
export class ResetPasswordComponent {
  private auth = inject(AuthService);
  private router = inject(Router);
  private notify = inject(NotifyService);
  token = inject(ActivatedRoute).snapshot.queryParams['token'] as string | undefined;
  p1 = '';
  p2 = '';
  loading = signal(false);
  error = signal<string | null>(null);

  submit(): void {
    if (this.p1.length < 6) return this.error.set('Mật khẩu tối thiểu 6 ký tự');
    if (this.p1 !== this.p2) return this.error.set('Mật khẩu nhập lại không khớp');
    this.loading.set(true);
    this.auth.resetPassword(this.token!, this.p1).subscribe({
      next: (r) => {
        this.notify.success(r.message);
        this.auth.logout(false);
        this.router.navigate(['/login']);
      },
      error: (e) => {
        this.loading.set(false);
        this.error.set(errorMessage(e));
      },
    });
  }
}
