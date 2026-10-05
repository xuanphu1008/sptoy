package com.toyland.promotion;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BannerRepository extends JpaRepository<Banner, Long> {

	List<Banner> findAllByOrderBySortOrderAscIdAsc();

	@Query("SELECT b FROM Banner b WHERE b.active = true AND (b.startAt IS NULL OR b.startAt <= :now) "
			+ "AND (b.endAt IS NULL OR b.endAt >= :now) ORDER BY b.sortOrder ASC, b.id ASC")
	List<Banner> findVisible(@Param("now") LocalDateTime now);
}
