import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { environment } from '../../environments/environment';
import {
  Address, AddressRequest, AppNotification, Banner, LoyaltySummary, PointTx, Brand, CartView, CategoryNode, Facets, Franchise, HomeData, MessageResponse, Option, Order,
  OrderSummary, Page, PlaceOrderResponse, ProductCard, ProductDetail, Profile, Quote, RatingSummary, Review, ShippingConfig,
  MomoResult, Suggestion, Tracking, Voucher,
} from './models';

/** Chuyen object -> HttpParams, bo qua gia tri rong */
export function toParams(obj: Record<string, unknown> = {}): HttpParams {
  let p = new HttpParams();
  for (const [k, v] of Object.entries(obj)) {
    if (v === null || v === undefined || v === '') continue;
    if (Array.isArray(v)) v.forEach((x) => (p = p.append(k, String(x))));
    else p = p.set(k, String(v));
  }
  return p;
}

export const SILENT = { headers: new HttpHeaders({ 'X-Silent': '1' }) };

/** API phia khach hang (cua hang) */
@Injectable({ providedIn: 'root' })
export class ShopApi {
  private http = inject(HttpClient);
  private api = environment.apiUrl;

  // ---- catalog
  home() { return this.http.get<HomeData>(`${this.api}/home`); }
  categories() { return this.http.get<CategoryNode[]>(`${this.api}/categories`); }
  brands() { return this.http.get<Brand[]>(`${this.api}/brands`); }
  franchises() { return this.http.get<Franchise[]>(`${this.api}/franchises`); }
  products(q: Record<string, unknown>) { return this.http.get<Page<ProductCard>>(`${this.api}/products`, { params: toParams(q) }); }
  facets(q: Record<string, unknown>) { return this.http.get<Facets>(`${this.api}/products/facets`, { params: toParams(q), ...SILENT }); }
  suggest(q: string) { return this.http.get<Suggestion>(`${this.api}/products/suggest`, { params: toParams({ q }), ...SILENT }); }
  byIds(ids: number[]) { return this.http.get<ProductCard[]>(`${this.api}/products/by-ids`, { params: toParams({ ids: ids.join(',') }), ...SILENT }); }
  productBySlug(slug: string) { return this.http.get<ProductDetail>(`${this.api}/products/slug/${slug}`); }
  ratingSummary(id: number) { return this.http.get<RatingSummary>(`${this.api}/products/${id}/rating-summary`, SILENT); }
  reviews(id: number, q: Record<string, unknown>) {
    return this.http.get<Page<Review>>(`${this.api}/products/${id}/reviews`, { params: toParams(q), ...SILENT });
  }
  banners() { return this.http.get<Banner[]>(`${this.api}/banners`); }
  availableVouchers() { return this.http.get<Voucher[]>(`${this.api}/vouchers/available`, SILENT); }

  // ---- shipping (GHN)
  shippingConfig() { return this.http.get<ShippingConfig>(`${this.api}/shipping/config`, SILENT); }
  provinces() { return this.http.get<Option[]>(`${this.api}/shipping/provinces`, SILENT); }
  districts(provinceId: number) { return this.http.get<Option[]>(`${this.api}/shipping/districts`, { params: toParams({ provinceId }), ...SILENT }); }
  wards(districtId: number) { return this.http.get<Option[]>(`${this.api}/shipping/wards`, { params: toParams({ districtId }), ...SILENT }); }

  // ---- account
  me() { return this.http.get<Profile>(`${this.api}/me`); }
  updateMe(body: { fullName: string; email: string; phone?: string | null }) { return this.http.put<Profile>(`${this.api}/me`, body); }
  changePassword(body: { currentPassword: string; newPassword: string }) { return this.http.put<MessageResponse>(`${this.api}/me/password`, body); }
  deleteAccount(password: string) { return this.http.post<MessageResponse>(`${this.api}/me/delete`, { password }); }
  addresses() { return this.http.get<Address[]>(`${this.api}/me/addresses`); }
  addAddress(a: AddressRequest) { return this.http.post<Address>(`${this.api}/me/addresses`, a); }
  updateAddress(id: number, a: AddressRequest) { return this.http.put<Address>(`${this.api}/me/addresses/${id}`, a); }
  setDefaultAddress(id: number) { return this.http.put<MessageResponse>(`${this.api}/me/addresses/${id}/default`, {}); }
  deleteAddress(id: number) { return this.http.delete<MessageResponse>(`${this.api}/me/addresses/${id}`); }
  wishlist() { return this.http.get<ProductCard[]>(`${this.api}/me/wishlist`); }

  // ---- checkout & orders
  quote(body: { addressId: number | null; voucherCode?: string | null; giftWrap: boolean; usePoints: boolean }) {
    return this.http.post<Quote>(`${this.api}/checkout/quote`, body, SILENT);
  }
  placeOrder(body: {
    addressId: number; voucherCode?: string | null; paymentMethod: string; note?: string | null; giftWrap: boolean; giftMessage?: string | null;
    usePoints: boolean;
  }) { return this.http.post<PlaceOrderResponse>(`${this.api}/orders`, body); }
  myOrders(q: Record<string, unknown>) { return this.http.get<Page<OrderSummary>>(`${this.api}/orders`, { params: toParams(q) }); }
  order(id: number) { return this.http.get<Order>(`${this.api}/orders/${id}`); }
  cancelOrder(id: number, reason: string) { return this.http.post<Order>(`${this.api}/orders/${id}/cancel`, { reason }); }
  payOrder(id: number) { return this.http.post<{ paymentUrl: string }>(`${this.api}/orders/${id}/pay`, {}); }
  returnOrder(id: number, body: { reason: string; description?: string | null; imageUrls: string[] }) {
    return this.http.post<Order>(`${this.api}/orders/${id}/return`, body);
  }
  reorder(id: number) { return this.http.post<CartView>(`${this.api}/orders/${id}/reorder`, {}); }
  review(body: { orderItemId: number; rating: number; comment?: string | null; imageUrls: string[] }) {
    return this.http.post<Review>(`${this.api}/reviews`, body);
  }
  track(code: string, phone: string) { return this.http.get<Tracking>(`${this.api}/orders/track`, { params: toParams({ code, phone }) }); }
  momoReturn(params: Record<string, string>) { return this.http.get<MomoResult>(`${this.api}/payments/momo/return`, { params: toParams(params) }); }

  // ---- diem thuong
  myPoints() { return this.http.get<LoyaltySummary>(`${this.api}/account/points`); }
  myPointHistory(q: Record<string, unknown>) { return this.http.get<Page<PointTx>>(`${this.api}/account/points/history`, { params: toParams(q) }); }

  // ---- thong bao (khach hang lan nhan vien)
  notifications(q: Record<string, unknown>) {
    return this.http.get<Page<AppNotification>>(`${this.api}/notifications`, { params: toParams(q), ...SILENT });
  }
  unreadCount() { return this.http.get<{ unread: number }>(`${this.api}/notifications/unread-count`, SILENT); }
  readNotification(id: number) { return this.http.put<AppNotification>(`${this.api}/notifications/${id}/read`, {}, SILENT); }
  readAllNotifications() { return this.http.put<MessageResponse>(`${this.api}/notifications/read-all`, {}, SILENT); }

  upload(file: File, folder: string) {
    const fd = new FormData();
    fd.append('file', file);
    fd.append('folder', folder);
    return this.http.post<{ url: string }>(`${this.api}/uploads`, fd);
  }
}
