import { Component, inject } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { RouterLink, RouterLinkActive, RouterOutlet, Routes } from '@angular/router';
import { AuthService } from '../../core/auth.service';

/** Khung trang tai khoan: menu ben trai + noi dung */
@Component({
  selector: 'tl-account-layout',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, MatIconModule],
  template: `
    <div class="container page layout">
      <aside class="card side">
        <div class="me">
          <div class="avatar">{{ auth.user()?.fullName?.charAt(0) }}</div>
          <div><b>{{ auth.user()?.fullName }}</b><div class="small muted">&#64;{{ auth.user()?.username }}</div></div>
        </div>
        <a routerLink="profile" routerLinkActive="on"><mat-icon>person</mat-icon>Hồ sơ của tôi</a>
        <a routerLink="orders" routerLinkActive="on"><mat-icon>receipt_long</mat-icon>Đơn mua</a>
        <a routerLink="points" routerLinkActive="on"><mat-icon>stars</mat-icon>Điểm thưởng</a>
        <a routerLink="wishlist" routerLinkActive="on"><mat-icon>favorite</mat-icon>Yêu thích</a>
        <a routerLink="addresses" routerLinkActive="on"><mat-icon>home_pin</mat-icon>Sổ địa chỉ</a>
        <a routerLink="security" routerLinkActive="on"><mat-icon>shield</mat-icon>Bảo mật & tài khoản</a>
      </aside>
      <section><router-outlet /></section>
    </div>
  `,
  styles: `
    .layout { display: grid; grid-template-columns: 250px minmax(0, 1fr); gap: 20px; align-items: start; }
    .side { padding: 8px; position: sticky; top: 130px; }
    .me { display: flex; gap: 10px; align-items: center; padding: 12px; border-bottom: 1px solid #f0f0f0; margin-bottom: 6px; }
    .avatar { width: 44px; height: 44px; border-radius: 50%; background: var(--tl-red); color: #fff; display: grid; place-items: center; font-weight: 900; font-size: 18px; }
    .side a { display: flex; align-items: center; gap: 10px; padding: 10px 12px; border-radius: 10px; font-weight: 600; font-size: 14px; }
    .side a.on, .side a:hover { background: var(--tl-red-soft); color: var(--tl-red); }
    @media (max-width: 800px) {
      .layout { grid-template-columns: minmax(0, 1fr); }
      .side { position: static; display: flex; overflow-x: auto; gap: 4px; }
      .me { display: none; }
      .side a { white-space: nowrap; }
    }
  `,
})
export class AccountLayoutComponent {
  auth = inject(AuthService);
}

export const ACCOUNT_ROUTES: Routes = [
  {
    path: '',
    component: AccountLayoutComponent,
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'profile' },
      { path: 'profile', title: 'Hồ sơ của tôi', loadComponent: () => import('./profile.component').then((m) => m.ProfileComponent) },
      { path: 'orders', title: 'Đơn mua', loadComponent: () => import('./orders.component').then((m) => m.OrdersComponent) },
      { path: 'orders/:id', title: 'Chi tiết đơn hàng', loadComponent: () => import('./order-detail.component').then((m) => m.OrderDetailComponent) },
      { path: 'points', title: 'Điểm thưởng', loadComponent: () => import('./points.component').then((m) => m.PointsComponent) },
      { path: 'wishlist', title: 'Yêu thích', loadComponent: () => import('./wishlist.component').then((m) => m.WishlistComponent) },
      { path: 'addresses', title: 'Sổ địa chỉ', loadComponent: () => import('./addresses.component').then((m) => m.AddressesComponent) },
      { path: 'security', title: 'Bảo mật', loadComponent: () => import('./security.component').then((m) => m.SecurityComponent) },
    ],
  },
];
