package com.toyland.order.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.toyland.cart.dto.CartDtos.CartLine;
import com.toyland.order.OrderStatus;
import com.toyland.order.PaymentMethod;
import com.toyland.order.PaymentStatus;
import com.toyland.order.ReturnStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class OrderDtos {

	private OrderDtos() {
	}

	// ---------------------------------------------------------------- checkout
	/** usePoints: dung toi da diem thuong duoc phep cho don nay. */
	public record QuoteRequest(Long addressId, String voucherCode, boolean giftWrap, boolean usePoints) {
	}

	public record Quote(List<CartLine> lines, BigDecimal subtotal, BigDecimal discount, BigDecimal shippingFee,
			BigDecimal shippingDiscount, BigDecimal giftWrapFee, BigDecimal total, String voucherCode, String voucherName,
			String voucherError, String shippingProvider, String shippingMessage, BigDecimal freeShipThreshold,
			boolean canPlaceOrder, List<String> issues, boolean momoEnabled,
			// diem thuong: so du, toi da duoc dung, dang dung, so tien tru, diem se nhan khi don hoan tat, gia tri 1 diem
			int pointsBalance, int pointsMax, int pointsUsed, BigDecimal pointsDiscount, int pointsToEarn, int pointValue) {
	}

	public record PlaceOrderRequest(@NotNull(message = "Vui lòng chọn địa chỉ giao hàng") Long addressId,
			String voucherCode, @NotNull(message = "Chọn phương thức thanh toán") PaymentMethod paymentMethod,
			@Size(max = 500) String note, boolean giftWrap, @Size(max = 300) String giftMessage, boolean usePoints) {
	}

	public record PlaceOrderResponse(Long orderId, String orderCode, BigDecimal totalAmount, PaymentMethod paymentMethod,
			String paymentUrl) {
	}

	// ---------------------------------------------------------------- don hang
	public record OrderItemDto(Long id, Long productId, String productSlug, Long variantId, String productName,
			String variantName, String sku, String imageUrl, BigDecimal unitPrice, BigDecimal originalPrice, int quantity,
			BigDecimal lineTotal, boolean reviewed, boolean canReview) {
	}

	public record HistoryDto(OrderStatus fromStatus, OrderStatus toStatus, String label, String note, String changedBy,
			LocalDateTime createdAt) {
	}

	public record ReturnDto(Long id, String reason, String description, List<String> imageUrls, ReturnStatus status,
			String adminNote, BigDecimal refundAmount, String processedBy, LocalDateTime processedAt, LocalDateTime createdAt) {
	}

	public record CustomerInfo(Long id, String username, String fullName, String email, String phone) {
	}

	public record OrderActions(boolean canCancel, boolean canPay, boolean canReturn, boolean canReorder,
			List<OrderStatus> nextStatuses) {
	}

	public record OrderDto(Long id, String orderCode, OrderStatus status, String statusLabel, PaymentMethod paymentMethod,
			PaymentStatus paymentStatus, String receiverName, String phone, String fullAddress, String note,
			boolean giftWrap, String giftMessage, BigDecimal subtotal, BigDecimal discountAmount, BigDecimal shippingFee,
			BigDecimal shippingDiscount, BigDecimal giftWrapFee, BigDecimal totalAmount, String voucherCode,
			int pointsUsed, BigDecimal pointsDiscount, int pointsEarned, String cancelReason, String internalNote, LocalDateTime createdAt, LocalDateTime paidAt,
			LocalDateTime deliveredAt, LocalDateTime paymentDeadline, List<OrderItemDto> items, List<HistoryDto> history,
			ReturnDto returnRequest, CustomerInfo customer, OrderActions actions) {
	}

	public record OrderSummary(Long id, String orderCode, OrderStatus status, String statusLabel,
			PaymentMethod paymentMethod, PaymentStatus paymentStatus, BigDecimal totalAmount, int itemCount,
			String firstItemName, String firstItemImage, String receiverName, String phone, String customerName,
			LocalDateTime createdAt) {
	}

	public record CancelRequest(@NotBlank(message = "Vui lòng chọn lý do huỷ") @Size(max = 300) String reason) {
	}

	public record ReturnCreateRequest(@NotBlank(message = "Vui lòng chọn lý do") @Size(max = 200) String reason,
			@Size(max = 1000) String description, List<String> imageUrls) {
	}

	public record StatusUpdateRequest(@NotNull OrderStatus status, @Size(max = 500) String note) {
	}

	public record BulkStatusRequest(@NotNull List<Long> ids, @NotNull OrderStatus status, @Size(max = 500) String note) {
	}

	public record InternalNoteRequest(@Size(max = 500) String internalNote) {
	}

	public record ReturnDecisionRequest(@Size(max = 500) String adminNote, BigDecimal refundAmount) {
	}

	public record AdminReturnRow(Long id, Long orderId, String orderCode, String customerName, String reason,
			ReturnStatus status, BigDecimal orderTotal, BigDecimal refundAmount, LocalDateTime createdAt) {
	}

	public record TrackingDto(String orderCode, OrderStatus status, String statusLabel, LocalDateTime createdAt,
			BigDecimal totalAmount, PaymentMethod paymentMethod, PaymentStatus paymentStatus, String receiverName,
			List<String> items, List<HistoryDto> history) {
	}
}
