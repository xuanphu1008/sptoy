package com.toyland.cart;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.toyland.cart.dto.CartDtos.AddToCartRequest;
import com.toyland.cart.dto.CartDtos.CartView;
import com.toyland.cart.dto.CartDtos.GuestLine;
import com.toyland.cart.dto.CartDtos.UpdateQuantityRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "06. Giỏ hàng")
@RestController
@RequestMapping("/api/v1/cart")
public class CartController {

	private final CartService service;

	public CartController(CartService service) {
		this.service = service;
	}

	@GetMapping
	public CartView get() {
		return service.get();
	}

	@PostMapping
	public CartView add(@Valid @RequestBody AddToCartRequest r) {
		return service.add(r.variantId(), r.quantity());
	}

	@PutMapping("/{itemId}")
	public CartView update(@PathVariable Long itemId, @Valid @RequestBody UpdateQuantityRequest r) {
		return service.update(itemId, r.quantity());
	}

	@DeleteMapping("/{itemId}")
	public CartView remove(@PathVariable Long itemId) {
		return service.remove(itemId);
	}

	@DeleteMapping
	public CartView clear() {
		return service.clear();
	}

	@Operation(summary = "Gộp giỏ hàng của khách (localStorage) vào tài khoản sau khi đăng nhập")
	@PostMapping("/merge")
	public CartView merge(@RequestBody List<GuestLine> lines) {
		return service.merge(lines);
	}

	@Operation(summary = "Xem trước giỏ hàng cho khách chưa đăng nhập (không cần token)")
	@PostMapping("/preview")
	public CartView preview(@RequestBody List<GuestLine> lines) {
		return service.preview(lines);
	}
}
