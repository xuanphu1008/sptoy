import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { RouterLink } from '@angular/router';
import { ShopApi } from '../../core/api.service';
import { AuthService } from '../../core/auth.service';
import { ROLE } from '../../core/labels';
import { Profile } from '../../core/models';
import { NotifyService } from '../../core/notify.service';
import { emailValidators } from '../../core/validators';

@Component({
  selector: 'tl-profile',
  imports: [ReactiveFormsModule, RouterLink, MatFormFieldModule, MatInputModule, MatButtonModule, MatIconModule, DatePipe],
  template: `
    <div class="card">
      <h2>Hồ sơ của tôi</h2>
      <p class="muted">Quản lý thông tin hồ sơ để bảo mật tài khoản</p>
      @if (profile(); as p) {
        <div class="meta small muted">
          Tên đăng nhập: <b>{{ p.username }}</b> · Vai trò: <b>{{ role[p.role] }}</b> · Thành viên từ {{ p.createdAt | date: 'dd/MM/yyyy' }}
          @if (p.consentAt) { · Đã đồng ý chính sách dữ liệu lúc {{ p.consentAt | date: 'dd/MM/yyyy HH:mm' }} }
        </div>
      }
      <form [formGroup]="f" (ngSubmit)="save()" class="form">
        <mat-form-field class="full"><mat-label>Họ và tên</mat-label><input matInput formControlName="fullName" /></mat-form-field>
        <mat-form-field class="full"><mat-label>Email</mat-label><input matInput type="email" formControlName="email" />
          @if (f.controls.email.hasError('required')) { <mat-error>Vui lòng nhập email</mat-error> }
          @else if (f.controls.email.invalid) { <mat-error>Email không hợp lệ, ví dụ đúng: ten.ho2004&#64;gmail.com</mat-error> }
          @if (profile(); as p) {
            @if (p.emailVerified) { <mat-icon matSuffix class="ev ok" title="Email đã xác nhận">verified</mat-icon> }
            @else { <mat-icon matSuffix class="ev no" title="Email chưa xác nhận">error_outline</mat-icon> }
          }
          @if (f.controls.email.dirty && f.controls.email.valid && profile()?.role === 'CUSTOMER') {
            <mat-hint>Đổi email cần xác nhận lại email mới trước khi đặt hàng tiếp</mat-hint>
          }
        </mat-form-field>
        @if (profile(); as p) {
          @if (!p.emailVerified && p.role === 'CUSTOMER') {
            <div class="unverified small">Email <b>{{ p.email }}</b> chưa được xác nhận.
              <a routerLink="/verify-email" [queryParams]="{ returnUrl: '/account/profile' }" class="link">Gửi lại email xác nhận</a></div>
          }
        }
        <mat-form-field class="full"><mat-label>Số điện thoại</mat-label><input matInput formControlName="phone" />
          @if (f.controls.phone.hasError('pattern')) { <mat-error>SĐT 10-11 số, bắt đầu bằng 0</mat-error> }
        </mat-form-field>
        <button mat-flat-button [disabled]="f.pristine || f.invalid">Lưu thay đổi</button>
      </form>
    </div>
  `,
  styles: `.form { max-width: 520px; margin-top: 16px; } .meta { background: var(--tl-hover); padding: 10px 12px; border-radius: 10px; }
    .ev { margin-right: 8px; } .ev.ok { color: var(--tl-green); } .ev.no { color: var(--tl-orange); }
    .unverified { background: var(--tl-yellow-soft); padding: 10px 12px; border-radius: 12px; margin: 0 0 14px; }
    .link { color: var(--tl-red-dark); font-weight: 700; }`,
})
export class ProfileComponent implements OnInit {
  private api = inject(ShopApi);
  private auth = inject(AuthService);
  private notify = inject(NotifyService);
  role = ROLE;
  profile = signal<Profile | null>(null);
  f = inject(FormBuilder).nonNullable.group({
    fullName: ['', Validators.required],
    email: ['', emailValidators],
    phone: ['', Validators.pattern(/^0\d{9,10}$/)],
  });

  ngOnInit(): void {
    this.api.me().subscribe((p) => {
      this.profile.set(p);
      this.f.reset({ fullName: p.fullName, email: p.email, phone: p.phone ?? '' });
    });
  }

  save(): void {
    const v = this.f.getRawValue();
    this.api.updateMe({ ...v, phone: v.phone || null }).subscribe({
      next: (p) => {
        this.profile.set(p);
        this.f.markAsPristine();
        this.auth.updateUser({ fullName: p.fullName, email: p.email, phone: p.phone, emailVerified: p.emailVerified });
        this.notify.success(p.emailVerified || p.role !== 'CUSTOMER' ? 'Đã cập nhật hồ sơ'
          : `Đã cập nhật hồ sơ. Vui lòng mở liên kết xác nhận đã gửi tới ${p.email}`);
      },
      error: (e) => this.notify.error(e),
    });
  }
}
