import { DatePipe, DecimalPipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSelectModule } from '@angular/material/select';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { AdminApi } from '../../core/admin-api.service';
import { AuthService } from '../../core/auth.service';
import { ROLE } from '../../core/labels';
import { AdminUser, LoyaltySummary, Page, PointTx, Role } from '../../core/models';
import { POINT_TYPE } from '../../core/labels';
import { NotifyService } from '../../core/notify.service';
import { confirmDialog } from '../../shared/confirm-dialog.component';
import { openForm } from '../../shared/form-dialog.component';
import { EMAIL_PATTERN } from '../../core/validators';

/** Hop thoai diem thuong cua 1 khach: so du, hang, lich su, dieu chinh thu cong. */
@Component({
  selector: 'tl-user-points-dialog',
  imports: [FormsModule, DatePipe, DecimalPipe, MatDialogModule, MatFormFieldModule, MatInputModule, MatButtonModule, MatIconModule, MatPaginatorModule],
  template: `
    <h2 mat-dialog-title>Điểm thưởng · {{ data.user.fullName }}</h2>
    <mat-dialog-content>
      @if (sum(); as s) {
        <div class="kpis">
          <div><b>{{ s.points | number }}</b><span>điểm hiện có (≈ {{ s.pointsValue | number }}đ)</span></div>
          <div><b>{{ s.tier.label }}</b><span>tích luỹ {{ s.lifetimePoints | number }} điểm · x{{ s.tier.multiplier }}</span></div>
        </div>
      }
      <form class="adj" (submit)="adjust(); $event.preventDefault()">
        <mat-form-field><mat-label>Cộng / trừ điểm</mat-label><input matInput type="number" [(ngModel)]="delta" name="delta" />
          <mat-hint>Số âm để trừ</mat-hint></mat-form-field>
        <mat-form-field class="grow"><mat-label>Lý do</mat-label><input matInput [(ngModel)]="note" name="note" maxlength="300" /></mat-form-field>
        <button mat-flat-button type="submit" [disabled]="!delta">Lưu</button>
      </form>
      <table class="simple">
        <tr><th>Thời gian</th><th>Loại</th><th class="r">Điểm</th><th class="r">Số dư</th><th>Ghi chú</th></tr>
        @for (t of history()?.content ?? []; track t.id) {
          <tr><td class="small">{{ t.createdAt | date: 'dd/MM/yy HH:mm' }}</td><td><span class="chip" [class]="typeClass[t.type]">{{ types[t.type] }}</span></td>
            <td class="r bold" [class.plus]="t.points > 0">{{ t.points > 0 ? '+' : '' }}{{ t.points | number }}</td>
            <td class="r">{{ t.balanceAfter | number }}</td><td class="small">{{ t.note }}</td></tr>
        } @empty { <tr><td colspan="5" class="muted">Chưa có giao dịch điểm</td></tr> }
      </table>
      @if ((history()?.totalPages ?? 0) > 1) {
        <mat-paginator [length]="history()?.totalElements ?? 0" [pageIndex]="history()?.page ?? 0" [pageSize]="8" [hidePageSize]="true" (page)="loadHistory($event.pageIndex)" />
      }
    </mat-dialog-content>
    <mat-dialog-actions align="end"><button mat-button mat-dialog-close>Đóng</button></mat-dialog-actions>
  `,
  styles: `
    mat-dialog-content { min-width: min(640px, 88vw); }
    .kpis { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; margin-bottom: 12px; }
    .kpis div { background: var(--p-butter); border-radius: 16px; padding: 12px 14px; display: flex; flex-direction: column; }
    html.dark .kpis div { background: var(--tl-yellow-soft); }
    .kpis b { font-family: var(--font-display); font-size: 22px; } .kpis span { font-size: 12px; color: var(--tl-muted); }
    .adj { display: flex; gap: 8px; align-items: flex-start; flex-wrap: wrap; } .adj mat-form-field { width: 150px; } .adj .grow { flex: 1; min-width: 200px; }
    .adj button { margin-top: 8px; }
    table.simple { width: 100%; border-collapse: collapse; font-size: 14px; margin-top: 8px; }
    table.simple td, table.simple th { padding: 8px 6px; border-bottom: 1px solid var(--tl-border); text-align: left; }
    .r { text-align: right !important; } .plus { color: var(--tl-green); }
  `,
})
export class UserPointsDialogComponent implements OnInit {
  data = inject<{ user: AdminUser }>(MAT_DIALOG_DATA);
  private api = inject(AdminApi);
  private notify = inject(NotifyService);
  sum = signal<LoyaltySummary | null>(null);
  history = signal<Page<PointTx> | null>(null);
  types = POINT_TYPE;
  typeClass: Record<string, string> = { EARN: 'green', REDEEM: 'blue', REFUND: 'yellow', ADJUST: 'grey' };
  delta: number | null = null;
  note = '';
  changed = false;

