package com.toyland.order;

public enum PaymentStatus {
	/** REFUND_PENDING: da thu tien nhung don bi huy / tra hang -> cho ke toan chuyen tra lai khach */
	UNPAID, PAID, FAILED, REFUND_PENDING, REFUNDED
}
