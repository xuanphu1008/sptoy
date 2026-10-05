package com.toyland.promotion;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.*;

/** Chuong trinh giam gia theo thoi gian. */
@Entity
@Table(name = "campaigns")
public class Campaign {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "name", nullable = false, length = 150)
	private String name;

	@Column(name = "description", length = 500)
	private String description;

	@Column(name = "discount_percent", nullable = false)
	private int discountPercent;

	@Enumerated(EnumType.STRING)
	@Column(name = "scope_type", nullable = false, length = 20)
	private CampaignScope scopeType = CampaignScope.ALL;

	@Column(name = "scope_id")
	private Long scopeId;

	@Column(name = "badge_label", length = 50)
	private String badgeLabel;

	@Column(name = "start_at", nullable = false)
	private LocalDateTime startAt;

	@Column(name = "end_at", nullable = false)
	private LocalDateTime endAt;

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
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	public String getDescription() { return description; }
	public void setDescription(String description) { this.description = description; }
	public int getDiscountPercent() { return discountPercent; }
	public void setDiscountPercent(int discountPercent) { this.discountPercent = discountPercent; }
	public CampaignScope getScopeType() { return scopeType; }
	public void setScopeType(CampaignScope scopeType) { this.scopeType = scopeType; }
	public Long getScopeId() { return scopeId; }
	public void setScopeId(Long scopeId) { this.scopeId = scopeId; }
	public String getBadgeLabel() { return badgeLabel; }
	public void setBadgeLabel(String badgeLabel) { this.badgeLabel = badgeLabel; }
	public LocalDateTime getStartAt() { return startAt; }
	public void setStartAt(LocalDateTime startAt) { this.startAt = startAt; }
	public LocalDateTime getEndAt() { return endAt; }
	public void setEndAt(LocalDateTime endAt) { this.endAt = endAt; }
	public boolean isActive() { return active; }
	public void setActive(boolean active) { this.active = active; }
	public LocalDateTime getCreatedAt() { return createdAt; }
	public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
	public LocalDateTime getUpdatedAt() { return updatedAt; }
	public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
