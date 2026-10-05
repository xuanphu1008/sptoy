package com.toyland.catalog;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.*;

@Entity
@Table(name = "product_specs")
public class ProductSpec {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "product_id", nullable = false)
	private Product product;

	@Column(name = "spec_key", nullable = false, length = 100)
	private String specKey;

	@Column(name = "spec_value", nullable = false, length = 255)
	private String specValue;

	@Column(name = "sort_order", nullable = false)
	private int sortOrder;

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }
	public Product getProduct() { return product; }
	public void setProduct(Product product) { this.product = product; }
	public String getSpecKey() { return specKey; }
	public void setSpecKey(String specKey) { this.specKey = specKey; }
	public String getSpecValue() { return specValue; }
	public void setSpecValue(String specValue) { this.specValue = specValue; }
	public int getSortOrder() { return sortOrder; }
	public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }
}
