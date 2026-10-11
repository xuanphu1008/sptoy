package com.toyland.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Giao dien cong bo cua mien Don hang cho doi tac ben thu ba (qua /api/partner/v1/orders).
 * Mien Doi tac chi goi giao dien nay, khong dung Repository hay entity cua mien Don hang.
 */
public interface PartnerOrderApi {

	record Line(String sku, int quantity) {
	}

	record Command(String externalRef, String receiverName, String phone, String province, String district, String ward,
			String addressDetail, String note, List<Line> items) {
	}

	record LineView(String sku, String productName, String variantName, BigDecimal unitPrice, int quantity, BigDecimal lineTotal) {
	}

	record PartnerOrder(String orderCode, String externalRef, String status, String statusLabel, String paymentStatus,
			BigDecimal subtotal, BigDecimal shippingFee, BigDecimal total, LocalDateTime createdAt, List<LineView> items) {
	}

	/** Tao don, tru kho trong cung mot giao dich. Loi o buoc nao thi hoan tac het. */
	PartnerOrder create(Long partnerId, String partnerName, Command c);

	Optional<PartnerOrder> find(Long partnerId, String orderCode);

	/** Huy don cua chinh doi tac (chi khi chua xac nhan), hoan lai kho. */
	PartnerOrder cancel(Long partnerId, String partnerName, String orderCode, String reason);
}
