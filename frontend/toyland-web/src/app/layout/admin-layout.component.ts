import { BreakpointObserver } from '@angular/cdk/layout';
import { Component, computed, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatMenuModule } from '@angular/material/menu';
import { MatSidenavModule } from '@angular/material/sidenav';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { map } from 'rxjs';
import { AuthService } from '../core/auth.service';
import { ROLE } from '../core/labels';
import { ThemeService } from '../core/theme.service';
import { LogoComponent } from '../shared/logo.component';
import { NotificationBellComponent } from '../shared/notification-bell.component';
import { MatTooltipModule } from '@angular/material/tooltip';
import { OnDestroy, OnInit } from '@angular/core';

interface NavItem { path: string; label: string; icon: string; adminOnly?: boolean; group: string; }

const NAV: NavItem[] = [
  { path: '/admin', label: 'Tổng quan', icon: 'dashboard', group: 'Báo cáo' },
  { path: '/admin/reports', label: 'Báo cáo & Xuất Excel', icon: 'monitoring', adminOnly: true, group: 'Báo cáo' },
  { path: '/admin/orders', label: 'Đơn hàng', icon: 'receipt_long', group: 'Bán hàng' },
  { path: '/admin/returns', label: 'Trả hàng', icon: 'assignment_return', group: 'Bán hàng' },
  { path: '/admin/payments', label: 'Thanh toán & đối soát', icon: 'account_balance_wallet', group: 'Bán hàng' },
  { path: '/admin/reviews', label: 'Đánh giá', icon: 'reviews', group: 'Bán hàng' },
  { path: '/admin/products', label: 'Sản phẩm', icon: 'toys', group: 'Sản phẩm & Kho' },
  { path: '/admin/categories', label: 'Danh mục', icon: 'account_tree', adminOnly: true, group: 'Sản phẩm & Kho' },
  { path: '/admin/brands', label: 'Thương hiệu & Nhân vật', icon: 'verified', adminOnly: true, group: 'Sản phẩm & Kho' },
  { path: '/admin/inventory', label: 'Kho hàng', icon: 'warehouse', group: 'Sản phẩm & Kho' },
  { path: '/admin/suppliers', label: 'Nhà cung cấp', icon: 'local_shipping', group: 'Sản phẩm & Kho' },
  { path: '/admin/vouchers', label: 'Mã giảm giá', icon: 'confirmation_number', adminOnly: true, group: 'Marketing' },
  { path: '/admin/campaigns', label: 'Chương trình KM', icon: 'campaign', adminOnly: true, group: 'Marketing' },
  { path: '/admin/banners', label: 'Banner trang chủ', icon: 'view_carousel', adminOnly: true, group: 'Marketing' },
  { path: '/admin/users', label: 'Người dùng & Điểm thưởng', icon: 'group', adminOnly: true, group: 'Hệ thống' },
  { path: '/admin/partners', label: 'Đối tác & API', icon: 'hub', adminOnly: true, group: 'Hệ thống' },
  { path: '/admin/audit-logs', label: 'Nhật ký hoạt động', icon: 'history', adminOnly: true, group: 'Hệ thống' },
];

