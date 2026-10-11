package com.toyland.partner;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.toyland.common.exception.ApiException;
import com.toyland.idempotency.IdempotencyService;
import com.toyland.order.PartnerOrderApi;
import com.toyland.order.PartnerOrderApi.Command;
import com.toyland.order.PartnerOrderApi.Line;
import com.toyland.order.PartnerOrderApi.PartnerOrder;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Dat / xem / huy don hang cua doi tac. Xac thuc ky HMAC giong cac API khac cua /api/partner/v1. */
@Tag(name = "13. API đối tác (Partner v1) - ký HMAC")
@RestController
@RequestMapping("/api/partner/v1/orders")
public class PartnerOrderController {

	private final PartnerOrderApi orders;
	private final IdempotencyService idempotency;

	public PartnerOrderController(PartnerOrderApi orders, IdempotencyService idempotency) {
		this.orders = orders;
		this.idempotency = idempotency;
	}

	public record OrderLine(@NotBlank @Size(max = 60) String sku, @Min(1) @Max(99) int quantity) {
	}

	public record CreateOrder(@NotBlank @Size(max = 64) String externalRef, @NotBlank @Size(max = 100) String receiverName,
			@NotBlank @Size(max = 20) String phone, @NotBlank @Size(max = 100) String province,
			@NotBlank @Size(max = 100) String district, @NotBlank @Size(max = 100) String ward,
			@NotBlank @Size(max = 255) String addressDetail, @Size(max = 500) String note,
			@NotEmpty @Size(max = 50) List<@Valid @NotNull OrderLine> items) {
	}

	public record CancelOrder(@Size(max = 300) String reason) {
	}

	@Operation(summary = "Đặt đơn hàng (trừ kho ngay)",
			description = "Bắt buộc gửi header Idempotency-Key. Gửi lại cùng khóa và cùng nội dung sẽ nhận lại đơn đã tạo. "
					+ "externalRef là mã đơn bên đối tác, không được trùng: trùng thì 409 PARTNER_REF_DUPLICATE. "
					+ "Hết hàng: 400 OUT_OF_STOCK. SKU không bán: 422 SKU_NOT_SELLABLE.")
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public PartnerOrder create(@RequestAttribute(PartnerAuthFilter.ATTR_PARTNER) Partner partner,
			@RequestHeader(value = IdempotencyService.HEADER, required = false) String idemKey,
			@Valid @RequestBody CreateOrder r) {
		if (idemKey == null || idemKey.isBlank()) {
			throw ApiException.badRequest("IDEMPOTENCY_KEY_REQUIRED", "Đặt hàng qua API đối tác bắt buộc có header Idempotency-Key");
		}
		Command cmd = new Command(r.externalRef(), r.receiverName(), r.phone(), r.province(), r.district(), r.ward(),
				r.addressDetail(), r.note(), r.items().stream().map(l -> new Line(l.sku(), l.quantity())).toList());
		return idempotency.execute(idemKey, "partner:" + partner.getId(), "POST /api/partner/v1/orders " + r, PartnerOrder.class,
				() -> orders.create(partner.getId(), partner.getName(), cmd));
	}

	@Operation(summary = "Xem đơn theo mã ToyLand (chỉ đơn của chính đối tác)")
	@GetMapping("/{orderCode}")
	public PartnerOrder get(@RequestAttribute(PartnerAuthFilter.ATTR_PARTNER) Partner partner, @PathVariable String orderCode) {
		return orders.find(partner.getId(), orderCode).orElseThrow(() -> ApiException.notFound("đơn hàng", orderCode));
	}

	@Operation(summary = "Huỷ đơn khi chưa được xác nhận, hoàn lại kho")
	@PostMapping("/{orderCode}/cancel")
	public PartnerOrder cancel(@RequestAttribute(PartnerAuthFilter.ATTR_PARTNER) Partner partner, @PathVariable String orderCode,
			@RequestBody(required = false) CancelOrder r) {
		return orders.cancel(partner.getId(), partner.getName(), orderCode, r == null ? null : r.reason());
	}
}
