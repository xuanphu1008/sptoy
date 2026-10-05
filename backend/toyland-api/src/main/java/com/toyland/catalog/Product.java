package com.toyland.catalog;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.*;

/** San pham (thong tin chung). Gia/ton kho nam o ProductVariant. */
/** DynamicUpdate: chi UPDATE cot thay doi -> giam ghi de lan nhau khi cap nhat dong thoi (ton kho, rating...). */
@Entity
@org.hibernate.annotations.DynamicUpdate
@Table(name = "products")
public class Product {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "name", nullable = false, length = 200)
	private String name;

	@Column(name = "slug", nullable = false, length = 220)
	private String slug;

	@Column(name = "short_description", length = 500)
	private String shortDescription;

	@Column(name = "description", columnDefinition = "TEXT")
	private String description;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "category_id", nullable = false)
	private Category category;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "brand_id")
	private Brand brand;

	@Column(name = "age_min_months", nullable = false)
	private int ageMinMonths;

	@Column(name = "age_max_months")
	private Integer ageMaxMonths;

	@Enumerated(EnumType.STRING)
	@Column(name = "gender", nullable = false, length = 10)
	private Gender gender = Gender.UNISEX;

	@Column(name = "material", length = 100)
	private String material;

	@Column(name = "brand_origin", length = 100)
	private String brandOrigin;

	@Column(name = "made_in", length = 100)
	private String madeIn;

	@Column(name = "manufacture_year")
	private Integer manufactureYear;

	@Column(name = "battery", length = 100)
	private String battery;

	@Column(name = "small_parts_warning", nullable = false)
	private boolean smallPartsWarning;

	@Column(name = "safety_warning", length = 500)
	private String safetyWarning;

	@Column(name = "cr_certified", nullable = false)
	private boolean crCertified = true;

	@Column(name = "importer", length = 255)
	private String importer;

	@Column(name = "length_cm", nullable = false)
	private int lengthCm = 20;

	@Column(name = "width_cm", nullable = false)
	private int widthCm = 15;

	@Column(name = "height_cm", nullable = false)
	private int heightCm = 10;

	@Column(name = "video_url", length = 500)
	private String videoUrl;

	@Column(name = "option1_name", length = 50)
	private String option1Name;

	@Column(name = "option2_name", length = 50)
	private String option2Name;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 20)
	private ProductStatus status = ProductStatus.ACTIVE;

	@Column(name = "featured", nullable = false)
	private boolean featured;

	@Column(name = "online_exclusive", nullable = false)
	private boolean onlineExclusive;

	@Column(name = "min_price", nullable = false, precision = 15, scale = 2)
	private BigDecimal minPrice = BigDecimal.ZERO;

	@Column(name = "max_price", nullable = false, precision = 15, scale = 2)
	private BigDecimal maxPrice = BigDecimal.ZERO;

	@Column(name = "total_stock", nullable = false)
	private int totalStock;

	@Column(name = "sold_count", nullable = false)
	private int soldCount;

	@Column(name = "avg_rating", nullable = false, precision = 3, scale = 2)
	private BigDecimal avgRating = BigDecimal.ZERO;

	@Column(name = "review_count", nullable = false)
	private int reviewCount;

	@Column(name = "search_text", length = 1000)
	private String searchText;

	@Column(name = "meta_title", length = 200)
	private String metaTitle;

	@Column(name = "meta_description", length = 500)
	private String metaDescription;

	@Column(name = "deleted", nullable = false)
	private boolean deleted;

	@ManyToMany
	@JoinTable(name = "product_franchises", joinColumns = @JoinColumn(name = "product_id"), inverseJoinColumns = @JoinColumn(name = "franchise_id"))
	private Set<Franchise> franchises = new HashSet<>();

	@OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
	@OrderBy("sortOrder ASC, id ASC")
	private List<ProductVariant> variants = new ArrayList<>();

	@OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("sortOrder ASC, id ASC")
	private List<ProductImage> images = new ArrayList<>();

	@OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("sortOrder ASC, id ASC")
	private List<ProductSpec> specs = new ArrayList<>();

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	@PrePersist
	protected void onCreate() {
		if (createdAt == null) {
			createdAt = LocalDateTime.now();
		}
		updatedAt = LocalDateTime.now();
	}

	@PreUpdate
	protected void onUpdate() {
		updatedAt = LocalDateTime.now();
	}

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	public String getSlug() { return slug; }
	public void setSlug(String slug) { this.slug = slug; }
	public String getShortDescription() { return shortDescription; }
	public void setShortDescription(String shortDescription) { this.shortDescription = shortDescription; }
	public String getDescription() { return description; }
	public void setDescription(String description) { this.description = description; }
	public Category getCategory() { return category; }
	public void setCategory(Category category) { this.category = category; }
	public Brand getBrand() { return brand; }
	public void setBrand(Brand brand) { this.brand = brand; }
	public int getAgeMinMonths() { return ageMinMonths; }
	public void setAgeMinMonths(int ageMinMonths) { this.ageMinMonths = ageMinMonths; }
	public Integer getAgeMaxMonths() { return ageMaxMonths; }
	public void setAgeMaxMonths(Integer ageMaxMonths) { this.ageMaxMonths = ageMaxMonths; }
	public Gender getGender() { return gender; }
	public void setGender(Gender gender) { this.gender = gender; }
	public String getMaterial() { return material; }
	public void setMaterial(String material) { this.material = material; }
	public String getBrandOrigin() { return brandOrigin; }
	public void setBrandOrigin(String brandOrigin) { this.brandOrigin = brandOrigin; }
	public String getMadeIn() { return madeIn; }
	public void setMadeIn(String madeIn) { this.madeIn = madeIn; }
	public Integer getManufactureYear() { return manufactureYear; }
	public void setManufactureYear(Integer manufactureYear) { this.manufactureYear = manufactureYear; }
	public String getBattery() { return battery; }
	public void setBattery(String battery) { this.battery = battery; }
	public boolean isSmallPartsWarning() { return smallPartsWarning; }
	public void setSmallPartsWarning(boolean smallPartsWarning) { this.smallPartsWarning = smallPartsWarning; }
	public String getSafetyWarning() { return safetyWarning; }
	public void setSafetyWarning(String safetyWarning) { this.safetyWarning = safetyWarning; }
	public boolean isCrCertified() { return crCertified; }
	public void setCrCertified(boolean crCertified) { this.crCertified = crCertified; }
	public String getImporter() { return importer; }
	public void setImporter(String importer) { this.importer = importer; }
	public int getLengthCm() { return lengthCm; }
	public void setLengthCm(int lengthCm) { this.lengthCm = lengthCm; }
	public int getWidthCm() { return widthCm; }
	public void setWidthCm(int widthCm) { this.widthCm = widthCm; }
	public int getHeightCm() { return heightCm; }
	public void setHeightCm(int heightCm) { this.heightCm = heightCm; }
	public String getVideoUrl() { return videoUrl; }
	public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }
	public String getOption1Name() { return option1Name; }
	public void setOption1Name(String option1Name) { this.option1Name = option1Name; }
	public String getOption2Name() { return option2Name; }
	public void setOption2Name(String option2Name) { this.option2Name = option2Name; }
	public ProductStatus getStatus() { return status; }
	public void setStatus(ProductStatus status) { this.status = status; }
	public boolean isFeatured() { return featured; }
	public void setFeatured(boolean featured) { this.featured = featured; }
	public boolean isOnlineExclusive() { return onlineExclusive; }
	public void setOnlineExclusive(boolean onlineExclusive) { this.onlineExclusive = onlineExclusive; }
	public BigDecimal getMinPrice() { return minPrice; }
	public void setMinPrice(BigDecimal minPrice) { this.minPrice = minPrice; }
	public BigDecimal getMaxPrice() { return maxPrice; }
	public void setMaxPrice(BigDecimal maxPrice) { this.maxPrice = maxPrice; }
	public int getTotalStock() { return totalStock; }
	public void setTotalStock(int totalStock) { this.totalStock = totalStock; }
	public int getSoldCount() { return soldCount; }
	public void setSoldCount(int soldCount) { this.soldCount = soldCount; }
	public BigDecimal getAvgRating() { return avgRating; }
	public void setAvgRating(BigDecimal avgRating) { this.avgRating = avgRating; }
	public int getReviewCount() { return reviewCount; }
	public void setReviewCount(int reviewCount) { this.reviewCount = reviewCount; }
	public String getSearchText() { return searchText; }
	public void setSearchText(String searchText) { this.searchText = searchText; }
	public String getMetaTitle() { return metaTitle; }
	public void setMetaTitle(String metaTitle) { this.metaTitle = metaTitle; }
	public String getMetaDescription() { return metaDescription; }
	public void setMetaDescription(String metaDescription) { this.metaDescription = metaDescription; }
	public boolean isDeleted() { return deleted; }
	public void setDeleted(boolean deleted) { this.deleted = deleted; }
	public Set<Franchise> getFranchises() { return franchises; }
	public void setFranchises(Set<Franchise> franchises) { this.franchises = franchises; }
	public List<ProductVariant> getVariants() { return variants; }
	public void setVariants(List<ProductVariant> variants) { this.variants = variants; }
	public List<ProductImage> getImages() { return images; }
	public void setImages(List<ProductImage> images) { this.images = images; }
	public List<ProductSpec> getSpecs() { return specs; }
	public void setSpecs(List<ProductSpec> specs) { this.specs = specs; }
	public LocalDateTime getCreatedAt() { return createdAt; }
	public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
	public LocalDateTime getUpdatedAt() { return updatedAt; }
	public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
