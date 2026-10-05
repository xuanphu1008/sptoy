package com.toyland.promotion;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.*;

/** Ma giam gia. */
@Entity
@Table(name = "vouchers")
public class Voucher {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "code", nullable = false, length = 30)
	private String code;

	@Column(name = "name", nullable = false, length = 150)
	private String name;

	@Column(name = "description", length = 500)
	private String description;

	@Enumerated(EnumType.STRING)
	@Column(name = "type", nullable = false, length = 20)
	private VoucherType type;

	@Column(name = "value", nullable = false, precision = 15, scale = 2)
	private BigDecimal value = BigDecimal.ZERO;

	@Column(name = "max_discount", precision = 15, scale = 2)
	private BigDecimal maxDiscount;

	@Column(name = "min_order_value", nullable = false, precision = 15, scale = 2)
	private BigDecimal minOrderValue = BigDecimal.ZERO;

	@Column(name = "start_at", nullable = false)
	private LocalDateTime startAt;

	@Column(name = "end_at", nullable = false)
	private LocalDateTime endAt;

	@Column(name = "usage_limit")
	private Integer usageLimit;

	@Column(name = "used_count", nullable = false)
	private int usedCount;

	@Column(name = "per_user_limit", nullable = false)
	private int perUserLimit = 1;

	@Column(name = "active", nullable = false)
	private boolean active = true;

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
	public String getCode() { return code; }
	public void setCode(String code) { this.code = code; }
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	public String getDescription() { return description; }
	public void setDescription(String description) { this.description = description; }
	public VoucherType getType() { return type; }
	public void setType(VoucherType type) { this.type = type; }
	public BigDecimal getValue() { return value; }
	public void setValue(BigDecimal value) { this.value = value; }
	public BigDecimal getMaxDiscount() { return maxDiscount; }
	public void setMaxDiscount(BigDecimal maxDiscount) { this.maxDiscount = maxDiscount; }
	public BigDecimal getMinOrderValue() { return minOrderValue; }
	public void setMinOrderValue(BigDecimal minOrderValue) { this.minOrderValue = minOrderValue; }
	public LocalDateTime getStartAt() { return startAt; }
	public void setStartAt(LocalDateTime startAt) { this.startAt = startAt; }
	public LocalDateTime getEndAt() { return endAt; }
	public void setEndAt(LocalDateTime endAt) { this.endAt = endAt; }
	public Integer getUsageLimit() { return usageLimit; }
	public void setUsageLimit(Integer usageLimit) { this.usageLimit = usageLimit; }
	public int getUsedCount() { return usedCount; }
	public void setUsedCount(int usedCount) { this.usedCount = usedCount; }
	public int getPerUserLimit() { return perUserLimit; }
	public void setPerUserLimit(int perUserLimit) { this.perUserLimit = perUserLimit; }
	public boolean isActive() { return active; }
	public void setActive(boolean active) { this.active = active; }
	public LocalDateTime getCreatedAt() { return createdAt; }
	public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
	public LocalDateTime getUpdatedAt() { return updatedAt; }
	public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
