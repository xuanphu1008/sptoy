package com.toyland.inventory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

	@Query(value = "SELECT m FROM StockMovement m WHERE (:variantId IS NULL OR m.variant.id = :variantId) "
			+ "AND (:type IS NULL OR m.type = :type) AND (:kw IS NULL OR LOWER(m.sku) LIKE CONCAT('%', :kw, '%') OR LOWER(m.refCode) LIKE CONCAT('%', :kw, '%')) "
			+ "ORDER BY m.id DESC",
			countQuery = "SELECT COUNT(m) FROM StockMovement m WHERE (:variantId IS NULL OR m.variant.id = :variantId) "
			+ "AND (:type IS NULL OR m.type = :type) AND (:kw IS NULL OR LOWER(m.sku) LIKE CONCAT('%', :kw, '%') OR LOWER(m.refCode) LIKE CONCAT('%', :kw, '%'))")
	Page<StockMovement> search(@Param("variantId") Long variantId, @Param("type") MovementType type,
			@Param("kw") String keyword, Pageable pageable);
}
