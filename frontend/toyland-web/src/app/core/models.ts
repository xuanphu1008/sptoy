// Kieu du lieu TypeScript tuong ung voi DTO cua Spring Boot (backend toyland-api)

export type Role = 'ADMIN' | 'STAFF' | 'CUSTOMER';
export type Gender = 'BOY' | 'GIRL' | 'UNISEX';
export type ProductStatus = 'DRAFT' | 'ACTIVE' | 'INACTIVE' | 'COMING_SOON';
export type OrderStatus =
  | 'PENDING_PAYMENT' | 'PENDING' | 'CONFIRMED' | 'PACKING' | 'SHIPPING'
  | 'DELIVERED' | 'DELIVERY_FAILED' | 'COMPLETED' | 'CANCELLED' | 'RETURN_REQUESTED' | 'RETURNED';
export type PaymentMethod = 'COD' | 'MOMO' | 'PARTNER';
export type PaymentStatus = 'UNPAID' | 'PAID' | 'FAILED' | 'REFUND_PENDING' | 'REFUNDED';
export type ReturnStatus = 'REQUESTED' | 'APPROVED' | 'REJECTED';
export type VoucherType = 'PERCENT' | 'FIXED' | 'FREESHIP';
export type CampaignScope = 'ALL' | 'CATEGORY' | 'BRAND' | 'FRANCHISE' | 'PRODUCT';
export type MovementType = 'INITIAL' | 'IMPORT' | 'SALE' | 'CANCEL' | 'RETURN' | 'ADJUSTMENT';

export interface Page<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface MessageResponse { message: string; }

// ---------------------------------------------------------------- auth
export interface UserSummary {
  id: number; username: string; fullName: string; email: string; phone?: string | null; role: Role;
  /** false = chua bam link xac nhan trong email (bi chan dat hang / danh gia) */
  emailVerified?: boolean;
}
export interface VerifyEmailResponse { alreadyVerified: boolean; email: string; message: string; }
export interface AuthResponse {
  accessToken: string; refreshToken: string; tokenType: string; expiresIn: number; user: UserSummary;
}

// ---------------------------------------------------------------- catalog
export interface CategoryNode {
  id: number; name: string; slug: string; icon?: string | null; imageUrl?: string | null; description?: string | null;
  parentId?: number | null; sortOrder: number; active: boolean; children: CategoryNode[];
}
export interface Brand { id: number; name: string; slug: string; logoUrl?: string | null; country?: string | null; description?: string | null; }
export interface Franchise { id: number; name: string; slug: string; imageUrl?: string | null; description?: string | null; }

