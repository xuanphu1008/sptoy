package com.toyland.catalog.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.toyland.catalog.Gender;
import com.toyland.catalog.ProductStatus;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** DTO trang quan tri san pham. */
public final class AdminCatalogDtos {

	private AdminCatalogDtos() {
	}

	public record CategoryRequest(@NotBlank(message = "Vui lòng nhập tên danh mục") @Size(max = 100) String name,
			@Size(max = 120) String slug, Long parentId, @Size(max = 500) String description, @Size(max = 500) String imageUrl,
			@Size(max = 50) String icon, int sortOrder, boolean active) {
	}

	public record BrandRequest(@NotBlank(message = "Vui lòng nhập tên thương hiệu") @Size(max = 100) String name,
			@Size(max = 120) String slug, @Size(max = 500) String logoUrl, @Size(max = 100) String country,
			@Size(max = 500) String description) {
	}

	public record FranchiseRequest(@NotBlank(message = "Vui lòng nhập tên nhân vật/chủ đề") @Size(max = 100) String name,
			@Size(max = 120) String slug, @Size(max = 500) String imageUrl, @Size(max = 500) String description) {
	}

	public record SpecRequest(@NotBlank @Size(max = 100) String key, @NotBlank @Size(max = 255) String value) {
	}

	public record VariantRequest(Long id,
			@NotBlank(message = "SKU không được để trống") @Size(max = 60) String sku, @Size(max = 60) String barcode,
			@Size(max = 100) String option1, @Size(max = 100) String option2,
			@NotNull(message = "Nhập giá bán") @DecimalMin(value = "0", message = "Giá phải >= 0") BigDecimal price,
			@DecimalMin("0") BigDecimal compareAtPrice, @DecimalMin("0") BigDecimal costPrice,
			@Min(value = 0, message = "Tồn ban đầu phải >= 0") Integer initialStock,
			@Min(value = 1, message = "Khối lượng tối thiểu 1g") int weightGram, @Size(max = 500) String imageUrl,
			boolean active) {
	}

	public record ProductUpsertRequest(
			@NotBlank(message = "Vui lòng nhập tên sản phẩm") @Size(max = 200) String name, @Size(max = 220) String slug,
			@Size(max = 500) String shortDescription, String description,
			@NotNull(message = "Vui lòng chọn danh mục") Long categoryId, Long brandId, List<Long> franchiseIds,
			@Min(0) @Max(240) int ageMinMonths, @Min(0) @Max(240) Integer ageMaxMonths, @NotNull Gender gender,
			@Size(max = 100) String material, @Size(max = 100) String brandOrigin, @Size(max = 100) String madeIn,
			Integer manufactureYear, @Size(max = 100) String battery, boolean smallPartsWarning,
			@Size(max = 500) String safetyWarning, boolean crCertified, @Size(max = 255) String importer,
			@Min(1) int lengthCm, @Min(1) int widthCm, @Min(1) int heightCm, @Size(max = 500) String videoUrl,
			@NotNull ProductStatus status, boolean featured, boolean onlineExclusive,
			@Size(max = 200) String metaTitle, @Size(max = 500) String metaDescription,
			@Size(max = 50) String option1Name, @Size(max = 50) String option2Name,
			List<@Valid SpecRequest> specs, List<String> images,
			@NotEmpty(message = "Sản phẩm cần ít nhất 1 biến thể") List<@Valid VariantRequest> variants) {
	}

	public record AdminVariantDto(Long id, String sku, String barcode, String option1, String option2, BigDecimal price,
			BigDecimal compareAtPrice, BigDecimal costPrice, int stock, int weightGram, String imageUrl, boolean active) {
	}

	public record AdminProductDto(Long id, String name, String slug, String shortDescription, String description,
			Long categoryId, Long brandId, List<Long> franchiseIds, int ageMinMonths, Integer ageMaxMonths, Gender gender,
			String material, String brandOrigin, String madeIn, Integer manufactureYear, String battery,
			boolean smallPartsWarning, String safetyWarning, boolean crCertified, String importer, int lengthCm,
			int widthCm, int heightCm, String videoUrl, ProductStatus status, boolean featured, boolean onlineExclusive,
			String metaTitle, String metaDescription, String option1Name, String option2Name, List<SpecRequest> specs,
			List<String> images, List<AdminVariantDto> variants, LocalDateTime createdAt, LocalDateTime updatedAt) {
	}

	public record AdminProductRow(Long id, String name, String slug, String imageUrl, String categoryName,
			String brandName, BigDecimal minPrice, BigDecimal maxPrice, int totalStock, int variantCount, int soldCount,
			BigDecimal avgRating, ProductStatus status, boolean featured, LocalDateTime updatedAt) {
	}
}
