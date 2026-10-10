package com.toyland.order;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.toyland.auth.CurrentUserService;
import com.toyland.cart.dto.CartDtos.CartView;
import com.toyland.common.dto.PageResponse;
import com.toyland.common.util.ClientIp;
import com.toyland.idempotency.IdempotencyService;
import com.toyland.order.dto.OrderDtos.CancelRequest;
import com.toyland.order.dto.OrderDtos.OrderDto;
import com.toyland.order.dto.OrderDtos.OrderSummary;
import com.toyland.order.dto.OrderDtos.PlaceOrderRequest;
import com.toyland.order.dto.OrderDtos.PlaceOrderResponse;
import com.toyland.order.dto.OrderDtos.Quote;
import com.toyland.order.dto.OrderDtos.QuoteRequest;
import com.toyland.order.dto.OrderDtos.ReturnCreateRequest;
import com.toyland.order.dto.OrderDtos.TrackingDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@Tag(name = "08. Đặt hàng & Đơn hàng của tôi")
@RestController
public class OrderController {

	private final CheckoutService checkoutService;
	private final OrderService orderService;
	private final IdempotencyService idempotency;
	private final CurrentUserService currentUser;

	public OrderController(CheckoutService checkoutService, OrderService orderService, IdempotencyService idempotency,
			CurrentUserService currentUser) {
		this.checkoutService = checkoutService;
		this.orderService = orderService;
		this.idempotency = idempotency;
		this.currentUser = currentUser;
	}

	@Operation(summary = "Tính tạm đơn hàng: tạm tính, giảm giá voucher, phí ship GHN, tổng tiền")
	@PostMapping("/api/v1/checkout/quote")
	public Quote quote(@RequestBody QuoteRequest r) {
		return checkoutService.quote(r);
	}

	@Operation(summary = "Đặt hàng từ giỏ. Nếu MoMo: trả về paymentUrl để chuyển hướng",
			description = "Nên gửi header Idempotency-Key (chuỗi ngẫu nhiên duy nhất cho mỗi lần bấm Đặt hàng). "
					+ "Gửi lại cùng khóa + cùng nội dung sẽ nhận lại đơn đã tạo, không tạo đơn thứ hai. "
					+ "Cùng khóa nhưng nội dung khác: 409 IDEMPOTENCY_KEY_REUSED. Đang xử lý: 409 IDEMPOTENCY_IN_PROGRESS.")
	@PostMapping("/api/v1/orders")
	@ResponseStatus(HttpStatus.CREATED)
	public PlaceOrderResponse place(@Valid @RequestBody PlaceOrderRequest r, HttpServletRequest req,
			@RequestHeader(value = IdempotencyService.HEADER, required = false) String idemKey) {
		String subject = "user:" + currentUser.requireId();
		return idempotency.execute(idemKey, subject, "POST /api/v1/orders " + r, PlaceOrderResponse.class,
				() -> checkoutService.place(r, ClientIp.of(req)));
	}

	@GetMapping("/api/v1/orders")
	public PageResponse<OrderSummary> myOrders(@RequestParam(required = false) OrderStatus status,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
		return orderService.myOrders(status, page, size);
	}

	@GetMapping("/api/v1/orders/{id}")
	public OrderDto myOrder(@PathVariable Long id) {
		return orderService.myOrder(id);
	}

	@PostMapping("/api/v1/orders/{id}/cancel")
	public OrderDto cancel(@PathVariable Long id, @Valid @RequestBody CancelRequest r) {
		return orderService.cancel(id, r.reason());
	}

	@Operation(summary = "Tạo lại giao dịch MoMo cho đơn đang chờ thanh toán")
	@PostMapping("/api/v1/orders/{id}/pay")
	public Map<String, String> pay(@PathVariable Long id, HttpServletRequest req) {
		return Map.of("paymentUrl", orderService.pay(id, ClientIp.of(req)));
	}

	@PostMapping("/api/v1/orders/{id}/return")
	public OrderDto requestReturn(@PathVariable Long id, @Valid @RequestBody ReturnCreateRequest r) {
		return orderService.requestReturn(id, r);
	}

	@PostMapping("/api/v1/orders/{id}/reorder")
	public CartView reorder(@PathVariable Long id) {
		return orderService.reorder(id);
	}

	@Operation(summary = "Tra cứu đơn hàng không cần đăng nhập (mã đơn + số điện thoại)")
	@GetMapping("/api/v1/orders/track")
	public TrackingDto track(@RequestParam String code, @RequestParam String phone) {
		return orderService.track(code, phone);
	}
}
