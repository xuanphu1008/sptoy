package com.toyland.order;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Giao dien cong bo (chi doc) cua mien Don hang cho cac mien khac: thanh toan, danh gia,
 * tai khoan, bao cao. Mien khac khong dung truc tiep OrderRepository / OrderItemRepository /
 * ReturnRequestRepository.
 */
public interface OrderQueryApi {

	Optional<Order> findById(Long orderId);

	long countOrdersOfUser(Long userId);

	/** Dong san pham thuoc don cua chinh nguoi dung (dung khi viet danh gia). */
	Optional<OrderItem> findOwnItem(Long orderItemId, Long userId);

	Optional<ReturnRequest> findReturnOfOrder(Long orderId);

	List<Order> findRefundPending();

	Page<Order> findCodOutstanding(List<OrderStatus> statuses, Pageable pageable);

	/** [so don, tong tien] COD chua thu. */
	Object[] codOutstandingTotals(List<OrderStatus> statuses);

	long countByStatusIn(List<OrderStatus> statuses);

	long countReturnsByStatus(ReturnStatus status);

	List<Order> latest(int limit);
}
