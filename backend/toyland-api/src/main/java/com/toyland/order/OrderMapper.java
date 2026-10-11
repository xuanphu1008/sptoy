package com.toyland.order;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.toyland.order.dto.OrderDtos.CustomerInfo;
import com.toyland.order.dto.OrderDtos.HistoryDto;
import com.toyland.order.dto.OrderDtos.OrderActions;
import com.toyland.order.dto.OrderDtos.OrderDto;
import com.toyland.order.dto.OrderDtos.OrderItemDto;
import com.toyland.order.dto.OrderDtos.OrderSummary;
import com.toyland.order.dto.OrderDtos.ReturnDto;
import com.toyland.user.User;

@Component
public class OrderMapper {

	@Value("${app.order.return-days}")
	private int returnDays;

	public boolean canReturn(Order o, ReturnRequest rr) {
		return o.getStatus() == OrderStatus.DELIVERED && rr == null && o.getDeliveredAt() != null
				&& o.getDeliveredAt().isAfter(LocalDateTime.now().minusDays(returnDays));
	}

	public OrderDto toDto(Order o, ReturnRequest rr, boolean forAdmin) {
		boolean reviewable = o.getStatus() == OrderStatus.DELIVERED || o.getStatus() == OrderStatus.COMPLETED;
		List<OrderItemDto> items = o.getItems().stream().map(i -> new OrderItemDto(i.getId(),
				i.getProduct() == null ? null : i.getProduct().getId(),
				i.getProduct() == null ? null : i.getProduct().getSlug(),
				i.getVariant() == null ? null : i.getVariant().getId(), i.getProductName(), i.getVariantName(), i.getSku(),
				i.getImageUrl(), i.getUnitPrice(), i.getOriginalPrice(), i.getQuantity(), i.getLineTotal(), i.isReviewed(),
				reviewable && !i.isReviewed() && i.getProduct() != null)).toList();
		List<HistoryDto> history = o.getHistory().stream().map(OrderMapper::history).toList();
		User u = o.getUser();
		boolean canPay = o.getStatus() == OrderStatus.PENDING_PAYMENT && o.getPaymentMethod() == PaymentMethod.MOMO
				&& (o.getPaymentDeadline() == null || o.getPaymentDeadline().isAfter(LocalDateTime.now()));
		OrderActions actions = new OrderActions(o.getStatus().customerCancellable(), canPay, canReturn(o, rr),
				o.getStatus().isFinal() || o.getStatus() == OrderStatus.DELIVERED,
				forAdmin ? List.copyOf(o.getStatus().nextForStaff()) : List.of());
		return new OrderDto(o.getId(), o.getOrderCode(), o.getStatus(), o.getStatus().getLabel(), o.getPaymentMethod(),
				o.getPaymentStatus(), o.getReceiverName(), o.getPhone(), o.getFullAddress(), o.getNote(), o.isGiftWrap(),
				o.getGiftMessage(), o.getSubtotal(), o.getDiscountAmount(), o.getShippingFee(), o.getShippingDiscount(),
				o.getGiftWrapFee(), o.getTotalAmount(), o.getVoucherCode(), o.getPointsUsed(), o.getPointsDiscount(),
				o.getPointsEarned(), o.getCancelReason(),
				forAdmin ? o.getInternalNote() : null, o.getCreatedAt(), o.getPaidAt(), o.getDeliveredAt(),
				o.getPaymentDeadline(), items, history, rr == null ? null : returnDto(rr),
				u == null ? new CustomerInfo(null, "partner", o.getReceiverName(), null, o.getPhone())
				: new CustomerInfo(u.getId(), u.getUsername(), u.getFullName(), u.getEmail(), u.getPhone()), actions);
	}

	public static HistoryDto history(OrderStatusHistory h) {
		return new HistoryDto(h.getFromStatus(), h.getToStatus(), h.getToStatus().getLabel(), h.getNote(), h.getChangedBy(),
				h.getCreatedAt());
	}

	public static ReturnDto returnDto(ReturnRequest r) {
		return new ReturnDto(r.getId(), r.getReason(), r.getDescription(), r.getImageUrls(), r.getStatus(), r.getAdminNote(),
				r.getRefundAmount(), r.getProcessedBy(), r.getProcessedAt(), r.getCreatedAt());
	}

	public static OrderSummary summary(Order o) {
		OrderItem first = o.getItems().isEmpty() ? null : o.getItems().get(0);
		int count = o.getItems().stream().mapToInt(OrderItem::getQuantity).sum();
		return new OrderSummary(o.getId(), o.getOrderCode(), o.getStatus(), o.getStatus().getLabel(), o.getPaymentMethod(),
				o.getPaymentStatus(), o.getTotalAmount(), count, first == null ? null : first.getProductName(),
				first == null ? null : first.getImageUrl(), o.getReceiverName(), o.getPhone(), o.getUser() == null ? o.getReceiverName() : o.getUser().getFullName(),
				o.getCreatedAt());
	}
}
