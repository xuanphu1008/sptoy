package com.toyland.catalog;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.*;

/** Bien the san pham: moi to hop option1/option2 co SKU, gia, ton kho, anh rieng. */
@Entity
@Table(name = "product_variants")
public class ProductVariant {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "product_id", nullable = false)
	private Product product;

	@Column(name = "sku", nullable = false, length = 60)
	private String sku;

	@Column(name = "barcode", length = 60)
	private String barcode;

	@Column(name = "option1", length = 100)
	private String option1;

	@Column(name = "option2", length = 100)
	private String option2;

	@Column(name = "price", nullable = false, precision = 15, scale = 2)
	private BigDecimal price;

	@Column(name = "compare_at_price", precision = 15, scale = 2)
	private BigDecimal compareAtPrice;

	@Column(name = "cost_price", precision = 15, scale = 2)
	private BigDecimal costPrice;

	@Column(name = "stock_quantity", nullable = false)
	private int stockQuantity;

	@Column(name = "weight_gram", nullable = false)
	private int weightGram = 500;

	@Column(name = "image_url", length = 500)
	private String imageUrl;

	@Column(name = "is_default", nullable = false)
	private boolean defaultVariant;

	@Column(name = "active", nullable = false)
	private boolean active = true;

	@Column(name = "sort_order", nullable = false)
	private int sortOrder;

	@Version
	@Column(name = "version", nullable = false)
	private int version;

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

	/** Ten hien thi bien the, vd "80 cm / Hong". Null neu la bien the mac dinh khong co thuoc tinh. */
	public String getDisplayName() {
		if (option1 == null || option1.isBlank()) {
			return null;
		}
		return (option2 == null || option2.isBlank()) ? option1 : option1 + " / " + option2;
	}

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }
	public Product getProduct() { return product; }
	public void setProduct(Product product) { this.product = product; }
	public String getSku() { return sku; }
	public void setSku(String sku) { this.sku = sku; }
	public String getBarcode() { return barcode; }
	public void setBarcode(String barcode) { this.barcode = barcode; }
	public String getOption1() { return option1; }
	public void setOption1(String option1) { this.option1 = option1; }
	public String getOption2() { return option2; }
	public void setOption2(String option2) { this.option2 = option2; }
	public BigDecimal getPrice() { return price; }
	public void setPrice(BigDecimal price) { this.price = price; }
	public BigDecimal getCompareAtPrice() { return compareAtPrice; }
	public void setCompareAtPrice(BigDecimal compareAtPrice) { this.compareAtPrice = compareAtPrice; }
	public BigDecimal getCostPrice() { return costPrice; }
	public void setCostPrice(BigDecimal costPrice) { this.costPrice = costPrice; }
	public int getStockQuantity() { return stockQuantity; }
	public void setStockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; }
	public int getWeightGram() { return weightGram; }
	public void setWeightGram(int weightGram) { this.weightGram = weightGram; }
	public String getImageUrl() { return imageUrl; }
	public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
	public boolean isDefaultVariant() { return defaultVariant; }
	public void setDefaultVariant(boolean defaultVariant) { this.defaultVariant = defaultVariant; }
	public boolean isActive() { return active; }
	public void setActive(boolean active) { this.active = active; }
	public int getSortOrder() { return sortOrder; }
	public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }
	public int getVersion() { return version; }
	public void setVersion(int version) { this.version = version; }
	public LocalDateTime getCreatedAt() { return createdAt; }
	public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
	public LocalDateTime getUpdatedAt() { return updatedAt; }
	public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
