package com.toyland.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.*;

import com.toyland.common.util.StringListConverter;
import com.toyland.user.User;

/** Yeu cau doi tra / hoan tien. */
@Entity
@Table(name = "return_requests")
public class ReturnRequest {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "order_id", nullable = false)
	private Order order;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(name = "reason", nullable = false, length = 200)
	private String reason;

	@Column(name = "description", length = 1000)
	private String description;

	@Convert(converter = StringListConverter.class)
	@Column(name = "image_urls", columnDefinition = "TEXT")
	private List<String> imageUrls = new ArrayList<>();

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 20)
	private ReturnStatus status = ReturnStatus.REQUESTED;

	@Column(name = "admin_note", length = 500)
	private String adminNote;

	@Column(name = "refund_amount", precision = 15, scale = 2)
	private BigDecimal refundAmount;

	@Column(name = "processed_by", length = 50)
	private String processedBy;

	@Column(name = "processed_at")
	private LocalDateTime processedAt;

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
	public Order getOrder() { return order; }
	public void setOrder(Order order) { this.order = order; }
	public User getUser() { return user; }
	public void setUser(User user) { this.user = user; }
	public String getReason() { return reason; }
	public void setReason(String reason) { this.reason = reason; }
	public String getDescription() { return description; }
	public void setDescription(String description) { this.description = description; }
	public List<String> getImageUrls() { return imageUrls; }
	public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }
	public ReturnStatus getStatus() { return status; }
	public void setStatus(ReturnStatus status) { this.status = status; }
	public String getAdminNote() { return adminNote; }
	public void setAdminNote(String adminNote) { this.adminNote = adminNote; }
	public BigDecimal getRefundAmount() { return refundAmount; }
	public void setRefundAmount(BigDecimal refundAmount) { this.refundAmount = refundAmount; }
	public String getProcessedBy() { return processedBy; }
	public void setProcessedBy(String processedBy) { this.processedBy = processedBy; }
	public LocalDateTime getProcessedAt() { return processedAt; }
	public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }
	public LocalDateTime getCreatedAt() { return createdAt; }
	public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
	public LocalDateTime getUpdatedAt() { return updatedAt; }
	public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
