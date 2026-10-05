package com.toyland.review;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

/** Tu khoa cam trong danh gia (luu dang khong dau, chu thuong de so khop ca khi khach go khong dau). */
@Entity
@Table(name = "banned_keywords")
public class BannedKeyword {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "keyword", nullable = false, length = 100)
	private String keyword;

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
	public String getKeyword() { return keyword; }
	public void setKeyword(String keyword) { this.keyword = keyword; }
	public String getCreatedBy() { return createdBy; }
	public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
	public LocalDateTime getCreatedAt() { return createdAt; }
}
