package com.toyland.report;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "15. Quản trị - Thống kê")
@RestController
public class DashboardController {

	private final DashboardService service;

	public DashboardController(DashboardService service) {
		this.service = service;
	}

	/** Mac dinh 30 ngay gan nhat. */
	@GetMapping("/api/v1/admin/dashboard")
	public DashboardService.Dashboard dashboard(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
		return service.build(from, to);
	}
}
