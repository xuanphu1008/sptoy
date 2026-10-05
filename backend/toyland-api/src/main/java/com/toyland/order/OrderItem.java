package com.toyland.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.*;

import com.toyland.catalog.Product;
import com.toyland.catalog.ProductVariant;

@Entity
@Table(name = "order_items")
public class OrderItem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "order_id", nullable = false)
	private Order order;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "product_id")
	private Product product;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "variant_id")
	private ProductVariant variant;

	@Column(name = "product_name", nullable = false, length = 200)
	private String productName;

	@Column(name = "variant_name", length = 200)
	private String variantName;

	@Column(name = "sku", nullable = false, length = 60)
	private String sku;

	@Column(name = "image_url", length = 500)
	private String imageUrl;

	@Column(name = "unit_price", nullable = false, precision = 15, scale = 2)
	private BigDecimal unitPrice;

	@Column(name = "original_price", precision = 15, scale = 2)
	private BigDecimal originalPrice;

	/** Gia von luc ban (bao cao loi nhuan gop). */
	@Column(name = "cost_price", precision = 15, scale = 2)
	private BigDecimal costPrice;

	@Column(name = "quantity", nullable = false)
	private int quantity;

	@Column(name = "reviewed", nullable = false)
	private boolean reviewed;

	public BigDecimal getLineTotal() {
		return unitPrice.multiply(BigDecimal.valueOf(quantity));
	}

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }
	public Order getOrder() { return order; }
	public void setOrder(Order order) { this.order = order; }
	public Product getProduct() { return product; }
	public void setProduct(Product product) { this.product = product; }
	public ProductVariant getVariant() { return variant; }
	public void setVariant(ProductVariant variant) { this.variant = variant; }
	public String getProductName() { return productName; }
	public void setProductName(String productName) { this.productName = productName; }
	public String getVariantName() { return variantName; }
	public void setVariantName(String variantName) { this.variantName = variantName; }
	public String getSku() { return sku; }
	public void setSku(String sku) { this.sku = sku; }
	public String getImageUrl() { return imageUrl; }
	public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
	public BigDecimal getUnitPrice() { return unitPrice; }
	public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
	public BigDecimal getOriginalPrice() { return originalPrice; }
	public void setOriginalPrice(BigDecimal originalPrice) { this.originalPrice = originalPrice; }
	public int getQuantity() { return quantity; }
	public void setQuantity(int quantity) { this.quantity = quantity; }
	public boolean isReviewed() { return reviewed; }
	public void setReviewed(boolean reviewed) { this.reviewed = reviewed; }
	public BigDecimal getCostPrice() { return costPrice; }
	public void setCostPrice(BigDecimal costPrice) { this.costPrice = costPrice; }
}
