package com.toyland.loyalty;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointTransactionRepository extends JpaRepository<PointTransaction, Long> {

	Page<PointTransaction> findByUserIdOrderByIdDesc(Long userId, Pageable pageable);

	boolean existsByOrderIdAndType(Long orderId, PointType type);
}
