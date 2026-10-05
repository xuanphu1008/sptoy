package com.toyland.inventory.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.toyland.inventory.MovementType;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public final class InventoryDtos {

	private InventoryDtos() {
	}

	public record StockRow(Long variantId, Long productId, String productName, String productSlug, String sku,
			String variantName, String imageUrl, int stock, BigDecimal price, BigDecimal costPrice, boolean lowStock) {
	}

	public record AdjustRequest(@NotNull Long variantId, @NotNull @Min(value = 0, message = "Tồn kho không được âm") Integer newQuantity,
			@Size(max = 300) String note) {
	}

	public record MovementDto(Long id, Long variantId, String sku, MovementType type, int quantityChange, int balanceAfter,
			String refCode, String note, String createdBy, LocalDateTime createdAt) {
	}

	public record ReceiptItemRequest(@NotNull Long variantId, @NotNull @Min(value = 1, message = "Số lượng nhập phải >= 1") Integer quantity,
			@NotNull @PositiveOrZero BigDecimal unitCost) {
	}

	/** supplierId: chon tu danh muc NCC (uu tien); supplier: ghi tay khi nhap le. */
	public record ReceiptRequest(Long supplierId, @Size(max = 200) String supplier, @Size(max = 500) String note,
			@NotEmpty(message = "Phiếu nhập cần ít nhất 1 dòng") List<@Valid ReceiptItemRequest> items) {
	}

	public record ReceiptItemDto(Long variantId, String sku, String productName, int quantity, BigDecimal unitCost) {
	}

	public record ReceiptDto(Long id, String code, Long supplierId, String supplier, String note, int totalQuantity, BigDecimal totalCost,
			String createdBy, LocalDateTime createdAt, List<ReceiptItemDto> items) {
	}
}
