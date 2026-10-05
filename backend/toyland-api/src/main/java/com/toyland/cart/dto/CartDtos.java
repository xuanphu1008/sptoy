package com.toyland.cart.dto;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public final class CartDtos {

	private CartDtos() {
	}

	public record AddToCartRequest(@NotNull Long variantId,
			@NotNull @Min(value = 1, message = "Số lượng tối thiểu 1") @Max(value = 99, message = "Tối đa 99") Integer quantity) {
	}

	public record UpdateQuantityRequest(@NotNull @Min(1) @Max(99) Integer quantity) {
	}

	/** Dong gio hang tu localStorage cua khach chua dang nhap. */
	public record GuestLine(@NotNull Long variantId, @NotNull @Min(1) @Max(99) Integer quantity) {
	}

	public record CartLine(Long itemId, Long variantId, Long productId, String productName, String productSlug,
			String variantName, String imageUrl, BigDecimal unitPrice, BigDecimal originalPrice, int quantity, int stock,
			boolean available, String message, BigDecimal lineTotal, int weightGram) {
	}

	public record CartView(List<CartLine> lines, int totalQuantity, BigDecimal subtotal, boolean hasIssues) {
	}
}
