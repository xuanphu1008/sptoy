package com.toyland.catalog;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

	Optional<Product> findBySlugAndDeletedFalse(String slug);

	Optional<Product> findByIdAndDeletedFalse(Long id);

	boolean existsBySlug(String slug);

	boolean existsBySlugAndIdNot(String slug, Long id);

	@Query("SELECT COUNT(p) FROM Product p WHERE p.deleted = false AND p.category.id IN :ids")
	long countByCategoryIds(@Param("ids") Collection<Long> ids);

	/** Dem ca san pham da xoa mem (van giu khoa ngoai) - dung khi kiem tra truoc khi xoa danh muc/thuong hieu. */
	@Query("SELECT COUNT(p) FROM Product p WHERE p.category.id = :id")
	long countAnyByCategory(@Param("id") Long id);

	@Query("SELECT COUNT(p) FROM Product p WHERE p.brand.id = :id")
	long countAnyByBrand(@Param("id") Long id);

	@Query("SELECT COUNT(p) FROM Product p WHERE p.deleted = false AND p.brand.id = :id")
	long countByBrand(@Param("id") Long id);

	@Query("SELECT COUNT(p) FROM Product p JOIN p.franchises f WHERE p.deleted = false AND f.id = :id")
	long countByFranchise(@Param("id") Long id);

	@Query("SELECT COUNT(p) FROM Product p WHERE p.deleted = false AND p.status = com.toyland.catalog.ProductStatus.ACTIVE")
	long countActive();

	@Query("SELECT p FROM Product p WHERE p.deleted = false AND p.status = com.toyland.catalog.ProductStatus.ACTIVE "
			+ "AND p.category.id IN :catIds AND p.id <> :excludeId ORDER BY p.soldCount DESC, p.id DESC")
	List<Product> findRelated(@Param("catIds") Collection<Long> catIds, @Param("excludeId") Long excludeId, Pageable pageable);

	@Query("SELECT p FROM Product p WHERE p.id IN :ids AND p.deleted = false")
	List<Product> findAllActiveByIds(@Param("ids") Collection<Long> ids);
}