export interface ProductCard {
  id: number; name: string; slug: string; imageUrl?: string | null; hoverImageUrl?: string | null; brandName?: string | null;
  ageLabel: string; price: number; originalPrice?: number | null; discountPercent: number; badge?: string | null;
  avgRating: number; reviewCount: number; soldCount: number; inStock: boolean; status: ProductStatus;
  featured: boolean; onlineExclusive: boolean; isNew: boolean; variantCount: number; defaultVariantId?: number | null;
}
export interface Variant {
  id: number; sku: string; option1?: string | null; option2?: string | null; name?: string | null; price: number;
  originalPrice?: number | null; discountPercent: number; stock: number; inStock: boolean; imageUrl?: string | null; weightGram: number;
}
export interface ProductDetail {
  id: number; name: string; slug: string; shortDescription?: string | null; description?: string | null;
  breadcrumbs: { name: string; slug: string }[]; categoryId: number; brand?: Brand | null; franchises: Franchise[];
  ageMinMonths: number; ageMaxMonths?: number | null; ageLabel: string; gender: Gender; material?: string | null;
  brandOrigin?: string | null; madeIn?: string | null; manufactureYear?: number | null; battery?: string | null;
  smallPartsWarning: boolean; safetyWarning?: string | null; crCertified: boolean; importer?: string | null;
  lengthCm?: number; widthCm?: number; heightCm?: number; videoUrl?: string | null;
  option1Name?: string | null; option2Name?: string | null; option1Values: string[]; option2Values: string[];
  variants: Variant[]; images: { url: string; alt?: string | null }[]; specs: { key: string; value: string }[];
  status: ProductStatus; featured: boolean; onlineExclusive: boolean; campaignBadge?: string | null; soldCount: number;
  avgRating: number; reviewCount: number; metaTitle?: string | null; metaDescription?: string | null; createdAt: string;
  related: ProductCard[];
}
export interface FacetItem { value: string; label: string; count: number; }
export interface Facets {
  categories: FacetItem[]; brands: FacetItem[]; franchises: FacetItem[]; ages: FacetItem[]; genders: FacetItem[];
}
export interface Suggestion { products: ProductCard[]; categories: FacetItem[]; brands: FacetItem[]; }
export interface RatingSummary { average: number; total: number; counts: Record<string, number>; withImages: number; }
export interface Review {
  id: number; userName: string; rating: number; comment?: string | null; imageUrls: string[]; variantName?: string | null;
  verifiedPurchase: boolean; adminReply?: string | null; repliedAt?: string | null; createdAt: string;
  /** true: chua tu khoa cam -> dang cho duyet */
  pendingModeration?: boolean;
}
export interface Banner {
  id: number; title: string; subtitle?: string | null; imageUrl?: string | null; linkUrl?: string | null;
  buttonText?: string | null; bgColor?: string | null; sortOrder: number; active: boolean; startAt?: string | null; endAt?: string | null;
}
export interface CampaignInfo { id: number; name: string; badgeLabel?: string | null; discountPercent: number; endAt: string; }
export interface HomeData {
  banners: Banner[]; categories: CategoryNode[]; featured: ProductCard[]; bestSellers: ProductCard[];
  newArrivals: ProductCard[]; onSale: ProductCard[]; campaigns: CampaignInfo[]; franchises: Franchise[]; brands: Brand[];
}

// ---------------------------------------------------------------- cart / checkout
export interface CartLine {
  itemId?: number | null; variantId: number; productId: number; productName: string; productSlug: string;
  variantName?: string | null; imageUrl?: string | null; unitPrice: number; originalPrice?: number | null; quantity: number;
  stock: number; available: boolean; message?: string | null; lineTotal: number; weightGram: number;
}
export interface CartView { lines: CartLine[]; totalQuantity: number; subtotal: number; hasIssues: boolean; }
export interface Quote {
  lines: CartLine[]; subtotal: number; discount: number; shippingFee: number; shippingDiscount: number; giftWrapFee: number;
  total: number; voucherCode?: string | null; voucherName?: string | null; voucherError?: string | null;
  shippingProvider?: string | null; shippingMessage?: string | null; freeShipThreshold?: number | null;
  canPlaceOrder: boolean; issues: string[]; momoEnabled: boolean;
  pointsBalance: number; pointsMax: number; pointsUsed: number; pointsDiscount: number; pointsToEarn: number; pointValue: number;
}
export interface PlaceOrderResponse {
  orderId: number; orderCode: string; totalAmount: number; paymentMethod: PaymentMethod; paymentUrl?: string | null;
}
export interface ShippingConfig { ghnEnabled: boolean; freeShipThreshold: number; fixedFee: number; giftWrapFee: number; }
export interface Option { id: string; name: string; }

// ---------------------------------------------------------------- account
export interface Profile {
  id: number; username: string; fullName: string; email: string; phone?: string | null; role: Role; createdAt: string; consentAt?: string | null;
  emailVerified: boolean;
}
export interface Address {
  id: number; receiverName: string; phone: string; provinceId?: number | null; provinceName: string; districtId?: number | null;
  districtName: string; wardCode?: string | null; wardName: string; detail: string; isDefault: boolean; fullAddress: string;
}
export type AddressRequest = Omit<Address, 'id' | 'fullAddress'>;

