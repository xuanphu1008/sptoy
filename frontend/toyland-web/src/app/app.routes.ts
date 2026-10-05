import { Routes } from '@angular/router';
import { authGuard, guestGuard, staffGuard } from './core/guards';
import { AdminLayoutComponent } from './layout/admin-layout.component';
import { ShopLayoutComponent } from './layout/shop-layout.component';

export const routes: Routes = [
  {
    path: 'admin',
    component: AdminLayoutComponent,
    canActivate: [staffGuard],
    loadChildren: () => import('./pages/admin/admin.routes').then((m) => m.ADMIN_ROUTES),
  },
  {
    path: '',
    component: ShopLayoutComponent,
    children: [
      { path: '', title: 'ToyLand - Thế giới đồ chơi', loadComponent: () => import('./pages/shop/home.component').then((m) => m.HomeComponent) },
      { path: 'products', title: 'Sản phẩm - ToyLand', loadComponent: () => import('./pages/shop/product-list.component').then((m) => m.ProductListComponent) },
      { path: 'products/:slug', loadComponent: () => import('./pages/shop/product-detail.component').then((m) => m.ProductDetailComponent) },
      { path: 'cart', title: 'Giỏ hàng - ToyLand', loadComponent: () => import('./pages/shop/cart.component').then((m) => m.CartComponent) },
      { path: 'checkout', title: 'Thanh toán - ToyLand', canActivate: [authGuard], loadComponent: () => import('./pages/shop/checkout.component').then((m) => m.CheckoutComponent) },
      { path: 'order-success/:id', title: 'Đặt hàng thành công', canActivate: [authGuard], loadComponent: () => import('./pages/shop/order-success.component').then((m) => m.OrderSuccessComponent) },
      { path: 'payment/momo-return', title: 'Kết quả thanh toán', loadComponent: () => import('./pages/shop/momo-return.component').then((m) => m.MomoReturnComponent) },
      { path: 'track-order', title: 'Tra cứu đơn hàng', loadComponent: () => import('./pages/shop/track-order.component').then((m) => m.TrackOrderComponent) },
      { path: 'policy/:type', loadComponent: () => import('./pages/shop/policy.component').then((m) => m.PolicyComponent) },

      { path: 'login', title: 'Đăng nhập', canActivate: [guestGuard], loadComponent: () => import('./pages/auth/login.component').then((m) => m.LoginComponent) },
      { path: 'register', title: 'Đăng ký', canActivate: [guestGuard], loadComponent: () => import('./pages/auth/register.component').then((m) => m.RegisterComponent) },
      { path: 'forgot-password', title: 'Quên mật khẩu', loadComponent: () => import('./pages/auth/forgot-password.component').then((m) => m.ForgotPasswordComponent) },
      { path: 'verify-email', title: 'Xác nhận email', loadComponent: () => import('./pages/auth/verify-email.component').then((m) => m.VerifyEmailComponent) },
      { path: 'reset-password', title: 'Đặt lại mật khẩu', loadComponent: () => import('./pages/auth/reset-password.component').then((m) => m.ResetPasswordComponent) },

      {
        path: 'account',
        canActivate: [authGuard],
        loadChildren: () => import('./pages/account/account.routes').then((m) => m.ACCOUNT_ROUTES),
      },
      { path: '**', title: 'Không tìm thấy trang', loadComponent: () => import('./pages/shop/not-found.component').then((m) => m.NotFoundComponent) },
    ],
  },
];

