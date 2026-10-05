package com.toyland.promotion;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VoucherUsageRepository extends JpaRepository<VoucherUsage, Long> {

	@Query("SELECT COUNT(u) FROM VoucherUsage u WHERE u.voucher.id = :voucherId AND u.user.id = :userId")
	long countByVoucherAndUser(@Param("voucherId") Long voucherId, @Param("userId") Long userId);

	@Query("SELECT u FROM VoucherUsage u JOIN FETCH u.voucher WHERE u.order.id = :orderId")
	Optional<VoucherUsage> findByOrder(@Param("orderId") Long orderId);
}
