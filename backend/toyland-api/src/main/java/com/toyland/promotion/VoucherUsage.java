package com.toyland.promotion;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.*;

import com.toyland.order.Order;
import com.toyland.user.User;

@Entity
@Table(name = "voucher_usages")
public class VoucherUsage {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "voucher_id", nullable = false)
	private Voucher voucher;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "order_id", nullable = false)
	private Order order;

	@Column(name = "discount_amount", nullable = false, precision = 15, scale = 2)
	private BigDecimal discountAmount;

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
	public Voucher getVoucher() { return voucher; }
	public void setVoucher(Voucher voucher) { this.voucher = voucher; }
	public User getUser() { return user; }
	public void setUser(User user) { this.user = user; }
	public Order getOrder() { return order; }
	public void setOrder(Order order) { this.order = order; }
	public BigDecimal getDiscountAmount() { return discountAmount; }
	public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }
	public LocalDateTime getCreatedAt() { return createdAt; }
	public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
