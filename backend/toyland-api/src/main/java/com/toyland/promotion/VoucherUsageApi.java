package com.toyland.promotion;

import java.math.BigDecimal;

import com.toyland.order.Order;
import com.toyland.user.User;

/**
 * Giao dien cong bo cua mien Khuyen mai cho mien Don hang: ghi nhan va hoan lai luot dung ma giam gia.
 * Mien don hang khong thao tac truc tiep bang voucher_usages.
 */
public interface VoucherUsageApi {

	void recordUsage(Voucher voucher, User user, Order order, BigDecimal discountAmount);

	/** Huy luot dung cua don (khi don bi huy): tra lai 1 luot cho ma giam gia. */
	void releaseUsageOfOrder(Long orderId);
}
