package com.toyland.inventory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.*;

import com.toyland.supplier.Supplier;

/** Phieu nhap kho. */
@Entity
@Table(name = "stock_receipts")
public class StockReceipt {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "code", nullable = false, length = 30)
	private String code;

	/** Nha cung cap (null neu nhap le / NCC da xoa). */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "supplier_id")
	private Supplier supplierRef;

	/** Ten NCC luu kem tai thoi diem nhap. */
	@Column(name = "supplier", length = 200)
	private String supplier;

	@Column(name = "note", length = 500)
	private String note;

	@Column(name = "total_quantity", nullable = false)
	private int totalQuantity;

	@Column(name = "total_cost", nullable = false, precision = 15, scale = 2)
	private BigDecimal totalCost = BigDecimal.ZERO;

	@Column(name = "created_by", length = 50)
	private String createdBy;

	@OneToMany(mappedBy = "receipt", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("id ASC")
	private List<StockReceiptItem> items = new ArrayList<>();

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
	public String getCode() { return code; }
	public void setCode(String code) { this.code = code; }
	public Supplier getSupplierRef() { return supplierRef; }
	public void setSupplierRef(Supplier supplierRef) { this.supplierRef = supplierRef; }
	public String getSupplier() { return supplier; }
	public void setSupplier(String supplier) { this.supplier = supplier; }
	public String getNote() { return note; }
	public void setNote(String note) { this.note = note; }
	public int getTotalQuantity() { return totalQuantity; }
	public void setTotalQuantity(int totalQuantity) { this.totalQuantity = totalQuantity; }
	public BigDecimal getTotalCost() { return totalCost; }
	public void setTotalCost(BigDecimal totalCost) { this.totalCost = totalCost; }
	public String getCreatedBy() { return createdBy; }
	public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
	public List<StockReceiptItem> getItems() { return items; }
	public void setItems(List<StockReceiptItem> items) { this.items = items; }
	public LocalDateTime getCreatedAt() { return createdAt; }
	public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
