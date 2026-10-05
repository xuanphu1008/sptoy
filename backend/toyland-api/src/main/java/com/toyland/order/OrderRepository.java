package com.toyland.order;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

	Optional<Order> findByOrderCode(String orderCode);

	boolean existsByOrderCode(String orderCode);

	@Query("SELECT COUNT(o) FROM ShopOrder o WHERE o.user.id = :userId")
	long countOfUser(@Param("userId") Long userId);

	@Query(value = "SELECT o FROM ShopOrder o WHERE o.user.id = :userId AND (:status IS NULL OR o.status = :status) ORDER BY o.createdAt DESC",
			countQuery = "SELECT COUNT(o) FROM ShopOrder o WHERE o.user.id = :userId AND (:status IS NULL OR o.status = :status)")
	Page<Order> findOfUser(@Param("userId") Long userId, @Param("status") OrderStatus status, Pageable pageable);

	@Query("SELECT o FROM ShopOrder o WHERE o.status = com.toyland.order.OrderStatus.PENDING_PAYMENT AND o.paymentDeadline < :now")
	List<Order> findExpiredUnpaid(@Param("now") LocalDateTime now);

	@Query("SELECT o FROM ShopOrder o WHERE o.status = com.toyland.order.OrderStatus.DELIVERED AND o.deliveredAt < :before")
	List<Order> findDeliveredBefore(@Param("before") LocalDateTime before);

	/** Don cho hoan tien (hang doi cua man hinh Thanh toan). */
	@Query("SELECT o FROM ShopOrder o WHERE o.paymentStatus = com.toyland.order.PaymentStatus.REFUND_PENDING ORDER BY o.updatedAt")
	List<Order> findRefundPending();

	/** Don COD chua thu tien, hang con dang tren duong giao (tien dang nam o khau van chuyen). */
	@Query(value = "SELECT o FROM ShopOrder o WHERE o.paymentMethod = com.toyland.order.PaymentMethod.COD"
			+ " AND o.paymentStatus = com.toyland.order.PaymentStatus.UNPAID AND o.status IN :statuses ORDER BY o.createdAt",
			countQuery = "SELECT COUNT(o) FROM ShopOrder o WHERE o.paymentMethod = com.toyland.order.PaymentMethod.COD"
					+ " AND o.paymentStatus = com.toyland.order.PaymentStatus.UNPAID AND o.status IN :statuses")
	Page<Order> findCodOutstanding(@Param("statuses") List<OrderStatus> statuses, Pageable pageable);

	@Query("SELECT COUNT(o), COALESCE(SUM(o.totalAmount), 0) FROM ShopOrder o WHERE o.paymentMethod = com.toyland.order.PaymentMethod.COD"
			+ " AND o.paymentStatus = com.toyland.order.PaymentStatus.UNPAID AND o.status IN :statuses")
	List<Object[]> codOutstandingTotals(@Param("statuses") List<OrderStatus> statuses);

	@Query("SELECT COUNT(o) FROM ShopOrder o WHERE o.status IN :statuses")
	long countByStatusIn(@Param("statuses") List<OrderStatus> statuses);
}
