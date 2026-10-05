package com.toyland.catalog;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.toyland.catalog.dto.CatalogDtos.BrandDto;
import com.toyland.catalog.dto.CatalogDtos.CategoryNode;
import com.toyland.catalog.dto.CatalogDtos.FranchiseDto;
import com.toyland.catalog.dto.CatalogDtos.ProductCard;
import com.toyland.promotion.BannerRepository;
import com.toyland.promotion.BannerService;
import com.toyland.promotion.Campaign;
import com.toyland.promotion.PricingService;
import com.toyland.promotion.dto.PromotionDtos.BannerDto;

/** Du lieu trang chu gom trong 1 lan goi API. */
@Service
@Transactional(readOnly = true)
public class HomeService {

	private final ProductQueryService queryService;
	private final PricingService pricingService;
	private final BannerRepository bannerRepository;
	private final BrandRepository brandRepository;
	private final FranchiseRepository franchiseRepository;

	public HomeService(ProductQueryService queryService, PricingService pricingService, BannerRepository bannerRepository,
			BrandRepository brandRepository, FranchiseRepository franchiseRepository) {
		this.queryService = queryService;
		this.pricingService = pricingService;
		this.bannerRepository = bannerRepository;
		this.brandRepository = brandRepository;
		this.franchiseRepository = franchiseRepository;
	}

	public record CampaignInfo(Long id, String name, String badgeLabel, int discountPercent, LocalDateTime endAt) {
	}

	public record HomeData(List<BannerDto> banners, List<CategoryNode> categories, List<ProductCard> featured,
			List<ProductCard> bestSellers, List<ProductCard> newArrivals, List<ProductCard> onSale,
			List<CampaignInfo> campaigns, List<FranchiseDto> franchises, List<BrandDto> brands) {
	}

	public HomeData home() {
		List<Campaign> campaigns = pricingService.runningCampaigns();
		CategoryTree tree = queryService.tree();
		ProductFilter.SaleScope scope = queryService.saleScope(campaigns, tree);
		ProductFilter base = new ProductFilter(null, null, null, null, null, null, null, null, null, null, null, null, true, scope);
		ProductFilter featured = new ProductFilter(null, null, null, null, null, null, null, null, null, null, true, null, true, scope);
		ProductFilter sale = new ProductFilter(null, null, null, null, null, null, null, null, true, true, null, null, true, scope);
		return new HomeData(
				bannerRepository.findVisible(LocalDateTime.now()).stream().map(BannerService::toDto).toList(),
				tree.toNodes(true),
				queryService.list(featured, "bestseller", 8, campaigns),
				queryService.list(base, "bestseller", 8, campaigns),
				queryService.list(base, "newest", 8, campaigns),
				queryService.list(sale, "bestseller", 8, campaigns),
				campaigns.stream().map(c -> new CampaignInfo(c.getId(), c.getName(), c.getBadgeLabel(), c.getDiscountPercent(), c.getEndAt())).toList(),
				franchiseRepository.findAllByOrderByNameAsc().stream().map(ProductMapper::franchise).toList(),
				brandRepository.findAllByOrderByNameAsc().stream().map(ProductMapper::brand).toList());
	}
}
