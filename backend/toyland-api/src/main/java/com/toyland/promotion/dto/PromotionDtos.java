package com.toyland.promotion.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.toyland.promotion.CampaignScope;
import com.toyland.promotion.VoucherType;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public final class PromotionDtos {

	private PromotionDtos() {
	}

	public record BannerDto(Long id, String title, String subtitle, String imageUrl, String linkUrl, String buttonText,
			String bgColor, int sortOrder, boolean active, LocalDateTime startAt, LocalDateTime endAt) {
	}

	public record BannerRequest(@NotBlank(message = "Vui lòng nhập tiêu đề") @Size(max = 150) String title,
			@Size(max = 300) String subtitle, @Size(max = 500) String imageUrl, @Size(max = 300) String linkUrl,
			@Size(max = 50) String buttonText, @Size(max = 20) String bgColor, int sortOrder, boolean active,
			LocalDateTime startAt, LocalDateTime endAt) {
	}

	public record VoucherDto(Long id, String code, String name, String description, VoucherType type, BigDecimal value,
			BigDecimal maxDiscount, BigDecimal minOrderValue, LocalDateTime startAt, LocalDateTime endAt, Integer usageLimit,
			int usedCount, int perUserLimit, boolean active, String summary) {
	}

	public record VoucherRequest(
			@NotBlank(message = "Vui lòng nhập mã") @Pattern(regexp = "^[A-Za-z0-9_-]{3,30}$", message = "Mã gồm 3-30 ký tự chữ/số") String code,
			@NotBlank(message = "Vui lòng nhập tên") @Size(max = 150) String name, @Size(max = 500) String description,
			@NotNull VoucherType type, @NotNull @PositiveOrZero BigDecimal value, @PositiveOrZero BigDecimal maxDiscount,
			@NotNull @PositiveOrZero BigDecimal minOrderValue, @NotNull LocalDateTime startAt, @NotNull LocalDateTime endAt,
			@Min(1) Integer usageLimit, @Min(1) int perUserLimit, boolean active) {
	}

	public record CampaignDto(Long id, String name, String description, int discountPercent, CampaignScope scopeType,
			Long scopeId, String scopeName, String badgeLabel, LocalDateTime startAt, LocalDateTime endAt, boolean active,
			boolean running) {
	}

	public record CampaignRequest(@NotBlank(message = "Vui lòng nhập tên") @Size(max = 150) String name,
			@Size(max = 500) String description,
			@Min(value = 1, message = "Giảm từ 1%") @Max(value = 90, message = "Giảm tối đa 90%") int discountPercent,
			@NotNull CampaignScope scopeType, Long scopeId, @Size(max = 50) String badgeLabel,
			@NotNull LocalDateTime startAt, @NotNull LocalDateTime endAt, boolean active) {
	}
}
