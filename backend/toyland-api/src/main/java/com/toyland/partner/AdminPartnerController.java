package com.toyland.partner;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Quan tri doi tac (chi ADMIN): cap khoa, bat/tat. Secret chi hien MOT lan luc tao. */
@Tag(name = "14. Quản trị đối tác (ADMIN)")
@RestController
@RequestMapping("/api/v1/admin/partners")
public class AdminPartnerController {

	private final PartnerAdminService service;

	public AdminPartnerController(PartnerAdminService service) {
		this.service = service;
	}

	public record CreatePartner(@NotBlank @Size(max = 120) String name, @Min(1) @Max(6000) Integer rateLimitPerMin) {
	}

	public record PartnerView(Long id, String name, String apiKey, boolean active, int rateLimitPerMin,
			java.time.LocalDateTime lastUsedAt, String webhookUrl) {
	}

	public record CreatedPartner(Long id, String name, String apiKey, String secret, String note) {
	}

	public record ActiveRequest(boolean active) {
	}

	public record WebhookRequest(@Size(max = 300) @jakarta.validation.constraints.Pattern(
			regexp = "^$|^https://[^\\s]+$|^http://(localhost|127\\.0\\.0\\.1)(:\\d+)?(/[^\\s]*)?$",
			message = "Webhook phải là địa chỉ https:// (hoặc http://localhost khi chạy thử)") String webhookUrl) {
	}

	@Operation(summary = "Danh sách đối tác (không trả secret)")
	@GetMapping
	public List<PartnerView> list() {
		return service.list();
	}

	@Operation(summary = "Tạo đối tác mới; secret chỉ hiển thị một lần trong phản hồi này")
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public CreatedPartner create(@Valid @RequestBody CreatePartner r) {
		return service.create(r);
	}

	@Operation(summary = "Đặt địa chỉ nhận webhook của đối tác (để trống để tắt)")
	@PatchMapping("/{id}/webhook")
	public PartnerView setWebhook(@PathVariable Long id, @Valid @RequestBody WebhookRequest r) {
		return service.setWebhook(id, r.webhookUrl());
	}

	@Operation(summary = "Bật/tắt đối tác")
	@PatchMapping("/{id}/active")
	public PartnerView setActive(@PathVariable Long id, @RequestBody ActiveRequest r) {
		return service.setActive(id, r.active());
	}
}
