package com.toyland.order;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.toyland.audit.AuditService;
import com.toyland.auth.CurrentUserService;
import com.toyland.common.dto.PageResponse;
import com.toyland.common.exception.ApiException;
import com.toyland.common.util.TextUtils;
import com.toyland.order.dto.OrderDtos.AdminReturnRow;
import com.toyland.order.dto.OrderDtos.OrderDto;
import com.toyland.order.dto.OrderDtos.OrderSummary;
import com.toyland.order.dto.OrderDtos.ReturnDecisionRequest;

import jakarta.persistence.criteria.Predicate;

/** Nghiep vu xu ly don hang cua nhan vien / quan tri. */
@Service
@Transactional
public class AdminOrderService {

	private final OrderRepository orderRepository;
	private final ReturnRequestRepository returnRepository;
	private final OrderMapper mapper;
	private final OrderWorkflow workflow;
	private final CurrentUserService currentUser;
	private final AuditService audit;

	public AdminOrderService(OrderRepository orderRepository, ReturnRequestRepository returnRepository, OrderMapper mapper,
			OrderWorkflow workflow, CurrentUserService currentUser, AuditService audit) {
		this.orderRepository = orderRepository;
		this.returnRepository = returnRepository;
		this.mapper = mapper;
		this.workflow = workflow;
		this.currentUser = currentUser;
		this.audit = audit;
	}

