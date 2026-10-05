package com.toyland.catalog;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.*;

/** Thuong hieu. */
@Entity
@Table(name = "brands")
public class Brand {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "name", nullable = false, length = 100)
	private String name;

	@Column(name = "slug", nullable = false, length = 120)
	private String slug;

	@Column(name = "logo_url", length = 500)
	private String logoUrl;

	@Column(name = "country", length = 100)
	private String country;

	@Column(name = "description", length = 500)
	private String description;

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
	public String getSlug() { return slug; }
	public void setSlug(String slug) { this.slug = slug; }
	public String getLogoUrl() { return logoUrl; }
	public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }
	public String getCountry() { return country; }
	public void setCountry(String country) { this.country = country; }
	public String getDescription() { return description; }
	public void setDescription(String description) { this.description = description; }
	public LocalDateTime getCreatedAt() { return createdAt; }
	public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
	public LocalDateTime getUpdatedAt() { return updatedAt; }
	public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
