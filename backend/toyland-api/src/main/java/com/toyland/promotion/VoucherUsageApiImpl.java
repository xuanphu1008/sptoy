package com.toyland.promotion;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.toyland.order.Order;
import com.toyland.user.User;

@Service
class VoucherUsageApiImpl implements VoucherUsageApi {

	private final VoucherUsageRepository repo;

	VoucherUsageApiImpl(VoucherUsageRepository repo) {
		this.repo = repo;
	}

	@Override
	public void recordUsage(Voucher voucher, User user, Order order, BigDecimal discountAmount) {
		VoucherUsage u = new VoucherUsage();
		u.setVoucher(voucher);
		u.setUser(user);
		u.setOrder(order);
		u.setDiscountAmount(discountAmount);
		repo.save(u);
	}

	@Override
	public void releaseUsageOfOrder(Long orderId) {
		repo.findByOrder(orderId).ifPresent(u -> {
			Voucher v = u.getVoucher();
			v.setUsedCount(Math.max(0, v.getUsedCount() - 1));
			repo.delete(u);
		});
	}
}
