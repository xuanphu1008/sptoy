package com.toyland.inventory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.*;

import com.toyland.catalog.ProductVariant;

/** Nhat ky bien dong ton kho. */
@Entity
@Table(name = "stock_movements")
public class StockMovement {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "variant_id")
	private ProductVariant variant;

	@Column(name = "sku", nullable = false, length = 60)
	private String sku;

	@Enumerated(EnumType.STRING)
	@Column(name = "type", nullable = false, length = 20)
	private MovementType type;

	@Column(name = "quantity_change", nullable = false)
	private int quantityChange;

	@Column(name = "balance_after", nullable = false)
	private int balanceAfter;

	@Column(name = "ref_code", length = 50)
	private String refCode;

	@Column(name = "note", length = 300)
	private String note;

	@Column(name = "created_by", length = 50)
	private String createdBy;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@PrePersist
	protected void onCreate() {
		if (createdAt == null) {
			createdAt = LocalDateTime.now();
		}
	}

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }
	public ProductVariant getVariant() { return variant; }
	public void setVariant(ProductVariant variant) { this.variant = variant; }
	public String getSku() { return sku; }
	public void setSku(String sku) { this.sku = sku; }
	public MovementType getType() { return type; }
	public void setType(MovementType type) { this.type = type; }
	public int getQuantityChange() { return quantityChange; }
	public void setQuantityChange(int quantityChange) { this.quantityChange = quantityChange; }
	public int getBalanceAfter() { return balanceAfter; }
	public void setBalanceAfter(int balanceAfter) { this.balanceAfter = balanceAfter; }
	public String getRefCode() { return refCode; }
	public void setRefCode(String refCode) { this.refCode = refCode; }
	public String getNote() { return note; }
	public void setNote(String note) { this.note = note; }
	public String getCreatedBy() { return createdBy; }
	public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
	public LocalDateTime getCreatedAt() { return createdAt; }
	public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
