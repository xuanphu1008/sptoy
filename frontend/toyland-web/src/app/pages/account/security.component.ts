import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { ShopApi } from '../../core/api.service';
import { AuthService } from '../../core/auth.service';
import { NotifyService } from '../../core/notify.service';
import { confirmDialog } from '../../shared/confirm-dialog.component';

@Component({
  selector: 'tl-security',
  imports: [FormsModule, MatFormFieldModule, MatInputModule, MatButtonModule, MatIconModule],
  template: `
    <div class="card">
      <h2>Đổi mật khẩu</h2>
      <form class="form" (ngSubmit)="change()">
        <mat-form-field class="full"><mat-label>Mật khẩu hiện tại</mat-label><input matInput type="password" name="cur" [(ngModel)]="cur" required /></mat-form-field>
        <mat-form-field class="full"><mat-label>Mật khẩu mới (≥ 6 ký tự)</mat-label><input matInput type="password" name="p1" [(ngModel)]="p1" required /></mat-form-field>
        <mat-form-field class="full"><mat-label>Nhập lại mật khẩu mới</mat-label><input matInput type="password" name="p2" [(ngModel)]="p2" required /></mat-form-field>
        <button mat-flat-button>Cập nhật mật khẩu</button>
      </form>
      <p class="small muted">Sau khi đổi mật khẩu, các phiên đăng nhập trên thiết bị khác sẽ bị đăng xuất.</p>
    </div>
    @if (!auth.isStaff()) {
      <div class="card danger">
        <h2><mat-icon>warning</mat-icon> Xoá tài khoản</h2>
        <p>Theo Nghị định 13/2023/NĐ-CP, bạn có quyền yêu cầu xoá dữ liệu cá nhân. Thông tin cá nhân sẽ được ẩn danh hoá;
          lịch sử đơn hàng vẫn được lưu phục vụ nghĩa vụ kế toán. Hành động này <b>không thể hoàn tác</b>.</p>
        <button mat-stroked-button class="del" (click)="deleteAccount()">Xoá tài khoản của tôi</button>
      </div>
    }
  `,
  styles: `
    .form { max-width: 440px; }
    .danger { border: 1.5px solid #ffcdd2; }
    .danger h2 { color: var(--tl-red-dark); display: flex; align-items: center; gap: 8px; }
    .del { color: var(--tl-red-dark); border-color: var(--tl-red); }
  `,
})
export class SecurityComponent {
  private api = inject(ShopApi);
  private notify = inject(NotifyService);
  private dialog = inject(MatDialog);
  auth = inject(AuthService);
  cur = '';
  p1 = '';
  p2 = '';

  change(): void {
    if (this.p1.length < 6) return this.notify.error('Mật khẩu mới tối thiểu 6 ký tự');
    if (this.p1 !== this.p2) return this.notify.error('Mật khẩu nhập lại không khớp');
    this.api.changePassword({ currentPassword: this.cur, newPassword: this.p1 }).subscribe({
      next: (r) => {
        // Backend thu hoi moi refresh token -> dang nhap lai ngay de lay phien moi cho thiet bi nay
        const username = this.auth.user()!.username;
        this.auth.login(username, this.p1).subscribe({ error: () => this.auth.logout() });
        this.notify.success(r.message);
        this.cur = this.p1 = this.p2 = '';
      },
      error: (e) => this.notify.error(e),
    });
  }

  deleteAccount(): void {
    confirmDialog<string>(this.dialog, {
      title: 'Xác nhận xoá tài khoản', danger: true, confirmText: 'Xoá vĩnh viễn',
      message: 'Nhập mật khẩu hiện tại để xác nhận.', input: { label: 'Mật khẩu', required: true },
    }).subscribe((pw) => {
      if (!pw) return;
      this.api.deleteAccount(pw).subscribe({
        next: (r) => {
          this.notify.success(r.message);
          this.auth.logout();
        },
        error: (e) => this.notify.error(e),
      });
    });
  }
}