  ngOnInit(): void {
    this.api.userPoints(this.data.user.id).subscribe((s) => this.sum.set(s));
    this.loadHistory(0);
  }

  loadHistory(p: number): void {
    this.api.userPointHistory(this.data.user.id, { page: p, size: 8 }).subscribe((h) => this.history.set(h));
  }

  adjust(): void {
    const d = Math.trunc(Number(this.delta));
    if (!d) return;
    this.api.adjustPoints(this.data.user.id, d, this.note || null).subscribe({
      next: (s) => {
        this.sum.set(s);
        this.changed = true;
        this.delta = null;
        this.note = '';
        this.loadHistory(0);
        this.notify.success('Đã cập nhật điểm thưởng');
      },
      error: (e) => this.notify.error(e),
    });
  }
}

@Component({
  selector: 'tl-users',
  imports: [FormsModule, DatePipe, MatTableModule, MatPaginatorModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatButtonModule, MatIconModule, MatTooltipModule],
  template: `
    <div class="page-title"><h1>Người dùng</h1><span class="spacer"></span>
      <button mat-flat-button (click)="edit(null)"><mat-icon>person_add</mat-icon>Thêm người dùng</button></div>
    <div class="filters">
      <mat-form-field class="search-field" subscriptSizing="dynamic"><mat-icon matPrefix>search</mat-icon><input matInput [(ngModel)]="keyword" (keyup.enter)="load(0)" placeholder="Tìm tên, username, email, SĐT…" aria-label="Tìm người dùng" />@if (keyword) { <button mat-icon-button matSuffix type="button" (click)="keyword = ''; load(0)" aria-label="Xoá từ khoá"><mat-icon>close</mat-icon></button> }</mat-form-field>
      <mat-form-field><mat-label>Vai trò</mat-label>
        <mat-select [(ngModel)]="role" (selectionChange)="load(0)"><mat-option [value]="null">Tất cả</mat-option>
          @for (r of roles; track r[0]) { <mat-option [value]="r[0]">{{ r[1] }}</mat-option> }</mat-select></mat-form-field>
    </div>
    <div class="table-wrap">
      <table mat-table [dataSource]="page()?.content ?? []">
        <ng-container matColumnDef="user"><th mat-header-cell *matHeaderCellDef>Người dùng</th>
          <td mat-cell *matCellDef="let u"><b>{{ u.fullName }}</b><div class="small muted">&#64;{{ u.username }}</div></td></ng-container>
        <ng-container matColumnDef="contact"><th mat-header-cell *matHeaderCellDef>Liên hệ</th>
          <td mat-cell *matCellDef="let u">{{ u.email }}@if (u.role === 'CUSTOMER' && !u.emailVerified) { <span class="chip yellow unv" title="Khách chưa bấm link xác nhận trong email">Chưa xác nhận</span> }<div class="small muted">{{ u.phone }}</div></td></ng-container>
        <ng-container matColumnDef="role"><th mat-header-cell *matHeaderCellDef>Vai trò</th>
          <td mat-cell *matCellDef="let u"><span class="chip" [class.red]="u.role === 'ADMIN'" [class.blue]="u.role === 'STAFF'">{{ roleLabel[u.role] }}</span></td></ng-container>
        <ng-container matColumnDef="orders"><th mat-header-cell *matHeaderCellDef>Đơn hàng</th><td mat-cell *matCellDef="let u">{{ u.orderCount }}</td></ng-container>
        <ng-container matColumnDef="points"><th mat-header-cell *matHeaderCellDef>Điểm thưởng</th>
          <td mat-cell *matCellDef="let u">@if (u.role === 'CUSTOMER') {
            <button class="pts" (click)="points(u)" [attr.aria-label]="'Điểm thưởng của ' + u.fullName"><b>{{ u.points }}</b>
              <span class="chip" [class.yellow]="u.tier === 'Vàng'" [class.blue]="u.tier === 'Kim cương'" [class.grey]="u.tier === 'Bạc' || u.tier === 'Thành viên'">{{ u.tier }}</span></button>
          } @else { <span class="muted">—</span> }</td></ng-container>
        <ng-container matColumnDef="created"><th mat-header-cell *matHeaderCellDef>Ngày tạo</th><td mat-cell *matCellDef="let u" class="small">{{ u.createdAt | date: 'dd/MM/yyyy' }}</td></ng-container>
        <ng-container matColumnDef="status"><th mat-header-cell *matHeaderCellDef>Trạng thái</th>
          <td mat-cell *matCellDef="let u">
            @if (!u.enabled) { <span class="chip grey">Vô hiệu</span> }
            @else if (u.locked) { <span class="chip orange" [matTooltip]="'Khoá đến ' + (u.lockedUntil | date: 'HH:mm dd/MM')">Tạm khoá</span> }
            @else { <span class="chip green">Hoạt động</span> }
          </td></ng-container>
        <ng-container matColumnDef="actions"><th mat-header-cell *matHeaderCellDef></th>
          <td mat-cell *matCellDef="let u" class="nowrap"><div class="cell-actions">
            @if (u.locked) { <button mat-icon-button (click)="unlock(u)" matTooltip="Mở khoá" aria-label="Mở khoá đăng nhập"><mat-icon>lock_open</mat-icon></button> }
            <button mat-icon-button (click)="edit(u)" matTooltip="Sửa" aria-label="Sửa người dùng"><mat-icon>edit</mat-icon></button>
            @if (u.id !== auth.user()?.id) { <button mat-icon-button (click)="remove(u)" class="danger-icon" matTooltip="Xoá / vô hiệu" aria-label="Xoá người dùng"><mat-icon>delete</mat-icon></button> }
          </div></td></ng-container>
        <tr mat-header-row *matHeaderRowDef="cols"></tr>
        <tr mat-row *matRowDef="let row; columns: cols"></tr>
      </table>
      <mat-paginator [length]="page()?.totalElements ?? 0" [pageIndex]="page()?.page ?? 0" [pageSize]="20" [hidePageSize]="true" (page)="go($event)" />
    </div>
  `,
  styles: `.unv { margin-left: 6px; padding: 2px 8px; font-size: 11px; } .pts { display: inline-flex; align-items: center; gap: 8px; border: 0; background: none; color: inherit; cursor: pointer; padding: 4px; border-radius: 10px; font: inherit; }
    .pts:hover { background: var(--tl-hover); }`,
})
export class UsersComponent implements OnInit {
  private api = inject(AdminApi);
  private dialog = inject(MatDialog);
  private notify = inject(NotifyService);
  auth = inject(AuthService);
  roleLabel: Record<string, string> = ROLE;
  roles = Object.entries(ROLE) as [Role, string][];
  cols = ['user', 'contact', 'role', 'orders', 'points', 'created', 'status', 'actions'];
  page = signal<Page<AdminUser> | null>(null);
  keyword = '';
  role: Role | null = null;
  private current = 0;

