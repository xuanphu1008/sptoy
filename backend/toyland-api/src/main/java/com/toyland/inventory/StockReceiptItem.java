package com.toyland.inventory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.*;

import com.toyland.catalog.ProductVariant;

@Entity
@Table(name = "stock_receipt_items")
public class StockReceiptItem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "receipt_id", nullable = false)
	private StockReceipt receipt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "variant_id")
	private ProductVariant variant;

	@Column(name = "sku", nullable = false, length = 60)
	private String sku;

	@Column(name = "product_name", nullable = false, length = 300)
	private String productName;

	@Column(name = "quantity", nullable = false)
	private int quantity;

	@Column(name = "unit_cost", nullable = false, precision = 15, scale = 2)
	private BigDecimal unitCost = BigDecimal.ZERO;

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }
	public StockReceipt getReceipt() { return receipt; }
	public void setReceipt(StockReceipt receipt) { this.receipt = receipt; }
	public ProductVariant getVariant() { return variant; }
	public void setVariant(ProductVariant variant) { this.variant = variant; }
	public String getSku() { return sku; }
	public void setSku(String sku) { this.sku = sku; }
	public String getProductName() { return productName; }
	public void setProductName(String productName) { this.productName = productName; }
	public int getQuantity() { return quantity; }
	public void setQuantity(int quantity) { this.quantity = quantity; }
	public BigDecimal getUnitCost() { return unitCost; }
	public void setUnitCost(BigDecimal unitCost) { this.unitCost = unitCost; }
}
