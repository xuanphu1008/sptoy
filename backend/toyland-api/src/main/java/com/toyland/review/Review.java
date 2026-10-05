package com.toyland.review;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.*;

import com.toyland.catalog.Product;
import com.toyland.common.util.StringListConverter;
import com.toyland.order.OrderItem;
import com.toyland.user.User;

/** Danh gia san pham (chi khach da mua). */
@Entity
@Table(name = "reviews")
public class Review {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "product_id", nullable = false)
	private Product product;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "order_item_id")
	private OrderItem orderItem;

	@Column(name = "variant_name", length = 200)
	private String variantName;

	@Column(name = "rating", nullable = false)
	private int rating;

	@Column(name = "comment", columnDefinition = "TEXT")
	private String comment;

	@Convert(converter = StringListConverter.class)
	@Column(name = "image_urls", columnDefinition = "TEXT")
	private List<String> imageUrls = new ArrayList<>();

	@Column(name = "hidden", nullable = false)
	private boolean hidden;

	/** Chua tu khoa cam -> tu dong an, cho nhan vien duyet. */
	@Column(name = "flagged", nullable = false)
	private boolean flagged;

	@Column(name = "flag_reason", length = 300)
	private String flagReason;

	@Column(name = "admin_reply", columnDefinition = "TEXT")
	private String adminReply;

	@Column(name = "replied_at")
	private LocalDateTime repliedAt;

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
	public Product getProduct() { return product; }
	public void setProduct(Product product) { this.product = product; }
	public User getUser() { return user; }
	public void setUser(User user) { this.user = user; }
	public OrderItem getOrderItem() { return orderItem; }
	public void setOrderItem(OrderItem orderItem) { this.orderItem = orderItem; }
	public String getVariantName() { return variantName; }
	public void setVariantName(String variantName) { this.variantName = variantName; }
	public int getRating() { return rating; }
	public void setRating(int rating) { this.rating = rating; }
	public String getComment() { return comment; }
	public void setComment(String comment) { this.comment = comment; }
	public List<String> getImageUrls() { return imageUrls; }
	public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }
	public boolean isHidden() { return hidden; }
	public void setHidden(boolean hidden) { this.hidden = hidden; }
	public boolean isFlagged() { return flagged; }
	public void setFlagged(boolean flagged) { this.flagged = flagged; }
	public String getFlagReason() { return flagReason; }
	public void setFlagReason(String flagReason) { this.flagReason = flagReason; }
	public String getAdminReply() { return adminReply; }
	public void setAdminReply(String adminReply) { this.adminReply = adminReply; }
	public LocalDateTime getRepliedAt() { return repliedAt; }
	public void setRepliedAt(LocalDateTime repliedAt) { this.repliedAt = repliedAt; }
	public LocalDateTime getCreatedAt() { return createdAt; }
	public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
	public LocalDateTime getUpdatedAt() { return updatedAt; }
	public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
