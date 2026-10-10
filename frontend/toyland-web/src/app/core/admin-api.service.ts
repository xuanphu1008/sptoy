import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { environment } from '../../environments/environment';
import { toParams } from './api.service';
import {
  AdminProduct, AdminProductRow, AdminReturnRow, AdminReview, AdminUser, AuditLog, Banner, BannedKeyword, Brand, Campaign, CategoryNode,
  CodRow, CustomerReportRow, Dashboard, OrderLedger, PaymentSummary, PaymentTxn, RefundQueueRow, Franchise, InventoryReport, LoyaltySummary, MessageResponse, Movement, Order, OrderStatus, OrderSummary,
  Page, PartnerCreated, PartnerRow, PointTx, ProductReportRow, ProductStatus, Receipt, SalesReport, StockRow, Supplier, SupplierOption, Voucher,
} from './models';

type Q = Record<string, unknown>;

/** API trang quan tri (/api/v1/admin/**) */
@Injectable({ providedIn: 'root' })
export class AdminApi {
  private http = inject(HttpClient);
  private api = `${environment.apiUrl}/admin`;

  dashboard(q: Q) { return this.http.get<Dashboard>(`${this.api}/dashboard`, { params: toParams(q) }); }

  // ---- doi tac (provider API)
  partners() { return this.http.get<PartnerRow[]>(`${this.api}/partners`); }
  createPartner(body: unknown) { return this.http.post<PartnerCreated>(`${this.api}/partners`, body); }
  setPartnerActive(id: number, active: boolean) { return this.http.patch<PartnerRow>(`${this.api}/partners/${id}/active`, { active }); }

  // ---- danh muc / thuong hieu / nhan vat
  categories() { return this.http.get<CategoryNode[]>(`${this.api}/categories`); }
  saveCategory(id: number | null, body: unknown) {
    return id ? this.http.put<CategoryNode>(`${this.api}/categories/${id}`, body) : this.http.post<CategoryNode>(`${this.api}/categories`, body);
  }
  deleteCategory(id: number) { return this.http.delete<MessageResponse>(`${this.api}/categories/${id}`); }
  saveBrand(id: number | null, body: unknown) {
    return id ? this.http.put<Brand>(`${this.api}/brands/${id}`, body) : this.http.post<Brand>(`${this.api}/brands`, body);
  }
  deleteBrand(id: number) { return this.http.delete<MessageResponse>(`${this.api}/brands/${id}`); }
  saveFranchise(id: number | null, body: unknown) {
    return id ? this.http.put<Franchise>(`${this.api}/franchises/${id}`, body) : this.http.post<Franchise>(`${this.api}/franchises`, body);
  }
  deleteFranchise(id: number) { return this.http.delete<MessageResponse>(`${this.api}/franchises/${id}`); }

  // ---- san pham
  products(q: Q) { return this.http.get<Page<AdminProductRow>>(`${this.api}/products`, { params: toParams(q) }); }
  product(id: number) { return this.http.get<AdminProduct>(`${this.api}/products/${id}`); }
  saveProduct(id: number | null | undefined, body: unknown) {
    return id ? this.http.put<AdminProduct>(`${this.api}/products/${id}`, body) : this.http.post<AdminProduct>(`${this.api}/products`, body);
  }
  productStatus(id: number, status: ProductStatus) {
    return this.http.put<MessageResponse>(`${this.api}/products/${id}/status`, {}, { params: toParams({ status }) });
  }
  deleteProduct(id: number) { return this.http.delete<MessageResponse>(`${this.api}/products/${id}`); }

  // ---- don hang
  orders(q: Q) { return this.http.get<Page<OrderSummary>>(`${this.api}/orders`, { params: toParams(q) }); }
  order(id: number) { return this.http.get<Order>(`${this.api}/orders/${id}`); }
  orderStatus(id: number, status: OrderStatus, note?: string | null) { return this.http.put<Order>(`${this.api}/orders/${id}/status`, { status, note }); }
  bulkStatus(ids: number[], status: OrderStatus, note?: string | null) {
    return this.http.post<MessageResponse>(`${this.api}/orders/bulk-status`, { ids, status, note });
  }
  orderNote(id: number, internalNote: string) { return this.http.put<Order>(`${this.api}/orders/${id}/note`, { internalNote }); }
  returns(q: Q) { return this.http.get<Page<AdminReturnRow>>(`${this.api}/returns`, { params: toParams(q) }); }
  approveReturn(id: number, body: { adminNote?: string | null; refundAmount?: number | null }) {
    return this.http.put<Order>(`${this.api}/returns/${id}/approve`, body);
  }
  rejectReturn(id: number, body: { adminNote?: string | null }) { return this.http.put<Order>(`${this.api}/returns/${id}/reject`, body); }

