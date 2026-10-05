package com.toyland.supplier;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {

	boolean existsByCodeIgnoreCase(String code);

	boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);

	@Query("SELECT s FROM Supplier s WHERE (:active IS NULL OR s.active = :active) AND (:kw IS NULL "
			+ "OR LOWER(s.name) LIKE CONCAT('%', :kw, '%') OR LOWER(s.code) LIKE CONCAT('%', :kw, '%') "
			+ "OR s.phone LIKE CONCAT('%', :kw, '%') OR LOWER(s.contactName) LIKE CONCAT('%', :kw, '%')) ORDER BY s.active DESC, s.name ASC")
	Page<Supplier> search(@Param("kw") String keyword, @Param("active") Boolean active, Pageable pageable);

	List<Supplier> findByActiveTrueOrderByNameAsc();
}
