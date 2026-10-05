package com.toyland.audit;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.toyland.audit.AuditService.AuditDto;
import com.toyland.common.dto.PageResponse;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "19. Quản trị - Nhật ký hoạt động")
@RestController
public class AdminAuditController {

	private final AuditService service;

	public AdminAuditController(AuditService service) {
		this.service = service;
	}

	/** Chi ADMIN (cau hinh trong SecurityConfig). */
	@GetMapping("/api/v1/admin/audit-logs")
	public PageResponse<AuditDto> search(@RequestParam(required = false) String actor,
			@RequestParam(required = false) String entityType, @RequestParam(required = false) String action,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
			@RequestParam(required = false) String keyword, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "30") int size) {
		return service.search(actor, entityType, action, from, to, keyword, page, size);
	}
}