  // ---- kho
  stock(q: Q) { return this.http.get<Page<StockRow>>(`${this.api}/inventory`, { params: toParams(q) }); }
  adjust(body: { variantId: number; newQuantity: number; note?: string | null }) {
    return this.http.post<MessageResponse>(`${this.api}/inventory/adjust`, body);
  }
  movements(q: Q) { return this.http.get<Page<Movement>>(`${this.api}/inventory/movements`, { params: toParams(q) }); }
  receipts(q: Q) { return this.http.get<Page<Receipt>>(`${this.api}/inventory/receipts`, { params: toParams(q) }); }
  receipt(id: number) { return this.http.get<Receipt>(`${this.api}/inventory/receipts/${id}`); }
  createReceipt(body: {
    supplierId?: number | null; supplier?: string | null; note?: string | null; items: { variantId: number; quantity: number; unitCost: number }[];
  }) {
    return this.http.post<Receipt>(`${this.api}/inventory/receipts`, body);
  }

  // ---- marketing
  vouchers() { return this.http.get<Voucher[]>(`${this.api}/vouchers`); }
  saveVoucher(id: number | null, body: unknown) {
    return id ? this.http.put<Voucher>(`${this.api}/vouchers/${id}`, body) : this.http.post<Voucher>(`${this.api}/vouchers`, body);
  }
  deleteVoucher(id: number) { return this.http.delete<MessageResponse>(`${this.api}/vouchers/${id}`); }
  campaigns() { return this.http.get<Campaign[]>(`${this.api}/campaigns`); }
  saveCampaign(id: number | null, body: unknown) {
    return id ? this.http.put<Campaign>(`${this.api}/campaigns/${id}`, body) : this.http.post<Campaign>(`${this.api}/campaigns`, body);
  }
  deleteCampaign(id: number) { return this.http.delete<MessageResponse>(`${this.api}/campaigns/${id}`); }
  banners() { return this.http.get<Banner[]>(`${this.api}/banners`); }
  saveBanner(id: number | null, body: unknown) {
    return id ? this.http.put<Banner>(`${this.api}/banners/${id}`, body) : this.http.post<Banner>(`${this.api}/banners`, body);
  }
  deleteBanner(id: number) { return this.http.delete<MessageResponse>(`${this.api}/banners/${id}`); }

  // ---- danh gia
  reviews(q: Q) { return this.http.get<Page<AdminReview>>(`${this.api}/reviews`, { params: toParams(q) }); }
  reviewVisibility(id: number, hidden: boolean) {
    return this.http.put<AdminReview>(`${this.api}/reviews/${id}/visibility`, {}, { params: toParams({ hidden }) });
  }
  reviewReply(id: number, reply: string) { return this.http.put<AdminReview>(`${this.api}/reviews/${id}/reply`, { reply }); }
  approveReview(id: number) { return this.http.put<AdminReview>(`${this.api}/reviews/${id}/approve`, {}); }
  keywords() { return this.http.get<BannedKeyword[]>(`${this.api}/banned-keywords`); }
  addKeyword(keyword: string) { return this.http.post<BannedKeyword>(`${this.api}/banned-keywords`, { keyword }); }
  deleteKeyword(id: number) { return this.http.delete<MessageResponse>(`${this.api}/banned-keywords/${id}`); }
  rescanKeywords() { return this.http.post<MessageResponse>(`${this.api}/banned-keywords/rescan`, {}); }

