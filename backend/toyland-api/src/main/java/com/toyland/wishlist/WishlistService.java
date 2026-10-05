package com.toyland.wishlist;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.toyland.auth.CurrentUserService;
import com.toyland.catalog.Product;
import com.toyland.catalog.ProductMapper;
import com.toyland.catalog.ProductRepository;
import com.toyland.catalog.dto.CatalogDtos.ProductCard;
import com.toyland.common.exception.ApiException;
import com.toyland.promotion.PricingService;
import com.toyland.user.User;

@Service
@Transactional
public class WishlistService {

	private final WishlistRepository repo;
	private final ProductRepository productRepository;
	private final CurrentUserService currentUser;
	private final ProductMapper mapper;
	private final PricingService pricingService;

	public WishlistService(WishlistRepository repo, ProductRepository productRepository, CurrentUserService currentUser,
			ProductMapper mapper, PricingService pricingService) {
		this.repo = repo;
		this.productRepository = productRepository;
		this.currentUser = currentUser;
		this.mapper = mapper;
		this.pricingService = pricingService;
	}

	@Transactional(readOnly = true)
	public List<ProductCard> list() {
		var campaigns = pricingService.runningCampaigns();
		return repo.findAllOfUser(currentUser.requireId()).stream().map(w -> mapper.card(w.getProduct(), campaigns)).toList();
	}

	@Transactional(readOnly = true)
	public List<Long> ids() {
		return repo.findProductIdsOfUser(currentUser.requireId());
	}

	public void add(Long productId) {
		User u = currentUser.require();
		if (repo.findOne(u.getId(), productId).isPresent()) {
			return;
		}
		Product p = productRepository.findByIdAndDeletedFalse(productId).orElseThrow(() -> ApiException.notFound("sản phẩm", productId));
		WishlistItem w = new WishlistItem();
		w.setUser(u);
		w.setProduct(p);
		repo.save(w);
	}

	public void remove(Long productId) {
		repo.findOne(currentUser.requireId(), productId).ifPresent(repo::delete);
	}
}
