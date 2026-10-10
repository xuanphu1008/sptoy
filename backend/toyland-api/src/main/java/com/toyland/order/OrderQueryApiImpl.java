package com.toyland.order;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
class OrderQueryApiImpl implements OrderQueryApi {

	private final OrderRepository orderRepository;
	private final OrderItemRepository orderItemRepository;
	private final ReturnRequestRepository returnRepository;

	OrderQueryApiImpl(OrderRepository orderRepository, OrderItemRepository orderItemRepository,
			ReturnRequestRepository returnRepository) {
		this.orderRepository = orderRepository;
		this.orderItemRepository = orderItemRepository;
		this.returnRepository = returnRepository;
	}

	@Override
	public Optional<Order> findById(Long orderId) {
		return orderRepository.findById(orderId);
	}

	@Override
	public long countOrdersOfUser(Long userId) {
		return orderRepository.countOfUser(userId);
	}

	@Override
	public Optional<OrderItem> findOwnItem(Long orderItemId, Long userId) {
		return orderItemRepository.findOwn(orderItemId, userId);
	}

	@Override
	public Optional<ReturnRequest> findReturnOfOrder(Long orderId) {
		return returnRepository.findByOrderId(orderId);
	}

	@Override
	public List<Order> findRefundPending() {
		return orderRepository.findRefundPending();
	}

	@Override
	public Page<Order> findCodOutstanding(List<OrderStatus> statuses, Pageable pageable) {
		return orderRepository.findCodOutstanding(statuses, pageable);
	}

	@Override
	public Object[] codOutstandingTotals(List<OrderStatus> statuses) {
		return orderRepository.codOutstandingTotals(statuses).get(0);
	}

	@Override
	public long countByStatusIn(List<OrderStatus> statuses) {
		return orderRepository.countByStatusIn(statuses);
	}

	@Override
	public long countReturnsByStatus(ReturnStatus status) {
		return returnRepository.countByStatus(status);
	}

	@Override
	public List<Order> latest(int limit) {
		return orderRepository.findAll(PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"))).getContent();
	}
}
