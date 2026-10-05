package com.toyland.wishlist;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.toyland.catalog.dto.CatalogDtos.ProductCard;
import com.toyland.common.dto.MessageResponse;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "04. Yêu thích")
@RestController
@RequestMapping("/api/v1/me/wishlist")
public class WishlistController {

	private final WishlistService service;

	public WishlistController(WishlistService service) {
		this.service = service;
	}

	@GetMapping
	public List<ProductCard> list() {
		return service.list();
	}

	@GetMapping("/ids")
	public List<Long> ids() {
		return service.ids();
	}

	@PostMapping("/{productId}")
	public MessageResponse add(@PathVariable Long productId) {
		service.add(productId);
		return new MessageResponse("Đã thêm vào yêu thích");
	}

	@DeleteMapping("/{productId}")
	public MessageResponse remove(@PathVariable Long productId) {
		service.remove(productId);
		return new MessageResponse("Đã bỏ khỏi yêu thích");
	}
}