@Component({
  selector: 'tl-admin-layout',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, MatSidenavModule, MatListModule, MatIconModule, MatButtonModule, MatMenuModule, MatTooltipModule, LogoComponent, NotificationBellComponent],
  template: `
    <mat-sidenav-container class="shell">
      <mat-sidenav #nav [mode]="mobile() ? 'over' : 'side'" [opened]="!mobile()" class="side">
        <a routerLink="/admin" class="brand"><tl-logo variant="dark" [size]="34" /><span class="tag">Admin</span></a>
        <nav aria-label="Quản trị">
          @for (g of groups(); track g.name) {
            <div class="group">{{ g.name }}</div>
            @for (i of g.items; track i.path) {
              <a class="nav-link" [routerLink]="i.path" routerLinkActive="on" [routerLinkActiveOptions]="{ exact: i.path === '/admin' }"
                 (click)="mobile() && nav.close()">
                <span class="ni"><mat-icon>{{ i.icon }}</mat-icon></span><span>{{ i.label }}</span>
              </a>
            }
          }
        </nav>
        <a class="to-shop" routerLink="/" target="_blank"><mat-icon>storefront</mat-icon>Mở cửa hàng<mat-icon class="ext">open_in_new</mat-icon></a>
      </mat-sidenav>
      <mat-sidenav-content>
        <header class="bar">
          <button mat-icon-button (click)="nav.toggle()" aria-label="Thu gọn menu"><mat-icon>menu</mat-icon></button>
          <div class="hello hide-mobile">Xin chào, <b>{{ auth.user()?.fullName }}</b> 👋</div>
          <span class="spacer"></span>
          <tl-notification-bell variant="admin" />
          <button class="theme" (click)="theme.toggle()" [attr.aria-label]="theme.dark() ? 'Chuyển giao diện sáng' : 'Chuyển giao diện tối'"
                  [matTooltip]="theme.dark() ? 'Giao diện sáng' : 'Giao diện tối'">
            <span class="knob" [class.dark]="theme.dark()"><mat-icon>{{ theme.dark() ? 'dark_mode' : 'light_mode' }}</mat-icon></span>
          </button>
          @if (auth.user(); as u) {
            <button class="user" [matMenuTriggerFor]="m">
              <span class="avatar">{{ u.fullName.charAt(0) }}</span>
              <span class="hide-mobile"><b>{{ u.fullName }}</b><small>{{ role[u.role] }}</small></span>
              <mat-icon>expand_more</mat-icon>
            </button>
            <mat-menu #m="matMenu">
              <a mat-menu-item routerLink="/account/profile"><mat-icon>person</mat-icon>Hồ sơ</a>
              <button mat-menu-item (click)="auth.logout()"><mat-icon>logout</mat-icon>Đăng xuất</button>
            </mat-menu>
          }
        </header>
        <div class="content"><router-outlet /></div>
      </mat-sidenav-content>
    </mat-sidenav-container>
  `,
  styles: `
    .shell { height: 100vh; background: var(--tl-bg); }
    .side { width: 264px; background: #2b2350; color: #cfc8ea; border-right: 0; display: flex; flex-direction: column; }
    :host-context(html.dark) .side { background: #1a1628; }
    .side ::ng-deep .mat-drawer-inner-container { display: flex; flex-direction: column; }
    .brand { display: flex; align-items: center; gap: 8px; padding: 22px 20px 14px; }
    .tag { font-size: 11px; font-weight: 800; background: var(--grad-sun); color: #2b2350; padding: 3px 8px; border-radius: 999px; }
    .group { font-size: 11px; font-weight: 800; text-transform: uppercase; letter-spacing: .12em; color: #8a82ab; padding: 18px 24px 6px; }
    .nav-link { display: flex; align-items: center; gap: 12px; margin: 2px 12px; padding: 8px 12px 8px 8px; border-radius: 16px; font-size: 14px; font-weight: 700;
      transition: background .2s, color .2s; }
    .ni { width: 34px; height: 34px; border-radius: 12px; display: grid; place-items: center; background: rgb(255 255 255 / 6%); transition: background .2s, transform .3s var(--ease-bounce); }
    .ni mat-icon { font-size: 20px; width: 20px; height: 20px; }
    .nav-link:hover { background: rgb(255 255 255 / 6%); color: #fff; }
    .nav-link:hover .ni { transform: rotate(-8deg); }
    .nav-link.on { background: #fff; color: #2b2350; box-shadow: 0 10px 20px -10px rgb(0 0 0 / 50%); }
    .nav-link.on .ni { background: var(--grad-cta); color: #fff; }
    :host-context(html.dark) .nav-link.on { background: #3a3160; color: #fff; box-shadow: inset 0 1px 0 rgb(255 255 255 / 8%), 0 10px 20px -10px rgb(0 0 0 / 60%); }
    .to-shop { margin: auto 16px 20px; display: flex; align-items: center; gap: 10px; padding: 12px 14px; border-radius: 16px; background: rgb(255 255 255 / 7%); font-weight: 700; font-size: 14px; }
    .to-shop:hover { background: rgb(255 255 255 / 12%); color: #fff; }
    .to-shop .ext { margin-left: auto; font-size: 16px; width: 16px; height: 16px; opacity: .6; }
    .bar { position: sticky; top: 0; z-index: 10; display: flex; align-items: center; gap: 12px; height: 68px; padding: 0 20px;
      background: color-mix(in srgb, var(--tl-bg) 80%, transparent); backdrop-filter: blur(12px); -webkit-backdrop-filter: blur(12px); }
    .hello { font-size: 15px; color: var(--tl-muted); }
    .hello b { color: var(--tl-text); }
    .theme { width: 64px; height: 36px; border: 0; border-radius: 999px; padding: 3px; cursor: pointer; background: var(--p-butter); box-shadow: inset 0 2px 4px rgb(0 0 0 / 12%); transition: background .3s; }
    :host-context(html.dark) .theme { background: #3a2f63; }
    .knob { width: 30px; height: 30px; border-radius: 50%; display: grid; place-items: center; background: #fff; color: #f0a92a; box-shadow: 0 3px 6px rgb(0 0 0 / 20%);
      transition: transform .4s var(--ease-bounce), background .3s, color .3s; }
    .knob.dark { transform: translateX(28px) rotate(-200deg); background: #1f1a2e; color: #ffd66b; }
    .knob mat-icon { font-size: 18px; width: 18px; height: 18px; }
    .user { display: flex; align-items: center; gap: 10px; border: 0; background: var(--tl-card); box-shadow: var(--clay-sm); border-radius: 999px; padding: 4px 10px 4px 4px;
      cursor: pointer; color: var(--tl-text); font: inherit; text-align: left; }
    .user b { display: block; font-size: 13px; line-height: 1.2; } .user small { color: var(--tl-muted); font-size: 12px; }
    .avatar { width: 34px; height: 34px; border-radius: 50%; display: grid; place-items: center; background: var(--grad-sun); color: #2b2350; font-weight: 800; font-family: var(--font-display); text-transform: uppercase; }
    .content { padding: 8px 28px 40px; max-width: 1440px; animation: tl-fade-up .3s ease-out both; }
    /* Admin: gon hon cua hang (bo goc nho hon) */
    .content { --tl-radius: 20px; }
    @media (max-width: 600px) { .content { padding: 8px 12px 24px; } .bar { padding: 0 8px; } }
  `,
})
export class AdminLayoutComponent implements OnInit, OnDestroy {
  auth = inject(AuthService);
  theme = inject(ThemeService);
  role = ROLE;
  mobile = toSignal(inject(BreakpointObserver).observe('(max-width: 960px)').pipe(map((r) => r.matches)), { initialValue: false });

  groups = computed(() => {
    const isAdmin = this.auth.isAdmin();
    const out: { name: string; items: NavItem[] }[] = [];
    for (const i of NAV.filter((n) => isAdmin || !n.adminOnly)) {
      let g = out.find((x) => x.name === i.group);
      if (!g) out.push((g = { name: i.group, items: [] }));
      g.items.push(i);
    }
    return out;
  });

  ngOnInit(): void {
    this.theme.attach(true);
  }

  ngOnDestroy(): void {
    this.theme.attach(false); // ve cua hang -> luon giao dien sang
  }
}
