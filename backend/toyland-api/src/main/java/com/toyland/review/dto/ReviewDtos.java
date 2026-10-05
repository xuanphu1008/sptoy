package com.toyland.review.dto;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class ReviewDtos {

	private ReviewDtos() {
	}

	public record ReviewDto(Long id, String userName, int rating, String comment, List<String> imageUrls,
			String variantName, boolean verifiedPurchase, String adminReply, LocalDateTime repliedAt, LocalDateTime createdAt,
			boolean pendingModeration) {
	}

	public record ReviewCreateRequest(@NotNull Long orderItemId,
			@NotNull @Min(value = 1, message = "Chọn từ 1 đến 5 sao") @Max(value = 5, message = "Chọn từ 1 đến 5 sao") Integer rating,
			@Size(max = 2000, message = "Nội dung tối đa 2000 ký tự") String comment, List<String> imageUrls) {
	}

	public record AdminReviewDto(Long id, Long productId, String productName, String productSlug, String userName,
			String userEmail, int rating, String comment, List<String> imageUrls, boolean hidden, String adminReply,
			LocalDateTime createdAt, boolean flagged, String flagReason) {
	}

	public record ReplyRequest(@Size(max = 2000) String reply) {
	}

	public record KeywordRequest(@jakarta.validation.constraints.NotBlank(message = "Nhập từ khoá") @Size(max = 100) String keyword) {
	}
}