// ---------------------------------------------------------------- orders
export interface OrderItem {
  id: number; productId?: number | null; productSlug?: string | null; variantId?: number | null; productName: string;
  variantName?: string | null; sku: string; imageUrl?: string | null; unitPrice: number; originalPrice?: number | null;
  quantity: number; lineTotal: number; reviewed: boolean; canReview: boolean;
}
export interface OrderHistory {
  fromStatus?: OrderStatus | null; toStatus: OrderStatus; label: string; note?: string | null; changedBy?: string | null; createdAt: string;
}
export interface ReturnInfo {
  id: number; reason: string; description?: string | null; imageUrls: string[]; status: ReturnStatus; adminNote?: string | null;
  refundAmount?: number | null; processedBy?: string | null; processedAt?: string | null; createdAt: string;
}
export interface Order {
  id: number; orderCode: string; status: OrderStatus; statusLabel: string; paymentMethod: PaymentMethod; paymentStatus: PaymentStatus;
  receiverName: string; phone: string; fullAddress: string; note?: string | null; giftWrap: boolean; giftMessage?: string | null;
  subtotal: number; discountAmount: number; shippingFee: number; shippingDiscount: number; giftWrapFee: number; totalAmount: number;
  voucherCode?: string | null; pointsUsed: number; pointsDiscount: number; pointsEarned: number;
  cancelReason?: string | null; internalNote?: string | null; createdAt: string; paidAt?: string | null;
  deliveredAt?: string | null; paymentDeadline?: string | null; items: OrderItem[]; history: OrderHistory[];
  returnRequest?: ReturnInfo | null; customer: { id: number; username: string; fullName: string; email: string; phone?: string | null };
  actions: { canCancel: boolean; canPay: boolean; canReturn: boolean; canReorder: boolean; nextStatuses: OrderStatus[] };
}
export interface OrderSummary {
  id: number; orderCode: string; status: OrderStatus; statusLabel: string; paymentMethod: PaymentMethod; paymentStatus: PaymentStatus;
  totalAmount: number; itemCount: number; firstItemName?: string | null; firstItemImage?: string | null; receiverName: string;
  phone: string; customerName: string; createdAt: string;
}
export interface Tracking {
  orderCode: string; status: OrderStatus; statusLabel: string; createdAt: string; totalAmount: number; paymentMethod: PaymentMethod;
  paymentStatus: PaymentStatus; receiverName: string; items: string[]; history: OrderHistory[];
}
export interface MomoResult { validSignature: boolean; success: boolean; orderId?: number | null; orderCode?: string | null; message: string; resultCode: string; }

// ---------------------------------------------------------------- promotion
export interface Voucher {
  id: number; code: string; name: string; description?: string | null; type: VoucherType; value: number; maxDiscount?: number | null;
  minOrderValue: number; startAt: string; endAt: string; usageLimit?: number | null; usedCount: number; perUserLimit: number;
  active: boolean; summary: string;
}
export interface Campaign {
  id: number; name: string; description?: string | null; discountPercent: number; scopeType: CampaignScope; scopeId?: number | null;
  scopeName: string; badgeLabel?: string | null; startAt: string; endAt: string; active: boolean; running: boolean;
}

