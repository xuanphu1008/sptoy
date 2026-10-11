import { CampaignScope, Gender, MovementType, OrderStatus, PaymentMethod, PaymentStatus, ProductStatus, ReturnStatus, Role, VoucherType, PointType } from './models';

export const ORDER_STATUS: Record<OrderStatus, { label: string; icon: string }> = {
  PENDING_PAYMENT: { label: 'Chờ thanh toán', icon: 'hourglass_top' },
  PENDING: { label: 'Chờ xác nhận', icon: 'pending_actions' },
  CONFIRMED: { label: 'Đã xác nhận', icon: 'task_alt' },
  PACKING: { label: 'Đang đóng gói', icon: 'inventory_2' },
  SHIPPING: { label: 'Đang giao hàng', icon: 'local_shipping' },
  DELIVERED: { label: 'Đã giao hàng', icon: 'home' },
  DELIVERY_FAILED: { label: 'Giao không thành công', icon: 'report' },
  COMPLETED: { label: 'Hoàn tất', icon: 'verified' },
  CANCELLED: { label: 'Đã huỷ', icon: 'cancel' },
  RETURN_REQUESTED: { label: 'Yêu cầu trả hàng', icon: 'assignment_return' },
  RETURNED: { label: 'Đã trả hàng', icon: 'keyboard_return' },
};
export const ORDER_STATUSES = Object.keys(ORDER_STATUS) as OrderStatus[];

/** Cac moc hien thi tren thanh tien trinh don hang (giong Shopee) */
export const ORDER_STEPS: OrderStatus[] = ['PENDING', 'CONFIRMED', 'PACKING', 'SHIPPING', 'DELIVERED', 'COMPLETED'];

export const PAYMENT_METHOD: Record<PaymentMethod, string> = {
  COD: 'Thanh toán khi nhận hàng (COD)',
  MOMO: 'Thanh toán qua ví MoMo',
  PARTNER: 'Đơn đối tác (đối soát ngoài hệ thống)',
};
export const PAYMENT_STATUS: Record<PaymentStatus, string> = {
  UNPAID: 'Chưa thanh toán', PAID: 'Đã thanh toán', FAILED: 'Thanh toán lỗi', REFUND_PENDING: 'Chờ hoàn tiền', REFUNDED: 'Đã hoàn tiền',
};
export const PRODUCT_STATUS: Record<ProductStatus, string> = {
  DRAFT: 'Nháp', ACTIVE: 'Đang bán', INACTIVE: 'Ngừng bán', COMING_SOON: 'Sắp về',
};
export const RETURN_STATUS: Record<ReturnStatus, string> = { REQUESTED: 'Chờ duyệt', APPROVED: 'Đã chấp nhận', REJECTED: 'Từ chối' };
export const GENDER: Record<Gender, string> = { BOY: 'Bé trai', GIRL: 'Bé gái', UNISEX: 'Cả bé trai & bé gái' };
export const ROLE: Record<Role, string> = { ADMIN: 'Quản trị viên', STAFF: 'Nhân viên', CUSTOMER: 'Khách hàng' };
export const VOUCHER_TYPE: Record<VoucherType, string> = { PERCENT: 'Giảm %', FIXED: 'Giảm tiền', FREESHIP: 'Miễn phí ship' };
export const CAMPAIGN_SCOPE: Record<CampaignScope, string> = {
  ALL: 'Toàn bộ sản phẩm', CATEGORY: 'Danh mục', BRAND: 'Thương hiệu', FRANCHISE: 'Nhân vật / chủ đề', PRODUCT: 'Sản phẩm',
};
export const MOVEMENT_TYPE: Record<MovementType, string> = {
  INITIAL: 'Tồn ban đầu', IMPORT: 'Nhập kho', SALE: 'Xuất bán', CANCEL: 'Hoàn do huỷ', RETURN: 'Nhập hàng trả', ADJUSTMENT: 'Điều chỉnh',
};
export const AGE_GROUPS = [
  { value: 'BABY', label: '0-12 tháng', icon: 'child_friendly' },
  { value: 'TODDLER', label: '1-3 tuổi', icon: 'toys' },
  { value: 'PRESCHOOL', label: '3-6 tuổi', icon: 'extension' },
  { value: 'SCHOOL', label: '6-12 tuổi', icon: 'school' },
  { value: 'TEEN', label: '12+ tuổi', icon: 'sports_esports' },
];
export const CANCEL_REASONS = [
  'Muốn thay đổi địa chỉ giao hàng', 'Muốn thay đổi sản phẩm (màu, kích thước...)', 'Tìm thấy giá rẻ hơn ở chỗ khác',
  'Đổi ý, không muốn mua nữa', 'Thủ tục thanh toán rắc rối', 'Lý do khác',
];
export const RETURN_REASONS = [
  'Sản phẩm bị lỗi / hư hỏng', 'Giao sai sản phẩm / sai mẫu', 'Thiếu phụ kiện / chi tiết', 'Sản phẩm khác mô tả', 'Lý do khác',
];
export const PLACEHOLDER_IMG = 'https://placehold.co/600x600/FFF1BF/2B2350/png?text=ToyLand';

