package com.toyland.inventory;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.toyland.common.dto.MessageResponse;
import com.toyland.common.dto.PageResponse;
import com.toyland.inventory.dto.InventoryDtos.AdjustRequest;
import com.toyland.inventory.dto.InventoryDtos.MovementDto;
import com.toyland.inventory.dto.InventoryDtos.ReceiptDto;
import com.toyland.inventory.dto.InventoryDtos.ReceiptRequest;
import com.toyland.inventory.dto.InventoryDtos.StockRow;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "11. Quản trị - Kho hàng")
@RestController
@RequestMapping("/api/v1/admin/inventory")
public class AdminInventoryController {

	private final InventoryService service;

	public AdminInventoryController(InventoryService service) {
		this.service = service;
	}

	@GetMapping
	public PageResponse<StockRow> list(@RequestParam(required = false) String keyword,
			@RequestParam(defaultValue = "false") boolean lowStock, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		return service.list(keyword, lowStock, page, size);
	}

	@PostMapping("/adjust")
	public MessageResponse adjust(@Valid @RequestBody AdjustRequest r) {
		service.adjust(r);
		return new MessageResponse("Đã điều chỉnh tồn kho");
	}

	@GetMapping("/movements")
	public PageResponse<MovementDto> movements(@RequestParam(required = false) Long variantId,
			@RequestParam(required = false) MovementType type, @RequestParam(required = false) String keyword,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "30") int size) {
		return service.movements(variantId, type, keyword, page, size);
	}

	@GetMapping("/receipts")
	public PageResponse<ReceiptDto> receipts(@RequestParam(required = false) Long supplierId,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
		return service.receipts(supplierId, page, size);
	}

	@GetMapping("/receipts/{id}")
	public ReceiptDto receipt(@PathVariable Long id) {
		return service.receipt(id);
	}

	@PostMapping("/receipts")
	@ResponseStatus(HttpStatus.CREATED)
	public ReceiptDto createReceipt(@Valid @RequestBody ReceiptRequest r) {
		return service.createReceipt(r);
	}
}
