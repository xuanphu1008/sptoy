package com.toyland.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.*;

@Entity
@Table(name = "order_status_history")
public class OrderStatusHistory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "order_id", nullable = false)
	private Order order;

	@Enumerated(EnumType.STRING)
	@Column(name = "from_status", length = 30)
	private OrderStatus fromStatus;

	@Enumerated(EnumType.STRING)
	@Column(name = "to_status", nullable = false, length = 30)
	private OrderStatus toStatus;

	@Column(name = "note", length = 500)
	private String note;

	@Column(name = "changed_by", length = 50)
	private String changedBy;

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
	public Order getOrder() { return order; }
	public void setOrder(Order order) { this.order = order; }
	public OrderStatus getFromStatus() { return fromStatus; }
	public void setFromStatus(OrderStatus fromStatus) { this.fromStatus = fromStatus; }
	public OrderStatus getToStatus() { return toStatus; }
	public void setToStatus(OrderStatus toStatus) { this.toStatus = toStatus; }
	public String getNote() { return note; }
	public void setNote(String note) { this.note = note; }
	public String getChangedBy() { return changedBy; }
	public void setChangedBy(String changedBy) { this.changedBy = changedBy; }
	public LocalDateTime getCreatedAt() { return createdAt; }
	public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
