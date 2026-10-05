package com.toyland.promotion;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.*;

/** Banner trang chu. */
@Entity
@Table(name = "banners")
public class Banner {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "title", nullable = false, length = 150)
	private String title;

	@Column(name = "subtitle", length = 300)
	private String subtitle;

	@Column(name = "image_url", length = 500)
	private String imageUrl;

	@Column(name = "link_url", length = 300)
	private String linkUrl;

	@Column(name = "button_text", length = 50)
	private String buttonText;

	@Column(name = "bg_color", length = 20)
	private String bgColor;

	@Column(name = "sort_order", nullable = false)
	private int sortOrder;

	@Column(name = "active", nullable = false)
	private boolean active = true;

	@Column(name = "start_at")
	private LocalDateTime startAt;

	@Column(name = "end_at")
	private LocalDateTime endAt;

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
	public String getTitle() { return title; }
	public void setTitle(String title) { this.title = title; }
	public String getSubtitle() { return subtitle; }
	public void setSubtitle(String subtitle) { this.subtitle = subtitle; }
	public String getImageUrl() { return imageUrl; }
	public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
	public String getLinkUrl() { return linkUrl; }
	public void setLinkUrl(String linkUrl) { this.linkUrl = linkUrl; }
	public String getButtonText() { return buttonText; }
	public void setButtonText(String buttonText) { this.buttonText = buttonText; }
	public String getBgColor() { return bgColor; }
	public void setBgColor(String bgColor) { this.bgColor = bgColor; }
	public int getSortOrder() { return sortOrder; }
	public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }
	public boolean isActive() { return active; }
	public void setActive(boolean active) { this.active = active; }
	public LocalDateTime getStartAt() { return startAt; }
	public void setStartAt(LocalDateTime startAt) { this.startAt = startAt; }
	public LocalDateTime getEndAt() { return endAt; }
	public void setEndAt(LocalDateTime endAt) { this.endAt = endAt; }
	public LocalDateTime getCreatedAt() { return createdAt; }
	public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
	public LocalDateTime getUpdatedAt() { return updatedAt; }
	public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
