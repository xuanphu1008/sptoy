package com.toyland.order;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.toyland.auth.CurrentUserService;
import com.toyland.cart.CartService;
import com.toyland.cart.dto.CartDtos.CartView;
import com.toyland.common.dto.PageResponse;
import com.toyland.common.exception.ApiException;
import com.toyland.common.util.TextUtils;
import com.toyland.order.dto.OrderDtos.OrderDto;
import com.toyland.order.dto.OrderDtos.OrderSummary;
import com.toyland.order.dto.OrderDtos.ReturnCreateRequest;
import com.toyland.order.dto.OrderDtos.TrackingDto;
import com.toyland.payment.MomoService;
import com.toyland.user.User;

/** Nghiep vu don hang phia khach hang. */
@Service
@Transactional
public class OrderService {

	private final OrderRepository orderRepository;
	private final ReturnRequestRepository returnRepository;
	private final CurrentUserService currentUser;
	private final OrderMapper mapper;
	private final OrderWorkflow workflow;
	private final MomoService momoService;
	private final CartService cartService;

	public OrderService(OrderRepository orderRepository, ReturnRequestRepository returnRepository,
			CurrentUserService currentUser, OrderMapper mapper, OrderWorkflow workflow, MomoService momoService,
			CartService cartService) {
		this.orderRepository = orderRepository;
		this.returnRepository = returnRepository;
		this.currentUser = currentUser;
		this.mapper = mapper;
		this.workflow = workflow;
		this.momoService = momoService;
		this.cartService = cartService;
	}

	@Transactional(readOnly = true)
	public PageResponse<OrderSummary> myOrders(OrderStatus status, int page, int size) {
		return PageResponse.of(orderRepository.findOfUser(currentUser.requireId(), status, PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100))),
				OrderMapper::summary);
	}

	@Transactional(readOnly = true)
	public OrderDto myOrder(Long id) {
		Order o = own(id);
		return mapper.toDto(o, returnRepository.findByOrderId(o.getId()).orElse(null), false);
	}

	public OrderDto cancel(Long id, String reason) {
		Order o = own(id);
		if (!o.getStatus().customerCancellable()) {
			throw ApiException.badRequest("CANNOT_CANCEL", "Đơn hàng đã được xác nhận, vui lòng liên hệ cửa hàng để huỷ");
		}
		o.setCancelReason(reason.trim());
		boolean wasPending = o.getStatus() == OrderStatus.PENDING;
		workflow.transition(o, OrderStatus.CANCELLED, "Khách huỷ: " + reason.trim(), currentUser.actorName());
		if (wasPending) {
			workflow.notifyStaff("ORDER_CANCELLED", "Khách huỷ đơn " + o.getOrderCode(), "Lý do: " + reason.trim(),
					"/admin/orders/" + o.getId());
		}
		return mapper.toDto(o, null, false);
	}

	/** Tao lai giao dich MoMo cho don dang cho thanh toan (thanh toan lai). */
	public String pay(Long id, String clientIp) {
		Order o = own(id);
		OrderDto dto = mapper.toDto(o, null, false);
		if (!dto.actions().canPay()) {
			throw ApiException.badRequest("CANNOT_PAY", "Đơn hàng không ở trạng thái chờ thanh toán hoặc đã quá hạn");
		}
		return momoService.createPayment(o);
	}

	public OrderDto requestReturn(Long id, ReturnCreateRequest r) {
		Order o = own(id);
		ReturnRequest existing = returnRepository.findByOrderId(o.getId()).orElse(null);
		if (!mapper.canReturn(o, existing)) {
			throw ApiException.badRequest("CANNOT_RETURN", "Đơn hàng không đủ điều kiện trả hàng (chỉ trong 7 ngày sau khi nhận hàng)");
		}
		ReturnRequest rr = new ReturnRequest();
		rr.setOrder(o);
		rr.setUser(o.getUser());
		rr.setReason(r.reason().trim());
		rr.setDescription(TextUtils.trimToNull(r.description()));
		if (r.imageUrls() != null) {
			rr.getImageUrls().addAll(r.imageUrls().stream().filter(s -> !TextUtils.isBlank(s)).limit(6).toList());
		}
		returnRepository.save(rr);
		workflow.transition(o, OrderStatus.RETURN_REQUESTED, "Yêu cầu trả hàng: " + rr.getReason(), currentUser.actorName());
		workflow.notifyStaff("RETURN_REQUESTED", "Yêu cầu trả hàng " + o.getOrderCode(),
				o.getReceiverName() + ": " + rr.getReason(), "/admin/orders/" + o.getId());
		return mapper.toDto(o, rr, false);
	}

	/** Mua lai: dua cac san pham cua don cu vao gio (bo qua san pham het hang). */
	public CartView reorder(Long id) {
		Order o = own(id);
		for (OrderItem i : o.getItems()) {
			if (i.getVariant() != null) {
				try {
					cartService.add(i.getVariant().getId(), i.getQuantity());
				} catch (ApiException ignored) {
					// san pham het hang / ngung ban -> bo qua
				}
			}
		}
		return cartService.get();
	}

	/** Tra cuu don khong can dang nhap: ma don + so dien thoai nguoi nhan. */
	@Transactional(readOnly = true)
	public TrackingDto track(String code, String phone) {
		Order o = orderRepository.findByOrderCode(code.trim().toUpperCase())
				.filter(x -> x.getPhone().replaceAll("\\s", "").equals(phone.replaceAll("\\s", "")))
				.orElseThrow(() -> ApiException.notFound("đơn hàng với mã và số điện thoại này", null));
		List<String> items = o.getItems().stream().map(i -> i.getProductName()
				+ (i.getVariantName() != null ? " (" + i.getVariantName() + ")" : "") + " × " + i.getQuantity()).toList();
		String name = o.getReceiverName();
		return new TrackingDto(o.getOrderCode(), o.getStatus(), o.getStatus().getLabel(), o.getCreatedAt(), o.getTotalAmount(),
				o.getPaymentMethod(), o.getPaymentStatus(), TextUtils.maskName(name), items,
				o.getHistory().stream().map(OrderMapper::history).toList());
	}

	private Order own(Long id) {
		User u = currentUser.require();
		Order o = orderRepository.findById(id).orElseThrow(() -> ApiException.notFound("đơn hàng", id));
		if (o.getUser() == null || !o.getUser().getId().equals(u.getId())) {
			throw ApiException.notFound("đơn hàng", id);
		}
		return o;
	}
}
