package com.toyland.catalog;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

	boolean existsBySku(String sku);

	boolean existsBySkuAndIdNot(String sku, Long id);

	@Query("SELECT v FROM ProductVariant v JOIN FETCH v.product p WHERE v.id IN :ids")
	List<ProductVariant> findAllWithProduct(@Param("ids") Collection<Long> ids);

	@Query("SELECT v FROM ProductVariant v JOIN FETCH v.product p WHERE v.id = :id")
	Optional<ProductVariant> findWithProduct(@Param("id") Long id);

	/** Danh sach ton kho cho trang quan ly kho. */
	@Query(value = "SELECT v FROM ProductVariant v JOIN FETCH v.product p WHERE p.deleted = false "
			+ "AND (:kw IS NULL OR p.searchText LIKE CONCAT('%', :kw, '%') OR LOWER(v.sku) LIKE CONCAT('%', :kw, '%')) "
			+ "AND (:threshold IS NULL OR v.stockQuantity <= :threshold) AND v.active = true",
			countQuery = "SELECT COUNT(v) FROM ProductVariant v JOIN v.product p WHERE p.deleted = false "
			+ "AND (:kw IS NULL OR p.searchText LIKE CONCAT('%', :kw, '%') OR LOWER(v.sku) LIKE CONCAT('%', :kw, '%')) "
			+ "AND (:threshold IS NULL OR v.stockQuantity <= :threshold) AND v.active = true")
	Page<ProductVariant> searchInventory(@Param("kw") String keyword, @Param("threshold") Integer threshold, Pageable pageable);

	@Query("SELECT COUNT(v) FROM ProductVariant v WHERE v.active = true AND v.product.deleted = false "
			+ "AND v.product.status = com.toyland.catalog.ProductStatus.ACTIVE AND v.stockQuantity <= :threshold")
	long countLowStock(@Param("threshold") int threshold);

	@Query("SELECT v FROM ProductVariant v JOIN FETCH v.product p WHERE v.active = true AND p.deleted = false "
			+ "AND p.status = com.toyland.catalog.ProductStatus.ACTIVE AND v.stockQuantity <= :threshold ORDER BY v.stockQuantity ASC")
	List<ProductVariant> findLowStock(@Param("threshold") int threshold, Pageable pageable);

	/** Bien the dang ban (san pham ACTIVE, chua xoa) cho API doi tac. */
	@Query(value = "SELECT v FROM ProductVariant v JOIN FETCH v.product p WHERE v.active = true AND p.deleted = false "
			+ "AND p.status = com.toyland.catalog.ProductStatus.ACTIVE",
			countQuery = "SELECT COUNT(v) FROM ProductVariant v JOIN v.product p WHERE v.active = true AND p.deleted = false "
			+ "AND p.status = com.toyland.catalog.ProductStatus.ACTIVE")
	Page<ProductVariant> findSellable(Pageable pageable);

	@Query("SELECT v FROM ProductVariant v JOIN FETCH v.product p WHERE v.sku = :sku AND v.active = true AND p.deleted = false "
			+ "AND p.status = com.toyland.catalog.ProductStatus.ACTIVE")
	Optional<ProductVariant> findSellableBySku(@Param("sku") String sku);
}
