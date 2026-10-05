package com.toyland.catalog.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.toyland.catalog.Gender;
import com.toyland.catalog.ProductStatus;

/** DTO cong khai cua module san pham. */
public final class CatalogDtos {

	private CatalogDtos() {
	}

	public record CategoryNode(Long id, String name, String slug, String icon, String imageUrl, String description,
			Long parentId, int sortOrder, boolean active, List<CategoryNode> children) {
	}

	public record BrandDto(Long id, String name, String slug, String logoUrl, String country, String description) {
	}

	public record FranchiseDto(Long id, String name, String slug, String imageUrl, String description) {
	}

	/** The san pham (trang danh sach, trang chu). */
	public record ProductCard(Long id, String name, String slug, String imageUrl, String hoverImageUrl, String brandName,
			String ageLabel, BigDecimal price, BigDecimal originalPrice, int discountPercent, String badge,
			BigDecimal avgRating, int reviewCount, int soldCount, boolean inStock, ProductStatus status,
			boolean featured, boolean onlineExclusive, boolean isNew, int variantCount, Long defaultVariantId) {
	}

	public record VariantDto(Long id, String sku, String option1, String option2, String name, BigDecimal price,
			BigDecimal originalPrice, int discountPercent, int stock, boolean inStock, String imageUrl, int weightGram) {
	}

	public record SpecDto(String key, String value) {
	}

	public record ImageDto(String url, String alt) {
	}

	public record Crumb(String name, String slug) {
	}

	public record RatingSummary(BigDecimal average, long total, Map<Integer, Long> counts, long withImages) {
	}

	public record ProductDetail(Long id, String name, String slug, String shortDescription, String description,
			List<Crumb> breadcrumbs, Long categoryId, BrandDto brand, List<FranchiseDto> franchises,
			int ageMinMonths, Integer ageMaxMonths, String ageLabel, Gender gender, String material, String brandOrigin,
			String madeIn, Integer manufactureYear, String battery, boolean smallPartsWarning, String safetyWarning,
			boolean crCertified, String importer, Integer lengthCm, Integer widthCm, Integer heightCm, String videoUrl,
			String option1Name, String option2Name, List<String> option1Values, List<String> option2Values,
			List<VariantDto> variants, List<ImageDto> images, List<SpecDto> specs, ProductStatus status,
			boolean featured, boolean onlineExclusive, String campaignBadge, int soldCount, BigDecimal avgRating,
			int reviewCount, String metaTitle, String metaDescription, LocalDateTime createdAt,
			List<ProductCard> related) {
	}

	public record FacetItem(String value, String label, long count) {
	}

	public record Facets(List<FacetItem> categories, List<FacetItem> brands, List<FacetItem> franchises,
			List<FacetItem> ages, List<FacetItem> genders) {
	}

	public record Suggestion(List<ProductCard> products, List<FacetItem> categories, List<FacetItem> brands) {
	}
}
