import { Routes } from '@angular/router';
import { adminGuard } from '../../core/guards';


export const ADMIN_ROUTES: Routes = [
  { path: '', title: 'Tổng quan - Admin', loadComponent: () => import('./dashboard.component').then((m) => m.DashboardComponent) },
  { path: 'orders', title: 'Đơn hàng - Admin', loadComponent: () => import('./orders.component').then((m) => m.AdminOrdersComponent) },
  { path: 'orders/:id', title: 'Chi tiết đơn - Admin', loadComponent: () => import('./order-detail.component').then((m) => m.AdminOrderDetailComponent) },
  { path: 'returns', title: 'Trả hàng - Admin', loadComponent: () => import('./returns.component').then((m) => m.AdminReturnsComponent) },
  { path: 'reviews', title: 'Đánh giá - Admin', loadComponent: () => import('./reviews.component').then((m) => m.AdminReviewsComponent) },
  { path: 'products', title: 'Sản phẩm - Admin', loadComponent: () => import('./products.component').then((m) => m.AdminProductsComponent) },
  { path: 'products/new', title: 'Thêm sản phẩm - Admin', loadComponent: () => import('./product-edit.component').then((m) => m.ProductEditComponent) },
  { path: 'products/:id', title: 'Sửa sản phẩm - Admin', loadComponent: () => import('./product-edit.component').then((m) => m.ProductEditComponent) },
  { path: 'inventory', title: 'Kho hàng - Admin', loadComponent: () => import('./inventory.component').then((m) => m.InventoryComponent) },
  { path: 'inventory/receipts/new', title: 'Phiếu nhập kho - Admin', loadComponent: () => import('./receipt-new.component').then((m) => m.ReceiptNewComponent) },
  { path: 'categories', title: 'Danh mục - Admin', canActivate: [adminGuard], loadComponent: () => import('./categories.component').then((m) => m.CategoriesComponent) },
  { path: 'brands', title: 'Thương hiệu - Admin', canActivate: [adminGuard], loadComponent: () => import('./brands.component').then((m) => m.BrandsComponent) },
  { path: 'vouchers', title: 'Mã giảm giá - Admin', canActivate: [adminGuard], loadComponent: () => import('./vouchers.component').then((m) => m.VouchersComponent) },
  { path: 'campaigns', title: 'Khuyến mãi - Admin', canActivate: [adminGuard], loadComponent: () => import('./campaigns.component').then((m) => m.CampaignsComponent) },
  { path: 'banners', title: 'Banner - Admin', canActivate: [adminGuard], loadComponent: () => import('./banners.component').then((m) => m.BannersComponent) },
  { path: 'payments', title: 'Thanh toán & đối soát - Admin', loadComponent: () => import('./payments.component').then((m) => m.PaymentsComponent) },
  { path: 'suppliers', title: 'Nhà cung cấp - Admin', loadComponent: () => import('./suppliers.component').then((m) => m.SuppliersComponent) },
  { path: 'reports', title: 'Báo cáo - Admin', canActivate: [adminGuard], loadComponent: () => import('./reports.component').then((m) => m.ReportsComponent) },
  { path: 'audit-logs', title: 'Nhật ký hoạt động - Admin', canActivate: [adminGuard], loadComponent: () => import('./audit-logs.component').then((m) => m.AuditLogsComponent) },
  { path: 'partners', title: 'Đối tác & API - Admin', canActivate: [adminGuard], loadComponent: () => import('./partners.component').then((m) => m.PartnersComponent) },
  { path: 'users', title: 'Người dùng - Admin', canActivate: [adminGuard], loadComponent: () => import('./users.component').then((m) => m.UsersComponent) },
];