  // ---- nha cung cap
  suppliers(q: Q) { return this.http.get<Page<Supplier>>(`${this.api}/suppliers`, { params: toParams(q) }); }
  supplierOptions() { return this.http.get<SupplierOption[]>(`${this.api}/suppliers/options`); }
  supplier(id: number) { return this.http.get<Supplier>(`${this.api}/suppliers/${id}`); }
  saveSupplier(id: number | null, body: unknown) {
    return id ? this.http.put<Supplier>(`${this.api}/suppliers/${id}`, body) : this.http.post<Supplier>(`${this.api}/suppliers`, body);
  }
  deleteSupplier(id: number) { return this.http.delete<MessageResponse>(`${this.api}/suppliers/${id}`); }

  // ---- bao cao (chi ADMIN)
  salesReport(q: Q) { return this.http.get<SalesReport>(`${this.api}/reports/sales`, { params: toParams(q) }); }
  productReport(q: Q) { return this.http.get<ProductReportRow[]>(`${this.api}/reports/products`, { params: toParams(q) }); }
  inventoryReport() { return this.http.get<InventoryReport>(`${this.api}/reports/inventory`); }
  customerReport(q: Q) { return this.http.get<CustomerReportRow[]>(`${this.api}/reports/customers`, { params: toParams(q) }); }
  /** Tai file Excel: tra ve HttpResponse<Blob> de doc ten file tu Content-Disposition */
  exportReport(type: 'sales' | 'products' | 'inventory' | 'customers', q: Q) {
    return this.http.get(`${this.api}/reports/${type}/export`, { params: toParams(q), responseType: 'blob', observe: 'response' });
  }

  // ---- thanh toan & doi soat (ADMIN + STAFF)
  paymentSummary(q: Q) { return this.http.get<PaymentSummary>(`${this.api}/payments/summary`, { params: toParams(q) }); }
  paymentTxns(q: Q) { return this.http.get<Page<PaymentTxn>>(`${this.api}/payments/transactions`, { params: toParams(q) }); }
  exportPaymentTxns(q: Q) {
    return this.http.get(`${this.api}/payments/transactions/export`, { params: toParams(q), responseType: 'blob', observe: 'response' });
  }
  refundQueue() { return this.http.get<RefundQueueRow[]>(`${this.api}/payments/refund-queue`); }
  codOutstanding(q: Q) { return this.http.get<Page<CodRow>>(`${this.api}/payments/cod-outstanding`, { params: toParams(q) }); }
  orderLedger(orderId: number) { return this.http.get<OrderLedger>(`${this.api}/payments/orders/${orderId}`); }
  collectPayment(orderId: number, body: { method: string; reference?: string | null; note?: string | null }) {
    return this.http.post<OrderLedger>(`${this.api}/payments/orders/${orderId}/collect`, body);
  }
  recordRefund(orderId: number, body: { amount: number; method: string; reference?: string | null; note?: string | null }) {
    return this.http.post<OrderLedger>(`${this.api}/payments/orders/${orderId}/refund`, body);
  }

  // ---- nhat ky hoat dong (chi ADMIN)
  auditLogs(q: Q) { return this.http.get<Page<AuditLog>>(`${this.api}/audit-logs`, { params: toParams(q) }); }

  // ---- nguoi dung
  users(q: Q) { return this.http.get<Page<AdminUser>>(`${this.api}/users`, { params: toParams(q) }); }
  saveUser(id: number | null, body: unknown) {
    return id ? this.http.put<AdminUser>(`${this.api}/users/${id}`, body) : this.http.post<AdminUser>(`${this.api}/users`, body);
  }
  unlockUser(id: number) { return this.http.put<MessageResponse>(`${this.api}/users/${id}/unlock`, {}); }
  deleteUser(id: number) { return this.http.delete<MessageResponse>(`${this.api}/users/${id}`); }
  userPoints(id: number) { return this.http.get<LoyaltySummary>(`${this.api}/users/${id}/points`); }
  userPointHistory(id: number, q: Q) { return this.http.get<Page<PointTx>>(`${this.api}/users/${id}/points/history`, { params: toParams(q) }); }
  adjustPoints(id: number, delta: number, note?: string | null) {
    return this.http.post<LoyaltySummary>(`${this.api}/users/${id}/points`, { delta, note });
  }
}
