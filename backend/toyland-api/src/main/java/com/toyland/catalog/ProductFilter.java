package com.toyland.catalog;

import java.math.BigDecimal;
import java.util.List;

/**
 * Tieu chi tim kiem san pham.
 * publicOnly = true: chi san pham dang ban / sap ve (trang cua hang).
 */
public record ProductFilter(String keyword, List<Long> categoryIds, List<String> brandSlugs, List<String> franchiseSlugs,
		List<AgeGroup> ages, List<Gender> genders, BigDecimal minPrice, BigDecimal maxPrice, Boolean inStock,
		Boolean onSale, Boolean featured, List<ProductStatus> statuses, boolean publicOnly, SaleScope saleScope) {

	/** Pham vi cac chuong trinh giam gia dang chay (de loc "dang giam gia"). */
	public record SaleScope(boolean all, List<Long> categoryIds, List<Long> brandIds, List<Long> franchiseIds,
			List<Long> productIds) {
	}

	public ProductFilter withoutCategories() {
		return new ProductFilter(keyword, null, brandSlugs, franchiseSlugs, ages, genders, minPrice, maxPrice, inStock,
				onSale, featured, statuses, publicOnly, saleScope);
	}

	public ProductFilter withoutBrands() {
		return new ProductFilter(keyword, categoryIds, null, franchiseSlugs, ages, genders, minPrice, maxPrice, inStock,
				onSale, featured, statuses, publicOnly, saleScope);
	}

	public ProductFilter withoutFranchises() {
		return new ProductFilter(keyword, categoryIds, brandSlugs, null, ages, genders, minPrice, maxPrice, inStock,
				onSale, featured, statuses, publicOnly, saleScope);
	}

	public ProductFilter withAges(List<AgeGroup> newAges) {
		return new ProductFilter(keyword, categoryIds, brandSlugs, franchiseSlugs, newAges, genders, minPrice, maxPrice,
				inStock, onSale, featured, statuses, publicOnly, saleScope);
	}

	public ProductFilter withoutGenders() {
		return new ProductFilter(keyword, categoryIds, brandSlugs, franchiseSlugs, ages, null, minPrice, maxPrice, inStock,
				onSale, featured, statuses, publicOnly, saleScope);
	}
}