// ---------------------------------------------------------------- admin
export interface AdminProductRow {
  id: number; name: string; slug: string; imageUrl?: string | null; categoryName: string; brandName?: string | null; minPrice: number;
  maxPrice: number; totalStock: number; variantCount: number; soldCount: number; avgRating: number; status: ProductStatus;
  featured: boolean; updatedAt: string;
}
export interface AdminVariant {
  id?: number | null; sku: string; barcode?: string | null; option1?: string | null; option2?: string | null; price: number;
  compareAtPrice?: number | null; costPrice?: number | null; stock?: number; initialStock?: number | null; weightGram: number;
  imageUrl?: string | null; active: boolean;
}
export interface AdminProduct {
  id?: number; name: string; slug?: string | null; shortDescription?: string | null; description?: string | null; categoryId: number | null;
  brandId?: number | null; franchiseIds: number[]; ageMinMonths: number; ageMaxMonths?: number | null; gender: Gender;
  material?: string | null; brandOrigin?: string | null; madeIn?: string | null; manufactureYear?: number | null; battery?: string | null;
  smallPartsWarning: boolean; safetyWarning?: string | null; crCertified: boolean; importer?: string | null; lengthCm: number;
  widthCm: number; heightCm: number; videoUrl?: string | null; status: ProductStatus; featured: boolean; onlineExclusive: boolean;
  metaTitle?: string | null; metaDescription?: string | null; option1Name?: string | null; option2Name?: string | null;
  specs: { key: string; value: string }[]; images: string[]; variants: AdminVariant[]; createdAt?: string; updatedAt?: string;
}
export interface StockRow {
  variantId: number; productId: number; productName: string; productSlug: string; sku: string; variantName?: string | null;
  imageUrl?: string | null; stock: number; price: number; costPrice?: number | null; lowStock: boolean;
}
export interface Movement {
  id: number; variantId?: number | null; sku: string; type: MovementType; quantityChange: number; balanceAfter: number;
  refCode?: string | null; note?: string | null; createdBy?: string | null; createdAt: string;
}
export interface Receipt {
  id: number; code: string; supplierId?: number | null; supplier?: string | null; note?: string | null; totalQuantity: number; totalCost: number;
  createdBy?: string | null; createdAt: string;
  items: { variantId?: number | null; sku: string; productName: string; quantity: number; unitCost: number }[];
}
export interface AdminReview {
  id: number; productId: number; productName: string; productSlug: string; userName: string; userEmail: string; rating: number;
  comment?: string | null; imageUrls: string[]; hidden: boolean; adminReply?: string | null; createdAt: string;
  flagged: boolean; flagReason?: string | null;
}
export interface AdminUser {
  id: number; username: string; fullName: string; email: string; phone?: string | null; role: Role; enabled: boolean;
  locked: boolean; lockedUntil?: string | null; orderCount: number; createdAt: string;
  points: number; lifetimePoints: number; tier: string; emailVerified: boolean;
}
export interface AdminReturnRow {
  id: number; orderId: number; orderCode: string; customerName: string; reason: string; status: ReturnStatus;
  orderTotal: number; refundAmount?: number | null; createdAt: string;
}
export interface NamedValue { name: string; label: string; count: number; amount?: number | null; }
export interface Dashboard {
  from: string; to: string;
  summary: {
    revenue: number; orders: number; completedOrders: number; cancelledOrders: number; cancelRate: number; avgOrderValue: number;
    newCustomers: number; activeProducts: number; lowStockVariants: number; pendingOrders: number; pendingReturns: number;
  };
  revenueSeries: { date: string; revenue: number; orders: number }[];
  topProducts: { productId?: number | null; name: string; quantity: number; revenue: number }[];
  categoryRevenue: NamedValue[]; statusCounts: NamedValue[]; paymentShare: NamedValue[];
  lowStock: { variantId: number; productId: number; productName: string; variantName?: string | null; sku: string; stock: number }[];
  recentOrders: OrderSummary[];
}

// ---------------------------------------------------------------- nha cung cap
export interface Supplier {
  id: number; code: string; name: string; contactName?: string | null; phone?: string | null; email?: string | null;
  address?: string | null; taxCode?: string | null; note?: string | null; active: boolean; receiptCount: number;
  totalImported: number; lastReceiptAt?: string | null; createdAt: string;
}
export interface SupplierOption { id: number; code: string; name: string; }

// ---------------------------------------------------------------- kiem duyet / nhat ky / thong bao
export interface BannedKeyword { id: number; keyword: string; createdBy?: string | null; createdAt: string; }
export interface AuditLog {
  id: number; actor: string; actorRole?: string | null; action: string; entityType: string; entityId?: string | null;
  summary: string; ip?: string | null; createdAt: string;
}
export interface AppNotification {
  id: number; type: string; title: string; message?: string | null; link?: string | null; read: boolean; createdAt: string;
}

