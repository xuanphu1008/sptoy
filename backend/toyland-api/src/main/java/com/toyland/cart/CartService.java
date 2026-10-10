package com.toyland.cart;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.toyland.auth.CurrentUserService;
import com.toyland.cart.dto.CartDtos.CartLine;
import com.toyland.cart.dto.CartDtos.CartView;
import com.toyland.cart.dto.CartDtos.GuestLine;
import com.toyland.catalog.Product;
import com.toyland.catalog.ProductStatus;
import com.toyland.catalog.ProductVariant;
import com.toyland.catalog.CatalogApi;
import com.toyland.common.exception.ApiException;
import com.toyland.promotion.Campaign;
import com.toyland.promotion.PricingService;
import com.toyland.promotion.PricingService.PriceInfo;
import com.toyland.user.User;

@Service
@Transactional
public class CartService {

	public static final int MAX_QTY = 99;

	private final CartItemRepository repo;
	private final CatalogApi catalog;
	private final CurrentUserService currentUser;
	private final PricingService pricingService;

	public CartService(CartItemRepository repo, CatalogApi catalog, CurrentUserService currentUser,
			PricingService pricingService) {
		this.repo = repo;
		this.catalog = catalog;
		this.currentUser = currentUser;
		this.pricingService = pricingService;
	}

	/** Bien the co dang ban khong? */
	public static String unavailableReason(ProductVariant v, int qty) {
		Product p = v.getProduct();
		if (p.isDeleted() || !v.isActive() || p.getStatus() == ProductStatus.INACTIVE || p.getStatus() == ProductStatus.DRAFT) {
			return "Sản phẩm đã ngừng kinh doanh";
		}
		if (p.getStatus() == ProductStatus.COMING_SOON) {
			return "Sản phẩm sắp về, chưa mở bán";
		}
		if (v.getStockQuantity() <= 0) {
			return "Hết hàng";
		}
		if (qty > v.getStockQuantity()) {
			return "Chỉ còn " + v.getStockQuantity() + " sản phẩm";
		}
		return null;
	}

	public CartLine line(Long itemId, ProductVariant v, int qty, List<Campaign> campaigns) {
		Product p = v.getProduct();
		PriceInfo pi = pricingService.price(p, v, campaigns);
		String reason = unavailableReason(v, qty);
		String img = v.getImageUrl() != null ? v.getImageUrl() : (p.getImages().isEmpty() ? null : p.getImages().get(0).getUrl());
		return new CartLine(itemId, v.getId(), p.getId(), p.getName(), p.getSlug(), v.getDisplayName(), img, pi.price(),
				pi.originalPrice(), qty, v.getStockQuantity(), reason == null, reason,
				pi.price().multiply(BigDecimal.valueOf(qty)), v.getWeightGram());
	}

	public static CartView view(List<CartLine> lines) {
		int qty = 0;
		BigDecimal subtotal = BigDecimal.ZERO;
		boolean issues = false;
		for (CartLine l : lines) {
			if (l.available()) {
				qty += l.quantity();
				subtotal = subtotal.add(l.lineTotal());
			} else {
				issues = true;
			}
		}
		return new CartView(lines, qty, subtotal, issues);
	}

	@Transactional(readOnly = true)
	public CartView get() {
		return build(currentUser.requireId());
	}

	private CartView build(Long userId) {
		List<Campaign> campaigns = pricingService.runningCampaigns();
		List<CartLine> lines = new ArrayList<>();
		for (CartItem c : repo.findAllOfUser(userId)) {
			lines.add(line(c.getId(), c.getVariant(), c.getQuantity(), campaigns));
		}
		return view(lines);
	}

	// noRollbackFor: "Mua lai" bat loi het hang tung san pham -> khong lam hong ca giao dich
	@Transactional(noRollbackFor = ApiException.class)
	public CartView add(Long variantId, int quantity) {
		User u = currentUser.require();
		ProductVariant v = catalog.findVariantWithProduct(variantId).orElseThrow(() -> ApiException.notFound("sản phẩm", variantId));
		CartItem item = repo.findOne(u.getId(), variantId).orElse(null);
		int newQty = (item == null ? 0 : item.getQuantity()) + quantity;
		String reason = unavailableReason(v, Math.min(newQty, MAX_QTY));
		if (reason != null) {
			throw ApiException.badRequest("OUT_OF_STOCK", reason);
		}
		if (item == null) {
			item = new CartItem();
			item.setUser(u);
			item.setVariant(v);
		}
		item.setQuantity(Math.min(newQty, MAX_QTY));
		repo.save(item);
		return build(u.getId());
	}

	public CartView update(Long itemId, int quantity) {
		Long uid = currentUser.requireId();
		CartItem item = repo.findOwn(itemId, uid).orElseThrow(() -> ApiException.notFound("mục giỏ hàng", itemId));
		String reason = unavailableReason(item.getVariant(), quantity);
		if (reason != null && item.getVariant().getStockQuantity() > 0 && quantity > item.getVariant().getStockQuantity()) {
			throw ApiException.badRequest("OUT_OF_STOCK", reason);
		}
		item.setQuantity(quantity);
		return build(uid);
	}

	public CartView remove(Long itemId) {
		Long uid = currentUser.requireId();
		repo.findOwn(itemId, uid).ifPresent(repo::delete);
		repo.flush();
		return build(uid);
	}

	public CartView clear() {
		Long uid = currentUser.requireId();
		repo.deleteAllOfUser(uid);
		return build(uid);
	}

	/** Gop gio hang cua khach (localStorage) vao gio hang tai khoan sau khi dang nhap. */
	public CartView merge(List<GuestLine> guest) {
		User u = currentUser.require();
		if (guest != null) {
			for (GuestLine g : guest) {
				if (g == null || g.variantId() == null || g.quantity() == null || g.quantity() < 1) {
					continue;
				}
				ProductVariant v = catalog.findVariantWithProduct(g.variantId()).orElse(null);
				if (v == null) {
					continue;
				}
				CartItem item = repo.findOne(u.getId(), v.getId()).orElse(null);
				int qty = Math.min((item == null ? 0 : item.getQuantity()) + g.quantity(), MAX_QTY);
				if (v.getStockQuantity() > 0) {
					qty = Math.min(qty, v.getStockQuantity());
				}
				if (item == null) {
					item = new CartItem();
					item.setUser(u);
					item.setVariant(v);
				}
				item.setQuantity(Math.max(qty, 1));
				repo.save(item);
			}
		}
		return build(u.getId());
	}

	/** Xem truoc gio hang cho khach chua dang nhap (khong luu DB). */
	@Transactional(readOnly = true)
	public CartView preview(List<GuestLine> guest) {
		List<Campaign> campaigns = pricingService.runningCampaigns();
		Map<Long, Integer> qty = new HashMap<>();
		if (guest != null) {
			guest.stream().filter(g -> g != null && g.variantId() != null && g.quantity() != null && g.quantity() > 0)
					.forEach(g -> qty.merge(g.variantId(), g.quantity(), Integer::sum));
		}
		List<CartLine> lines = new ArrayList<>();
		if (!qty.isEmpty()) {
			for (ProductVariant v : catalog.findVariantsWithProduct(qty.keySet())) {
				lines.add(line(null, v, Math.min(qty.get(v.getId()), MAX_QTY), campaigns));
			}
		}
		return view(lines);
	}
}
