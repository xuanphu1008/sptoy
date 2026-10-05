package com.toyland.loyalty;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

/** Lich su bien dong diem thuong (so du sau moi giao dich). */
@Entity
@Table(name = "point_transactions")
public class PointTransaction {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Column(name = "order_id")
	private Long orderId;

	@Enumerated(EnumType.STRING)
	@Column(name = "type", nullable = false, length = 20)
	private PointType type;

	@Column(name = "points", nullable = false)
	private int points;

	@Column(name = "balance_after", nullable = false)
	private int balanceAfter;

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
	public Long getUserId() { return userId; }
	public void setUserId(Long userId) { this.userId = userId; }
	public Long getOrderId() { return orderId; }
	public void setOrderId(Long orderId) { this.orderId = orderId; }
	public PointType getType() { return type; }
	public void setType(PointType type) { this.type = type; }
	public int getPoints() { return points; }
	public void setPoints(int points) { this.points = points; }
	public int getBalanceAfter() { return balanceAfter; }
	public void setBalanceAfter(int balanceAfter) { this.balanceAfter = balanceAfter; }
	public String getNote() { return note; }
	public void setNote(String note) { this.note = note; }
	public String getCreatedBy() { return createdBy; }
	public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
	public LocalDateTime getCreatedAt() { return createdAt; }
}