// ---------------------------------------------------------------- diem thuong
export type PointType = 'EARN' | 'REDEEM' | 'REFUND' | 'ADJUST';
export interface Tier { code: string; label: string; minPoints: number; multiplier: number; }
export interface LoyaltySummary {
  points: number; lifetimePoints: number; tier: Tier; nextTier?: Tier | null; pointsToNextTier: number; pointsValue: number;
  pointValue: number; earnPerAmount: number; maxRedeemPercent: number;
}
export interface PointTx {
  id: number; orderId?: number | null; type: PointType; points: number; balanceAfter: number; note?: string | null;
  createdBy?: string | null; createdAt: string;
}

// ---------------------------------------------------------------- bao cao
export type ReportGroupBy = 'DAY' | 'MONTH';
export interface SalesRow {
  period: string; orders: number; itemsSold: number; goodsRevenue: number; discounts: number; netRevenue: number; cost: number;
  grossProfit: number; margin: number;
}
export interface SalesReport { period: { from: string; to: string }; groupBy: ReportGroupBy; rows: SalesRow[]; total: SalesRow; }
export interface ProductReportRow {
  productId?: number | null; productName: string; category?: string | null; brand?: string | null; quantity: number; revenue: number;
  cost: number; profit: number; margin: number;
}
export interface InventoryReportRow {
  variantId: number; sku: string; productName: string; variantName?: string | null; category: string; stock: number; costPrice: number;
  price: number; stockValue: number; retailValue: number; sold30d: number; lowStock: boolean;
}
export interface InventoryReport {
  rows: InventoryReportRow[]; totalUnits: number; totalStockValue: number; totalRetailValue: number; lowStockCount: number;
  outOfStockCount: number;
}
export interface CustomerReportRow {
  userId: number; fullName: string; email: string; phone?: string | null; orders: number; spent: number; lastOrderAt?: string | null;
  points: number; tier: string;
}

// ---------------------------------------------------------------- thanh toan & doi soat
export type PaymentProvider = 'MOMO' | 'COD' | 'CASH' | 'BANK_TRANSFER';
export type PaymentKind = 'PAYMENT' | 'REFUND';
export interface PaymentTxn {
  id: number; orderId: number; orderCode: string; customerName: string; provider: PaymentProvider; kind: PaymentKind; amount: number;
  status: 'PENDING' | 'SUCCESS' | 'FAILED'; bankCode?: string | null; transactionNo?: string | null; responseCode?: string | null;
  txnRef: string; note?: string | null; createdBy?: string | null; createdAt: string;
}
export interface PaymentSummary {
  from: string; to: string; momoIn: number; codIn: number; otherIn: number; totalIn: number; refunded: number; net: number;
  successCount: number; momoFailed: number; momoSuccessRate: number; refundPendingCount: number; refundPendingAmount: number;
  codOutstandingCount: number; codOutstandingAmount: number; daily: { date: string; in: number; out: number }[];
}
export interface RefundQueueRow {
  orderId: number; orderCode: string; customerName: string; phone: string; orderStatus: OrderStatus; paymentMethod: PaymentMethod;
  total: number; paid: number; refunded: number; remaining: number; reason: string; since: string;
}
export interface CodRow { orderId: number; orderCode: string; customerName: string; phone: string; orderStatus: OrderStatus; total: number; createdAt: string; }
export interface OrderLedger {
  orderId: number; orderCode: string; paymentMethod: PaymentMethod; paymentStatus: PaymentStatus; total: number; paid: number;
  refunded: number; refundRemaining: number; canCollect: boolean; canRefund: boolean; transactions: PaymentTxn[];
}

export interface PartnerRow { id: number; name: string; apiKey: string; active: boolean; rateLimitPerMin: number; lastUsedAt: string | null; webhookUrl: string | null; }
export interface PartnerCreated { id: number; name: string; apiKey: string; secret: string; note: string; }

export interface OutboxRow { id: number; eventId: string; type: string; aggregateId: string; partnerId: number | null; status: 'NEW' | 'SENT' | 'DEAD'; attempts: number; createdAt: string; sentAt: string | null; lastError: string | null; }
export interface OutboxSummary { newCount: number; sentCount: number; deadCount: number; recent: OutboxRow[]; }