/** 6 mau pastel cua he thong (xoay vong cho danh muc, do tuoi, nhan vat...) */
export const PASTELS = ['var(--p-pink)', 'var(--p-sky)', 'var(--p-butter)', 'var(--p-mint)', 'var(--p-lilac)', 'var(--p-peach)'];

export const POINT_TYPE: Record<PointType, string> = { EARN: 'Tích điểm', REDEEM: 'Dùng điểm', REFUND: 'Hoàn điểm', ADJUST: 'Điều chỉnh' };

/** Nhan tieng Viet cho nhat ky hoat dong */
export const AUDIT_ACTION: Record<string, string> = {
  CREATE: 'Tạo mới', UPDATE: 'Cập nhật', DELETE: 'Xoá', STATUS: 'Đổi trạng thái', ADJUST: 'Điều chỉnh', LOGIN: 'Đăng nhập',
  HIDE: 'Ẩn', SHOW: 'Hiện', REPLY: 'Phản hồi', MODERATE: 'Kiểm duyệt', APPROVE: 'Duyệt', REJECT: 'Từ chối', NOTE: 'Ghi chú',
  UNLOCK: 'Mở khoá', DISABLE: 'Tắt', EXPORT: 'Xuất file', PAYMENT: 'Ghi nhận thu tiền', REFUND: 'Hoàn tiền',
};
export const AUDIT_ENTITY: Record<string, string> = {
  PRODUCT: 'Sản phẩm', CATEGORY: 'Danh mục', BRAND: 'Thương hiệu', FRANCHISE: 'Nhân vật', ORDER: 'Đơn hàng', RETURN: 'Trả hàng',
  INVENTORY: 'Tồn kho', STOCK_RECEIPT: 'Phiếu nhập', SUPPLIER: 'Nhà cung cấp', VOUCHER: 'Mã giảm giá', CAMPAIGN: 'Chương trình KM',
  BANNER: 'Banner', USER: 'Tài khoản', REVIEW: 'Đánh giá', BANNED_KEYWORD: 'Từ khoá cấm', POINTS: 'Điểm thưởng', REPORT: 'Báo cáo', PAYMENT: 'Thanh toán',
};

/** Hinh thuc / cong thanh toan trong so thu - chi */
export const PAYMENT_PROVIDER: Record<string, string> = {
  MOMO: 'Ví MoMo', COD: 'Thu hộ COD', CASH: 'Tiền mặt', BANK_TRANSFER: 'Chuyển khoản',
};
export const TXN_STATUS: Record<string, string> = { PENDING: 'Đang chờ', SUCCESS: 'Thành công', FAILED: 'Thất bại' };
