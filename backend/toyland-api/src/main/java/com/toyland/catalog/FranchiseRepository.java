package com.toyland.catalog;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FranchiseRepository extends JpaRepository<Franchise, Long> {

	List<Franchise> findAllByOrderByNameAsc();

	Optional<Franchise> findBySlug(String slug);

	boolean existsBySlug(String slug);

	boolean existsBySlugAndIdNot(String slug, Long id);

	boolean existsByNameIgnoreCase(String name);

	boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
