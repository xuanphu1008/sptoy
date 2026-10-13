import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth.service';
import { NotifyService, errorMessage } from '../../core/notify.service';

@Component({
  selector: 'tl-login',
  imports: [ReactiveFormsModule, RouterLink, MatFormFieldModule, MatInputModule, MatButtonModule, MatIconModule],
  template: `
    <div class="container page">
      <div class="auth-card card">
        <div class="head"><mat-icon>toys</mat-icon><h1>Đăng nhập</h1><p class="muted">Chào mừng bạn quay lại ToyLand!</p></div>
        @if (expired) { <div class="alert">Phiên đăng nhập đã hết hạn, vui lòng đăng nhập lại.</div> }
        @if (error()) { <div class="alert err">{{ error() }}</div> }
        <form [formGroup]="f" (ngSubmit)="submit()">
          <mat-form-field class="full"><mat-label>Tên đăng nhập</mat-label>
            <input matInput formControlName="username" autocomplete="username" /><mat-icon matPrefix>person</mat-icon>
          </mat-form-field>
          <mat-form-field class="full"><mat-label>Mật khẩu</mat-label>
            <input matInput [type]="show() ? 'text' : 'password'" formControlName="password" autocomplete="current-password" />
            <mat-icon matPrefix>lock</mat-icon>
            <button mat-icon-button matSuffix type="button" (click)="show.set(!show())"><mat-icon>{{ show() ? 'visibility_off' : 'visibility' }}</mat-icon></button>
          </mat-form-field>
          <div class="row end"><a routerLink="/forgot-password" class="link small">Quên mật khẩu?</a></div>
          <button mat-flat-button class="full submit" [disabled]="loading()">{{ loading() ? 'Đang đăng nhập…' : 'Đăng nhập' }}</button>
        </form>
        <p class="center">Chưa có tài khoản? <a routerLink="/register" [queryParams]="{ returnUrl }" class="link bold">Đăng ký ngay</a></p>
      </div>
    </div>
  `,
  styleUrl: './auth.scss',
})
export class LoginComponent {
  private fb = inject(FormBuilder);
  private auth = inject(AuthService);
  private router = inject(Router);
  private notify = inject(NotifyService);
  private route = inject(ActivatedRoute);

  returnUrl = this.route.snapshot.queryParams['returnUrl'] || null;
  expired = !!this.route.snapshot.queryParams['expired'];
  show = signal(false);
  loading = signal(false);
  error = signal<string | null>(null);
  f = this.fb.nonNullable.group({ username: ['', Validators.required], password: ['', Validators.required] });

  submit(): void {
    if (this.f.invalid) {
      this.f.markAllAsTouched();
      return;
    }
    this.loading.set(true);
    this.error.set(null);
    const { username, password } = this.f.getRawValue();
    this.auth.login(username.trim(), password).subscribe({
      next: (r) => {
        this.notify.success(`Xin chào ${r.user.fullName}!`);
        const staff = r.user.role !== 'CUSTOMER';
        this.router.navigateByUrl(this.returnUrl || (staff ? '/admin' : '/'));
      },
      error: (e) => {
        this.loading.set(false);
        this.error.set(errorMessage(e));
      },
    });
  }
}
