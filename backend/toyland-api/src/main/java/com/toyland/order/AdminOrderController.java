package com.toyland.order;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.toyland.common.dto.MessageResponse;
import com.toyland.common.dto.PageResponse;
import com.toyland.order.dto.OrderDtos.AdminReturnRow;
import com.toyland.order.dto.OrderDtos.BulkStatusRequest;
import com.toyland.order.dto.OrderDtos.InternalNoteRequest;
import com.toyland.order.dto.OrderDtos.OrderDto;
import com.toyland.order.dto.OrderDtos.OrderSummary;
import com.toyland.order.dto.OrderDtos.ReturnDecisionRequest;
import com.toyland.order.dto.OrderDtos.StatusUpdateRequest;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "12. Quản trị - Đơn hàng & trả hàng")
@RestController
public class AdminOrderController {

	private final AdminOrderService service;

	public AdminOrderController(AdminOrderService service) {
		this.service = service;
	}

	@GetMapping("/api/v1/admin/orders")
	public PageResponse<OrderSummary> search(@RequestParam(required = false) List<OrderStatus> status,
			@RequestParam(required = false) PaymentStatus paymentStatus,
			@RequestParam(required = false) PaymentMethod paymentMethod, @RequestParam(required = false) String keyword,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
		return service.search(status, paymentStatus, paymentMethod, keyword, from, to, page, size);
	}

	@GetMapping("/api/v1/admin/orders/{id}")
	public OrderDto get(@PathVariable Long id) {
		return service.get(id);
	}

	@PutMapping("/api/v1/admin/orders/{id}/status")
	public OrderDto updateStatus(@PathVariable Long id, @Valid @RequestBody StatusUpdateRequest r) {
		return service.updateStatus(id, r.status(), r.note());
	}

	@PostMapping("/api/v1/admin/orders/bulk-status")
	public MessageResponse bulk(@Valid @RequestBody BulkStatusRequest r) {
		int n = service.bulkStatus(r.ids(), r.status(), r.note());
		return new MessageResponse("Đã cập nhật " + n + "/" + r.ids().size() + " đơn hàng");
	}

	@PutMapping("/api/v1/admin/orders/{id}/note")
	public OrderDto note(@PathVariable Long id, @RequestBody InternalNoteRequest r) {
		return service.updateNote(id, r.internalNote());
	}

	@GetMapping("/api/v1/admin/returns")
	public PageResponse<AdminReturnRow> returns(@RequestParam(required = false) ReturnStatus status,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
		return service.returns(status, page, size);
	}

	@PutMapping("/api/v1/admin/returns/{id}/approve")
	public OrderDto approve(@PathVariable Long id, @RequestBody ReturnDecisionRequest r) {
		return service.approveReturn(id, r);
	}

	@PutMapping("/api/v1/admin/returns/{id}/reject")
	public OrderDto reject(@PathVariable Long id, @RequestBody ReturnDecisionRequest r) {
		return service.rejectReturn(id, r);
	}
}
