package com.toyland.wishlist;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.toyland.user.UserAccountDeletedEvent;

/** Mien yeu thich tu xoa danh sach cua khach khi tai khoan bi xoa. */
@Component
class WishlistAccountListener {

	private final WishlistRepository repo;

	WishlistAccountListener(WishlistRepository repo) {
		this.repo = repo;
	}

	@EventListener
	public void onAccountDeleted(UserAccountDeletedEvent e) {
		repo.deleteAll(repo.findAllOfUser(e.userId()));
	}
}
