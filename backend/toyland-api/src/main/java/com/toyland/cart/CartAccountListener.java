package com.toyland.cart;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.toyland.user.UserAccountDeletedEvent;

/** Mien gio hang tu xoa gio cua khach khi tai khoan bi xoa. */
@Component
class CartAccountListener {

	private final CartItemRepository repo;

	CartAccountListener(CartItemRepository repo) {
		this.repo = repo;
	}

	@EventListener
	public void onAccountDeleted(UserAccountDeletedEvent e) {
		repo.deleteAllOfUser(e.userId());
	}
}
