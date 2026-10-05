package com.toyland.review;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.toyland.common.dto.PageResponse;
import com.toyland.review.dto.ReviewDtos.AdminReviewDto;
import com.toyland.review.dto.ReviewDtos.ReplyRequest;
import com.toyland.review.dto.ReviewDtos.ReviewCreateRequest;
import com.toyland.review.dto.ReviewDtos.ReviewDto;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "13. Đánh giá sản phẩm")
@RestController
public class ReviewController {

	private final ReviewService service;

	public ReviewController(ReviewService service) {
		this.service = service;
	}

	@GetMapping("/api/v1/products/{productId}/reviews")
	public PageResponse<ReviewDto> list(@PathVariable Long productId, @RequestParam(required = false) Integer rating,
			@RequestParam(defaultValue = "false") boolean withImages, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "5") int size) {
		return service.list(productId, rating, withImages, page, size);
	}

	@PostMapping("/api/v1/reviews")
	@ResponseStatus(HttpStatus.CREATED)
	public ReviewDto create(@Valid @RequestBody ReviewCreateRequest r) {
		return service.create(r);
	}

	@GetMapping("/api/v1/admin/reviews")
	public PageResponse<AdminReviewDto> adminList(@RequestParam(required = false) Integer rating,
			@RequestParam(required = false) Boolean hidden, @RequestParam(required = false) Boolean flagged,
			@RequestParam(required = false) String keyword, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		return service.adminList(rating, hidden, flagged, keyword, page, size);
	}

	/** Duyet danh gia bi an do chua tu khoa cam. */
	@PutMapping("/api/v1/admin/reviews/{id}/approve")
	public AdminReviewDto approve(@PathVariable Long id) {
		return service.approve(id);
	}

	@PutMapping("/api/v1/admin/reviews/{id}/visibility")
	public AdminReviewDto visibility(@PathVariable Long id, @RequestParam boolean hidden) {
		return service.setHidden(id, hidden);
	}

	@PutMapping("/api/v1/admin/reviews/{id}/reply")
	public AdminReviewDto reply(@PathVariable Long id, @Valid @RequestBody ReplyRequest r) {
		return service.reply(id, r.reply());
	}
}
