package com.toyland.review;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
class ReviewStatsApiImpl implements ReviewStatsApi {

	private final ReviewRepository repo;

	ReviewStatsApiImpl(ReviewRepository repo) {
		this.repo = repo;
	}

	@Override
	public List<Object[]> countByRating(Long productId) {
		return repo.countByRating(productId);
	}

	@Override
	public long countWithImages(Long productId) {
		return repo.countWithImages(productId);
	}
}
