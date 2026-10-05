package com.toyland.order;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReturnRequestRepository extends JpaRepository<ReturnRequest, Long> {

	@Query("SELECT r FROM ReturnRequest r WHERE r.order.id = :orderId")
	Optional<ReturnRequest> findByOrderId(@Param("orderId") Long orderId);

	@Query(value = "SELECT r FROM ReturnRequest r JOIN FETCH r.order o JOIN FETCH r.user u WHERE (:status IS NULL OR r.status = :status) ORDER BY r.id DESC",
			countQuery = "SELECT COUNT(r) FROM ReturnRequest r WHERE (:status IS NULL OR r.status = :status)")
	Page<ReturnRequest> search(@Param("status") ReturnStatus status, Pageable pageable);

	long countByStatus(ReturnStatus status);
}
