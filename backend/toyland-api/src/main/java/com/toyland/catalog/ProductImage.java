package com.toyland.catalog;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.*;

@Entity
@Table(name = "product_images")
public class ProductImage {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "product_id", nullable = false)
	private Product product;

	@Column(name = "url", nullable = false, length = 500)
	private String url;

	@Column(name = "alt", length = 200)
	private String alt;

	@Column(name = "sort_order", nullable = false)
	private int sortOrder;

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }
	public Product getProduct() { return product; }
	public void setProduct(Product product) { this.product = product; }
	public String getUrl() { return url; }
	public void setUrl(String url) { this.url = url; }
	public String getAlt() { return alt; }
	public void setAlt(String alt) { this.alt = alt; }
	public int getSortOrder() { return sortOrder; }
	public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }
}
