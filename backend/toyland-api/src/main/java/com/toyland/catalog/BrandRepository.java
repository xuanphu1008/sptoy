package com.toyland.catalog;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BrandRepository extends JpaRepository<Brand, Long> {

	List<Brand> findAllByOrderByNameAsc();

	Optional<Brand> findBySlug(String slug);

	boolean existsBySlug(String slug);

	boolean existsBySlugAndIdNot(String slug, Long id);

	boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

	boolean existsByNameIgnoreCase(String name);
}
