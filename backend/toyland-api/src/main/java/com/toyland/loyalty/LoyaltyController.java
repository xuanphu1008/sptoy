package com.toyland.loyalty;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.toyland.common.dto.PageResponse;
import com.toyland.loyalty.LoyaltyService.Summary;
import com.toyland.loyalty.LoyaltyService.TxDto;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Tag(name = "18. Khách hàng thân thiết (điểm thưởng)")
@RestController
public class LoyaltyController {

	private final LoyaltyService service;

	public LoyaltyController(LoyaltyService service) {
		this.service = service;
	}

	public record AdjustPointsRequest(@NotNull(message = "Nhập số điểm") @Min(-1000000) @Max(1000000) Integer delta,
			@Size(max = 300) String note) {
	}

	@GetMapping("/api/v1/account/points")
	public Summary mine() {
		return service.mySummary();
	}

	@GetMapping("/api/v1/account/points/history")
	public PageResponse<TxDto> myHistory(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
		return service.myHistory(page, size);
	}

	/** Quan tri: xem diem cua 1 khach (duong dan /admin/users/** -> chi ADMIN). */
	@GetMapping("/api/v1/admin/users/{id}/points")
	public Summary summary(@PathVariable Long id) {
		return service.summary(id);
	}

	@GetMapping("/api/v1/admin/users/{id}/points/history")
	public PageResponse<TxDto> history(@PathVariable Long id, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		return service.history(id, page, size);
	}

	@PostMapping("/api/v1/admin/users/{id}/points")
	public Summary adjust(@PathVariable Long id, @Valid @RequestBody AdjustPointsRequest r) {
		return service.adjust(id, r.delta(), r.note());
	}
}