  ngOnInit(): void {
    this.load(0);
  }

  load(p: number): void {
    this.current = p;
    this.api.users({ keyword: this.keyword.trim(), role: this.role, page: p, size: 20 }).subscribe((r) => this.page.set(r));
  }

  go(e: PageEvent): void {
    this.load(e.pageIndex);
  }

  edit(u: AdminUser | null): void {
    openForm(this.dialog, {
      title: u ? `Sửa người dùng @${u.username}` : 'Thêm người dùng',
      value: (u ? { ...u, password: '' } : { username: '', password: '', fullName: '', email: '', phone: '', role: 'STAFF', enabled: true }) as Record<string, unknown>,
      fields: [
        { key: 'username', label: 'Tên đăng nhập', required: true },
        { key: 'password', label: u ? 'Mật khẩu mới (để trống nếu giữ nguyên)' : 'Mật khẩu', required: !u },
        { key: 'fullName', label: 'Họ tên', required: true },
        { key: 'email', label: 'Email', required: true, pattern: EMAIL_PATTERN, patternError: 'Email không hợp lệ, ví dụ đúng: ten.ho2004@gmail.com' },
        { key: 'phone', label: 'Số điện thoại' },
        { key: 'role', label: 'Vai trò', type: 'select', options: this.roles.map(([value, label]) => ({ value, label })) },
        { key: 'enabled', label: 'Cho phép đăng nhập', type: 'toggle' },
      ],
      save: (x) => this.api.saveUser(u?.id ?? null, x),
    }).subscribe((r) => r && this.load(this.current));
  }

  points(u: AdminUser): void {
    this.dialog.open(UserPointsDialogComponent, { data: { user: u }, autoFocus: false, maxWidth: '96vw' })
      .afterClosed().subscribe(() => this.load(this.current));
  }

  unlock(u: AdminUser): void {
    this.api.unlockUser(u.id).subscribe({ next: (r) => { this.notify.success(r.message); this.load(this.current); }, error: (e) => this.notify.error(e) });
  }

  remove(u: AdminUser): void {
    confirmDialog(this.dialog, { title: `Xoá người dùng @${u.username}?`, message: 'Người dùng đã có đơn hàng sẽ bị vô hiệu hoá thay vì xoá.', danger: true, confirmText: 'Xoá' })
      .subscribe((ok) => ok && this.api.deleteUser(u.id).subscribe({ next: (r) => { this.notify.success(r.message); this.load(this.current); }, error: (e) => this.notify.error(e) }));
  }
}
