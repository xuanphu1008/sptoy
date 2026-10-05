import { Component, inject, signal } from '@angular/core';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth.service';
import { NotifyService, errorMessage } from '../../core/notify.service';
import { PHONE_PATTERN, emailValidators } from '../../core/validators';

function matchPassword(g: AbstractControl): ValidationErrors | null {
  return g.get('password')?.value === g.get('confirm')?.value ? null : { mismatch: true };
}

@Component({
  selector: 'tl-register',
  imports: [ReactiveFormsModule, RouterLink, MatFormFieldModule, MatInputModule, MatButtonModule, MatIconModule, MatCheckboxModule],
  template: `
    <div class="container page">
      <div class="auth-card card">
        <div class="head"><mat-icon>celebration</mat-icon><h1>Tạo tài khoản</h1><p class="muted">Nhận ưu đãi thành viên & theo dõi đơn hàng dễ dàng</p></div>
        @if (error()) { <div class="alert err">{{ error() }}</div> }
        <form [formGroup]="f" (ngSubmit)="submit()">
          <mat-form-field class="full"><mat-label>Họ và tên</mat-label><input matInput formControlName="fullName" autocomplete="name" />
            @if (f.controls.fullName.hasError('required')) { <mat-error>Vui lòng nhập họ và tên</mat-error> }
          </mat-form-field>
          <mat-form-field class="full"><mat-label>Tên đăng nhập</mat-label><input matInput formControlName="username" autocomplete="username" />
            <mat-hint>4-50 ký tự: chữ không dấu, số, dấu . hoặc _</mat-hint>
            @if (f.controls.username.hasError('required')) { <mat-error>Vui lòng nhập tên đăng nhập</mat-error> }
            @else if (f.controls.username.hasError('pattern')) { <mat-error>Chỉ dùng chữ không dấu, số, dấu . và _ (không cách)</mat-error> }
            @else if (f.controls.username.hasError('minlength')) { <mat-error>Tên đăng nhập tối thiểu 4 ký tự</mat-error> }
          </mat-form-field>
          <mat-form-field class="full"><mat-label>Email</mat-label><input matInput type="email" formControlName="email" autocomplete="email" placeholder="vd: ten.ho2004@gmail.com" />
            @if (f.controls.email.hasError('required')) { <mat-error>Vui lòng nhập email</mat-error> }
            @else if (f.controls.email.invalid) { <mat-error>Email không hợp lệ, ví dụ đúng: ten.ho2004&#64;gmail.com</mat-error> }
          </mat-form-field>
          <mat-form-field class="full"><mat-label>Số điện thoại (không bắt buộc)</mat-label><input matInput type="tel" formControlName="phone" autocomplete="tel" />
            @if (f.controls.phone.hasError('pattern')) { <mat-error>SĐT 10-11 số, bắt đầu bằng 0</mat-error> }
          </mat-form-field>
          <mat-form-field class="full"><mat-label>Mật khẩu</mat-label><input matInput type="password" formControlName="password" autocomplete="new-password" />
            @if (f.controls.password.hasError('required')) { <mat-error>Vui lòng nhập mật khẩu</mat-error> }
            @else if (f.controls.password.hasError('minlength')) { <mat-error>Mật khẩu tối thiểu 6 ký tự</mat-error> }
          </mat-form-field>
          <mat-form-field class="full"><mat-label>Nhập lại mật khẩu</mat-label><input matInput type="password" formControlName="confirm" autocomplete="new-password" />
            @if (f.controls.confirm.hasError('required')) { <mat-error>Vui lòng nhập lại mật khẩu</mat-error> }
          </mat-form-field>
          @if (f.hasError('mismatch') && f.controls.confirm.touched && f.controls.confirm.value) { <div class="alert err">Mật khẩu nhập lại không khớp</div> }
          <mat-checkbox formControlName="agreeTerms">
            Tôi đồng ý với <a routerLink="/policy/terms" target="_blank" class="link">Điều khoản sử dụng</a> và
            <a routerLink="/policy/privacy" target="_blank" class="link">Chính sách bảo mật dữ liệu cá nhân</a>
          </mat-checkbox>
          <button mat-flat-button class="full submit" [disabled]="loading() || !f.value.agreeTerms">{{ loading() ? 'Đang tạo tài khoản…' : 'Đăng ký' }}</button>
        </form>
        <p class="center">Đã có tài khoản? <a routerLink="/login" class="link bold">Đăng nhập</a></p>
      </div>
    </div>
  `,
  styleUrl: './auth.scss',
})
export class RegisterComponent {
  private fb = inject(FormBuilder);
  private auth = inject(AuthService);
  private router = inject(Router);
  private notify = inject(NotifyService);
  private returnUrl = inject(ActivatedRoute).snapshot.queryParams['returnUrl'] || '/';
  loading = signal(false);
  error = signal<string | null>(null);

  f = this.fb.nonNullable.group(
    {
      fullName: ['', [Validators.required, Validators.maxLength(100)]],
      username: ['', [Validators.required, Validators.minLength(4), Validators.maxLength(50), Validators.pattern(/^[a-zA-Z0-9._]+$/)]],
      email: ['', emailValidators],
      phone: ['', Validators.pattern(PHONE_PATTERN)],
      password: ['', [Validators.required, Validators.minLength(6)]],
      confirm: ['', Validators.required],
      agreeTerms: [false, Validators.requiredTrue],
    },
    { validators: matchPassword },
  );

  submit(): void {
    if (this.f.invalid) {
      this.f.markAllAsTouched();
      return;
    }
    const { confirm: _c, ...body } = this.f.getRawValue();
    this.loading.set(true);
    this.error.set(null);
    this.auth.register({ ...body, username: body.username.trim(), email: body.email.trim(), phone: body.phone || undefined }).subscribe({
      next: (r) => {
        this.notify.success(`Chào mừng ${r.user.fullName} đến với ToyLand! Hãy xác nhận email để bắt đầu đặt hàng.`);
        // Giong Laravel: dang ky xong -> trang "Kiem tra hop thu" (van dang nhap, chua dat hang duoc)
        this.router.navigate(['/verify-email'], { queryParams: { returnUrl: this.returnUrl } });
      },
      error: (e) => {
        this.loading.set(false);
        this.error.set(errorMessage(e));
      },
    });
  }
}
