package com.toyland.catalog;

import java.util.Collection;
import java.util.List;
import java.math.BigDecimal;
import java.util.Optional;

/**
 * Giao dien cong bo cua mien Danh muc - San pham - Kho cho cac mien khac (gio hang, yeu thich,
 * khuyen mai, bao cao). Mien khac khong dung truc tiep repository cua catalog.
 */
public interface CatalogApi {

	Optional<ProductVariant> findVariantWithProduct(Long variantId);

	List<ProductVariant> findVariantsWithProduct(Collection<Long> variantIds);

	/** San pham chua bi xoa mem. */
	Optional<Product> findProduct(Long productId);

	Optional<String> categoryName(Long id);

	Optional<String> brandName(Long id);

	Optional<String> franchiseName(Long id);

	Optional<String> productName(Long id);

	long countActiveProducts();

	long countLowStock(int threshold);

	List<ProductVariant> findLowStock(int threshold, int limit);

	/** Hinh chieu "an toan" cua mot bien the cho doi tac ben ngoai: khong lo gia von, ma noi bo. */
	record SellableItem(String sku, Long productId, String productName, String variantName, String category, String brand,
			BigDecimal price, BigDecimal compareAtPrice, int stock, String imageUrl) {
	}

	record SellablePage(java.util.List<SellableItem> items, int page, int size, long totalElements, int totalPages) {
	}

	SellablePage sellableItems(int page, int size);

	Optional<SellableItem> sellableBySku(String sku);

	/** Bien the dang ban theo SKU (entity day du) de mien Don hang tru kho khi doi tac dat hang. */
	Optional<ProductVariant> findSellableVariant(String sku);
}
