package com.toyland.order;

import java.util.EnumSet;
import java.util.Set;

/**
 * Trang thai don hang + luong chuyen trang thai hop le (tham chieu Shopee).
 */
public enum OrderStatus {
	PENDING_PAYMENT("Chờ thanh toán"),
	PENDING("Chờ xác nhận"),
	CONFIRMED("Đã xác nhận"),
	PACKING("Đang đóng gói"),
	SHIPPING("Đang giao hàng"),
	DELIVERED("Đã giao hàng"),
	DELIVERY_FAILED("Giao không thành công"),
	COMPLETED("Hoàn tất"),
	CANCELLED("Đã huỷ"),
	RETURN_REQUESTED("Yêu cầu trả hàng"),
	RETURNED("Đã trả hàng");

	private final String label;

	OrderStatus(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

	/** Cac trang thai ke tiep ma nhan vien/quan tri duoc phep chuyen thu cong. */
	public Set<OrderStatus> nextForStaff() {
		return switch (this) {
		case PENDING_PAYMENT -> EnumSet.of(CANCELLED);
		case PENDING -> EnumSet.of(CONFIRMED, CANCELLED);
		case CONFIRMED -> EnumSet.of(PACKING, CANCELLED);
		case PACKING -> EnumSet.of(SHIPPING, CANCELLED);
		case SHIPPING -> EnumSet.of(DELIVERED, DELIVERY_FAILED);
		case DELIVERY_FAILED -> EnumSet.of(SHIPPING, CANCELLED);
		case DELIVERED -> EnumSet.of(COMPLETED);
		default -> EnumSet.noneOf(OrderStatus.class);
		};
	}

	public boolean canTransitionTo(OrderStatus next) {
		return nextForStaff().contains(next);
	}

	/** Khach hang duoc tu huy khi don chua duoc xac nhan. */
	public boolean customerCancellable() {
		return this == PENDING_PAYMENT || this == PENDING;
	}

	/** Don da tru ton kho va chua ket thuc -> huy thi phai hoan kho. */
	public boolean isFinal() {
		return this == COMPLETED || this == CANCELLED || this == RETURNED;
	}
}
