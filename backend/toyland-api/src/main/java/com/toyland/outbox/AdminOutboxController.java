package com.toyland.outbox;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.toyland.common.dto.MessageResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/** Theo doi hang doi su kien (outbox): so luong theo trang thai, 20 su kien gan nhat, gui lai su kien DEAD. */
@Tag(name = "14. Quản trị đối tác (ADMIN)")
@RestController
@RequestMapping("/api/v1/admin/outbox")
public class AdminOutboxController {

	private final OutboxService service;

	public AdminOutboxController(OutboxService service) {
		this.service = service;
	}

	@Operation(summary = "Tình trạng hàng đợi sự kiện (outbox)")
	@GetMapping
	public OutboxService.Summary summary() {
		return service.summary();
	}

	@Operation(summary = "Gửi lại một sự kiện (thường là DEAD)")
	@PostMapping("/{id}/retry")
	public MessageResponse retry(@PathVariable Long id) {
		service.retry(id);
		return new MessageResponse("Đã đưa sự kiện vào hàng đợi gửi lại");
	}
}
