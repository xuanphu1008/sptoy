package com.toyland.review;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BannedKeywordRepository extends JpaRepository<BannedKeyword, Long> {

	List<BannedKeyword> findAllByOrderByKeywordAsc();

	boolean existsByKeyword(String keyword);
}