	@Transactional(readOnly = true)
	public PageResponse<OrderSummary> search(List<OrderStatus> statuses, PaymentStatus paymentStatus,
			PaymentMethod paymentMethod, String keyword, LocalDate from, LocalDate to, int page, int size) {
		Specification<Order> spec = (root, q, cb) -> {
			List<Predicate> ps = new ArrayList<>();
			if (statuses != null && !statuses.isEmpty()) {
				ps.add(root.get("status").in(statuses));
			}
			if (paymentStatus != null) {
				ps.add(cb.equal(root.get("paymentStatus"), paymentStatus));
			}
			if (paymentMethod != null) {
				ps.add(cb.equal(root.get("paymentMethod"), paymentMethod));
			}
			if (!TextUtils.isBlank(keyword)) {
				String kw = "%" + keyword.trim().toLowerCase() + "%";
				ps.add(cb.or(cb.like(cb.lower(root.get("orderCode")), kw), cb.like(root.get("phone"), kw),
						cb.like(cb.lower(root.get("receiverName")), kw)));
			}
			if (from != null) {
				ps.add(cb.greaterThanOrEqualTo(root.get("createdAt"), from.atStartOfDay()));
			}
			if (to != null) {
				ps.add(cb.lessThan(root.get("createdAt"), to.plusDays(1).atStartOfDay()));
			}
			return cb.and(ps.toArray(new Predicate[0]));
		};
		return PageResponse.of(orderRepository.findAll(spec, PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 500), Sort.by(Sort.Direction.DESC, "createdAt"))),
				OrderMapper::summary);
	}

	@Transactional(readOnly = true)
	public OrderDto get(Long id) {
		Order o = find(id);
		return mapper.toDto(o, returnRepository.findByOrderId(id).orElse(null), true);
	}

	public OrderDto updateStatus(Long id, OrderStatus to, String note) {
		Order o = find(id);
		apply(o, to, note);
		return mapper.toDto(o, returnRepository.findByOrderId(id).orElse(null), true);
	}

	/** Cap nhat hang loat (vd: xac nhan nhieu don cung luc). Tra ve so don cap nhat thanh cong. */
	public int bulkStatus(List<Long> ids, OrderStatus to, String note) {
		int ok = 0;
		for (Long id : ids) {
			Order o = orderRepository.findById(id).orElse(null);
			if (o != null && o.getStatus().canTransitionTo(to)) {
				apply(o, to, note);
				ok++;
			}
		}
		return ok;
	}

	private void apply(Order o, OrderStatus to, String note) {
		if (!o.getStatus().canTransitionTo(to)) {
			throw ApiException.badRequest("INVALID_TRANSITION",
					"Không thể chuyển đơn từ \"" + o.getStatus().getLabel() + "\" sang \"" + to.getLabel() + "\"");
		}
		if (to == OrderStatus.CANCELLED) {
			o.setCancelReason(TextUtils.isBlank(note) ? "Cửa hàng huỷ đơn" : note.trim());
		}
		String from = o.getStatus().getLabel();
		workflow.transition(o, to, TextUtils.trimToNull(note), currentUser.actorName());
		audit.log("STATUS", "ORDER", o.getOrderCode(), "Đơn " + o.getOrderCode() + ": " + from + " → " + to.getLabel()
				+ (TextUtils.isBlank(note) ? "" : " (" + note.trim() + ")"));
	}

	public OrderDto updateNote(Long id, String note) {
		Order o = find(id);
		o.setInternalNote(TextUtils.trimToNull(note));
		audit.log("NOTE", "ORDER", o.getOrderCode(), "Cập nhật ghi chú nội bộ đơn " + o.getOrderCode());
		return mapper.toDto(o, returnRepository.findByOrderId(id).orElse(null), true);
	}

	// ------------------------------------------------ tra hang
	@Transactional(readOnly = true)
	public PageResponse<AdminReturnRow> returns(ReturnStatus status, int page, int size) {
		return PageResponse.of(returnRepository.search(status, PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 500))), r -> new AdminReturnRow(r.getId(),
				r.getOrder().getId(), r.getOrder().getOrderCode(), r.getUser().getFullName(), r.getReason(), r.getStatus(),
				r.getOrder().getTotalAmount(), r.getRefundAmount(), r.getCreatedAt()));
	}

	public OrderDto approveReturn(Long returnId, ReturnDecisionRequest d) {
		ReturnRequest r = findReturn(returnId);
		Order o = r.getOrder();
		BigDecimal refund = d.refundAmount() != null ? d.refundAmount() : o.getTotalAmount();
		if (refund.compareTo(o.getTotalAmount()) > 0 || refund.signum() < 0) {
			throw ApiException.badRequest("Số tiền hoàn không hợp lệ");
		}
		r.setStatus(ReturnStatus.APPROVED);
		r.setAdminNote(TextUtils.trimToNull(d.adminNote()));
		r.setRefundAmount(refund);
		r.setProcessedBy(currentUser.actorName());
		r.setProcessedAt(LocalDateTime.now());
		workflow.transition(o, OrderStatus.RETURNED, "Đồng ý trả hàng, hoàn " + refund.toBigInteger() + "đ", currentUser.actorName());
		audit.log("APPROVE", "RETURN", o.getOrderCode(), "Duyệt trả hàng đơn " + o.getOrderCode() + ", hoàn " + refund.toBigInteger() + "đ");
		return mapper.toDto(o, r, true);
	}

	public OrderDto rejectReturn(Long returnId, ReturnDecisionRequest d) {
		ReturnRequest r = findReturn(returnId);
		Order o = r.getOrder();
		r.setStatus(ReturnStatus.REJECTED);
		r.setAdminNote(TextUtils.trimToNull(d.adminNote()));
		r.setProcessedBy(currentUser.actorName());
		r.setProcessedAt(LocalDateTime.now());
		workflow.transition(o, OrderStatus.DELIVERED, "Từ chối trả hàng" + (r.getAdminNote() != null ? ": " + r.getAdminNote() : ""),
				currentUser.actorName());
		audit.log("REJECT", "RETURN", o.getOrderCode(), "Từ chối trả hàng đơn " + o.getOrderCode());
		return mapper.toDto(o, r, true);
	}

	private ReturnRequest findReturn(Long id) {
		ReturnRequest r = returnRepository.findById(id).orElseThrow(() -> ApiException.notFound("yêu cầu trả hàng", id));
		if (r.getStatus() != ReturnStatus.REQUESTED) {
			throw ApiException.badRequest("Yêu cầu này đã được xử lý");
		}
		return r;
	}

	private Order find(Long id) {
		return orderRepository.findById(id).orElseThrow(() -> ApiException.notFound("đơn hàng", id));
	}
}
