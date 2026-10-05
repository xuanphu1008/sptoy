package com.toyland.catalog;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CategoryRepository extends JpaRepository<Category, Long> {

	@Query("SELECT c FROM Category c LEFT JOIN FETCH c.parent ORDER BY c.sortOrder ASC, c.id ASC")
	List<Category> findAllOrdered();

	Optional<Category> findBySlug(String slug);

	boolean existsBySlugAndIdNot(String slug, Long id);

	boolean existsBySlug(String slug);

	@Query("SELECT COUNT(c) FROM Category c WHERE c.parent.id = :id")
	long countChildren(@Param("id") Long id);
}
