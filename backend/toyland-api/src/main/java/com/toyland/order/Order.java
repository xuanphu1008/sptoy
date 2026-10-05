package com.toyland.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.*;

import com.toyland.promotion.Voucher;
import com.toyland.user.User;

/** Don hang. Ten entity la ShopOrder de tranh trung tu khoa ORDER trong JPQL. */
@Entity(name = "ShopOrder")
@Table(name = "orders")
public class Order {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "order_code", nullable = false, length = 30)
	private String orderCode;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(name = "receiver_name", nullable = false, length = 100)
	private String receiverName;

	@Column(name = "phone", nullable = false, length = 20)
	private String phone;

	@Column(name = "province_name", nullable = false)
	private String provinceName;

	@Column(name = "district_name", nullable = false)
	private String districtName;

	@Column(name = "ward_name", nullable = false)
	private String wardName;

	@Column(name = "address_detail", nullable = false)
	private String addressDetail;

	@Column(name = "district_id")
	private Integer districtId;

	@Column(name = "ward_code", length = 20)
	private String wardCode;

	@Column(name = "note", length = 500)
	private String note;

	@Column(name = "gift_wrap", nullable = false)
	private boolean giftWrap;

	@Column(name = "gift_message", length = 300)
	private String giftMessage;

	@Column(name = "subtotal", nullable = false, precision = 15, scale = 2)
	private BigDecimal subtotal = BigDecimal.ZERO;

	@Column(name = "discount_amount", nullable = false, precision = 15, scale = 2)
	private BigDecimal discountAmount = BigDecimal.ZERO;

	@Column(name = "shipping_fee", nullable = false, precision = 15, scale = 2)
	private BigDecimal shippingFee = BigDecimal.ZERO;

	@Column(name = "shipping_discount", nullable = false, precision = 15, scale = 2)
	private BigDecimal shippingDiscount = BigDecimal.ZERO;

	@Column(name = "gift_wrap_fee", nullable = false, precision = 15, scale = 2)
	private BigDecimal giftWrapFee = BigDecimal.ZERO;

	@Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
	private BigDecimal totalAmount = BigDecimal.ZERO;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "voucher_id")
	private Voucher voucher;

	@Column(name = "voucher_code", length = 30)
	private String voucherCode;

	/** Diem thuong khach dung cho don nay va so tien duoc tru tuong ung. */
	@Column(name = "points_used", nullable = false)
	private int pointsUsed;

	@Column(name = "points_discount", nullable = false, precision = 15, scale = 2)
	private BigDecimal pointsDiscount = BigDecimal.ZERO;

	/** Diem duoc cong khi don HOAN TAT (0 = chua cong). */
	@Column(name = "points_earned", nullable = false)
	private int pointsEarned;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 30)
	private OrderStatus status;

	@Enumerated(EnumType.STRING)
	@Column(name = "payment_method", nullable = false, length = 20)
	private PaymentMethod paymentMethod;

	@Enumerated(EnumType.STRING)
	@Column(name = "payment_status", nullable = false, length = 20)
	private PaymentStatus paymentStatus = PaymentStatus.UNPAID;

	@Column(name = "paid_at")
	private LocalDateTime paidAt;

	@Column(name = "payment_deadline")
	private LocalDateTime paymentDeadline;

	@Column(name = "cancel_reason", length = 300)
	private String cancelReason;

	@Column(name = "internal_note", length = 500)
	private String internalNote;

	@Column(name = "delivered_at")
	private LocalDateTime deliveredAt;

	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("id ASC")
	private List<OrderItem> items = new ArrayList<>();

	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("createdAt ASC, id ASC")
	private List<OrderStatusHistory> history = new ArrayList<>();

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

	public void addItem(OrderItem item) {
		item.setOrder(this);
		items.add(item);
	}

	public void addHistory(OrderStatus from, OrderStatus to, String note, String by) {
		OrderStatusHistory h = new OrderStatusHistory();
		h.setOrder(this);
		h.setFromStatus(from);
		h.setToStatus(to);
		h.setNote(note);
		h.setChangedBy(by);
		history.add(h);
	}

	public String getFullAddress() {
		return addressDetail + ", " + wardName + ", " + districtName + ", " + provinceName;
	}

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }
	public String getOrderCode() { return orderCode; }
	public void setOrderCode(String orderCode) { this.orderCode = orderCode; }
	public User getUser() { return user; }
	public void setUser(User user) { this.user = user; }
	public String getReceiverName() { return receiverName; }
	public void setReceiverName(String receiverName) { this.receiverName = receiverName; }
	public String getPhone() { return phone; }
	public void setPhone(String phone) { this.phone = phone; }
	public String getProvinceName() { return provinceName; }
	public void setProvinceName(String provinceName) { this.provinceName = provinceName; }
	public String getDistrictName() { return districtName; }
	public void setDistrictName(String districtName) { this.districtName = districtName; }
	public String getWardName() { return wardName; }
	public void setWardName(String wardName) { this.wardName = wardName; }
	public String getAddressDetail() { return addressDetail; }
	public void setAddressDetail(String addressDetail) { this.addressDetail = addressDetail; }
	public Integer getDistrictId() { return districtId; }
	public void setDistrictId(Integer districtId) { this.districtId = districtId; }
	public String getWardCode() { return wardCode; }
	public void setWardCode(String wardCode) { this.wardCode = wardCode; }
	public String getNote() { return note; }
	public void setNote(String note) { this.note = note; }
	public boolean isGiftWrap() { return giftWrap; }
	public void setGiftWrap(boolean giftWrap) { this.giftWrap = giftWrap; }
	public String getGiftMessage() { return giftMessage; }
	public void setGiftMessage(String giftMessage) { this.giftMessage = giftMessage; }
	public BigDecimal getSubtotal() { return subtotal; }
	public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
	public BigDecimal getDiscountAmount() { return discountAmount; }
	public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }
	public BigDecimal getShippingFee() { return shippingFee; }
	public void setShippingFee(BigDecimal shippingFee) { this.shippingFee = shippingFee; }
	public BigDecimal getShippingDiscount() { return shippingDiscount; }
	public void setShippingDiscount(BigDecimal shippingDiscount) { this.shippingDiscount = shippingDiscount; }
	public BigDecimal getGiftWrapFee() { return giftWrapFee; }
	public void setGiftWrapFee(BigDecimal giftWrapFee) { this.giftWrapFee = giftWrapFee; }
	public BigDecimal getTotalAmount() { return totalAmount; }
	public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
	public Voucher getVoucher() { return voucher; }
	public void setVoucher(Voucher voucher) { this.voucher = voucher; }
	public int getPointsUsed() { return pointsUsed; }
	public void setPointsUsed(int pointsUsed) { this.pointsUsed = pointsUsed; }
	public BigDecimal getPointsDiscount() { return pointsDiscount; }
	public void setPointsDiscount(BigDecimal pointsDiscount) { this.pointsDiscount = pointsDiscount; }
	public int getPointsEarned() { return pointsEarned; }
	public void setPointsEarned(int pointsEarned) { this.pointsEarned = pointsEarned; }

	public String getVoucherCode() { return voucherCode; }
	public void setVoucherCode(String voucherCode) { this.voucherCode = voucherCode; }
	public OrderStatus getStatus() { return status; }
	public void setStatus(OrderStatus status) { this.status = status; }
	public PaymentMethod getPaymentMethod() { return paymentMethod; }
	public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }
	public PaymentStatus getPaymentStatus() { return paymentStatus; }
	public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }
	public LocalDateTime getPaidAt() { return paidAt; }
	public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }
	public LocalDateTime getPaymentDeadline() { return paymentDeadline; }
	public void setPaymentDeadline(LocalDateTime paymentDeadline) { this.paymentDeadline = paymentDeadline; }
	public String getCancelReason() { return cancelReason; }
	public void setCancelReason(String cancelReason) { this.cancelReason = cancelReason; }
	public String getInternalNote() { return internalNote; }
	public void setInternalNote(String internalNote) { this.internalNote = internalNote; }
	public LocalDateTime getDeliveredAt() { return deliveredAt; }
	public void setDeliveredAt(LocalDateTime deliveredAt) { this.deliveredAt = deliveredAt; }
	public List<OrderItem> getItems() { return items; }
	public void setItems(List<OrderItem> items) { this.items = items; }
	public List<OrderStatusHistory> getHistory() { return history; }
	public void setHistory(List<OrderStatusHistory> history) { this.history = history; }
	public LocalDateTime getCreatedAt() { return createdAt; }
	public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
	public LocalDateTime getUpdatedAt() { return updatedAt; }
	public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
