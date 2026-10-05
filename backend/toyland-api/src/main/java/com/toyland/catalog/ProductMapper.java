package com.toyland.catalog;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.toyland.catalog.dto.CatalogDtos.BrandDto;
import com.toyland.catalog.dto.CatalogDtos.Crumb;
import com.toyland.catalog.dto.CatalogDtos.FranchiseDto;
import com.toyland.catalog.dto.CatalogDtos.ImageDto;
import com.toyland.catalog.dto.CatalogDtos.ProductCard;
import com.toyland.catalog.dto.CatalogDtos.ProductDetail;
import com.toyland.catalog.dto.CatalogDtos.SpecDto;
import com.toyland.catalog.dto.CatalogDtos.VariantDto;
import com.toyland.promotion.Campaign;
import com.toyland.promotion.PricingService;
import com.toyland.promotion.PricingService.PriceInfo;

/** Chuyen Entity -> DTO (phai goi trong transaction vi truy cap quan he lazy). */
@Component
public class ProductMapper {

	private final PricingService pricingService;

	public ProductMapper(PricingService pricingService) {
		this.pricingService = pricingService;
	}

	public static BrandDto brand(Brand b) {
		return b == null ? null : new BrandDto(b.getId(), b.getName(), b.getSlug(), b.getLogoUrl(), b.getCountry(), b.getDescription());
	}

	public static FranchiseDto franchise(Franchise f) {
		return new FranchiseDto(f.getId(), f.getName(), f.getSlug(), f.getImageUrl(), f.getDescription());
	}

	public static List<ProductVariant> activeVariants(Product p) {
		return p.getVariants().stream().filter(ProductVariant::isActive).toList();
	}

	public ProductCard card(Product p, List<Campaign> campaigns) {
		Campaign camp = pricingService.bestCampaign(p, campaigns);
		List<ProductVariant> vs = activeVariants(p);
		ProductVariant best = null;
		PriceInfo bestPrice = null;
		for (ProductVariant v : vs) {
			PriceInfo pi = pricingService.price(v, camp);
			boolean better = best == null
					|| (v.getStockQuantity() > 0 && best.getStockQuantity() <= 0)
					|| ((v.getStockQuantity() > 0) == (best.getStockQuantity() > 0) && pi.price().compareTo(bestPrice.price()) < 0);
			if (better) {
				best = v;
				bestPrice = pi;
			}
		}
		List<ProductImage> imgs = p.getImages();
		String img = !imgs.isEmpty() ? imgs.get(0).getUrl() : (best != null ? best.getImageUrl() : null);
		String hover = imgs.size() > 1 ? imgs.get(1).getUrl() : null;
		boolean isNew = p.getCreatedAt() != null && p.getCreatedAt().isAfter(LocalDateTime.now().minusDays(30));
		return new ProductCard(p.getId(), p.getName(), p.getSlug(), img, hover,
				p.getBrand() == null ? null : p.getBrand().getName(),
				AgeGroup.labelOf(p.getAgeMinMonths(), p.getAgeMaxMonths()),
				bestPrice == null ? p.getMinPrice() : bestPrice.price(),
				bestPrice == null ? null : bestPrice.originalPrice(),
				bestPrice == null ? 0 : bestPrice.discountPercent(),
				camp == null ? null : camp.getBadgeLabel(),
				p.getAvgRating(), p.getReviewCount(), p.getSoldCount(), p.getTotalStock() > 0, p.getStatus(),
				p.isFeatured(), p.isOnlineExclusive(), isNew, vs.size(),
				vs.size() == 1 ? vs.get(0).getId() : null);
	}

	public List<ProductCard> cards(List<Product> products, List<Campaign> campaigns) {
		return products.stream().map(p -> card(p, campaigns)).toList();
	}

	public VariantDto variant(ProductVariant v, Campaign camp) {
		PriceInfo pi = pricingService.price(v, camp);
		return new VariantDto(v.getId(), v.getSku(), v.getOption1(), v.getOption2(), v.getDisplayName(), pi.price(),
				pi.originalPrice(), pi.discountPercent(), v.getStockQuantity(), v.getStockQuantity() > 0, v.getImageUrl(),
				v.getWeightGram());
	}

	public ProductDetail detail(Product p, CategoryTree tree, List<Campaign> campaigns, List<ProductCard> related) {
		Campaign camp = pricingService.bestCampaign(p, campaigns);
		List<ProductVariant> vs = activeVariants(p);
		List<Crumb> crumbs = tree.pathTo(p.getCategory().getId()).stream().map(c -> new Crumb(c.getName(), c.getSlug())).toList();
		List<String> o1 = vs.stream().map(ProductVariant::getOption1).filter(x -> x != null && !x.isBlank())
				.collect(Collectors.toCollection(LinkedHashSet::new)).stream().toList();
		List<String> o2 = vs.stream().map(ProductVariant::getOption2).filter(x -> x != null && !x.isBlank())
				.collect(Collectors.toCollection(LinkedHashSet::new)).stream().toList();
		List<ImageDto> images = new ArrayList<>(p.getImages().stream().map(i -> new ImageDto(i.getUrl(), i.getAlt())).toList());
		List<FranchiseDto> frs = p.getFranchises().stream().sorted(Comparator.comparing(Franchise::getName)).map(ProductMapper::franchise).toList();
		return new ProductDetail(p.getId(), p.getName(), p.getSlug(), p.getShortDescription(), p.getDescription(), crumbs,
				p.getCategory().getId(), brand(p.getBrand()), frs, p.getAgeMinMonths(), p.getAgeMaxMonths(),
				AgeGroup.labelOf(p.getAgeMinMonths(), p.getAgeMaxMonths()), p.getGender(), p.getMaterial(), p.getBrandOrigin(),
				p.getMadeIn(), p.getManufactureYear(), p.getBattery(), p.isSmallPartsWarning(), p.getSafetyWarning(),
				p.isCrCertified(), p.getImporter(), p.getLengthCm(), p.getWidthCm(), p.getHeightCm(), p.getVideoUrl(),
				p.getOption1Name(), p.getOption2Name(), o1, o2, vs.stream().map(v -> variant(v, camp)).toList(), images,
				p.getSpecs().stream().map(s -> new SpecDto(s.getSpecKey(), s.getSpecValue())).toList(), p.getStatus(),
				p.isFeatured(), p.isOnlineExclusive(), camp == null ? null : camp.getBadgeLabel(), p.getSoldCount(),
				p.getAvgRating(), p.getReviewCount(), p.getMetaTitle(), p.getMetaDescription(), p.getCreatedAt(), related);
	}

	/** Ghi lai cac cot tong hop (gia thap/cao nhat, tong ton) sau khi bien the thay doi. */
	public static void refreshAggregates(Product p) {
		List<ProductVariant> vs = activeVariants(p);
		BigDecimal min = vs.stream().map(ProductVariant::getPrice).min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
		BigDecimal max = vs.stream().map(ProductVariant::getPrice).max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
		p.setMinPrice(min);
		p.setMaxPrice(max);
		p.setTotalStock(vs.stream().mapToInt(ProductVariant::getStockQuantity).sum());
	}

	public static Map<Long, Long> toCountMap(List<Object[]> rows) {
		return rows.stream().collect(Collectors.toMap(r -> ((Number) r[0]).longValue(), r -> ((Number) r[1]).longValue()));
	}
}
