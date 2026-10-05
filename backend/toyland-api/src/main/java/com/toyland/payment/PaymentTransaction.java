package com.toyland.payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;

import com.toyland.order.Order;

/**
 * So cai thu - chi cua don hang (doi soat): moi lan khach tra tien (MoMo, COD, tien mat, chuyen khoan)
 * hoac shop hoan tien deu la 1 dong. provider: MOMO | COD | CASH | BANK_TRANSFER.
 */
@Entity
@Table(name = "payment_transactions")
public class PaymentTransaction {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "order_id", nullable = false)
	private Order order;

	@Column(name = "provider", nullable = false, length = 20)
	private String provider = "MOMO";

	@Column(name = "txn_ref", nullable = false, length = 50)
	private String txnRef;

	@Column(name = "amount", nullable = false, precision = 15, scale = 2)
	private BigDecimal amount;

	@Column(name = "bank_code", length = 30)
	private String bankCode;

	@Column(name = "transaction_no", length = 50)
	private String transactionNo;

	@Column(name = "response_code", length = 10)
	private String responseCode;

	@Column(name = "status", nullable = false, length = 20)
	private String status = "PENDING";

	@Enumerated(EnumType.STRING)
	@Column(name = "kind", nullable = false, length = 10)
	private PaymentKind kind = PaymentKind.PAYMENT;

	@Column(name = "note", length = 300)
	private String note;

	/** Nguoi ghi nhan (null = cong thanh toan / he thong tu dong) */
	@Column(name = "created_by", length = 50)
	private String createdBy;

	@Column(name = "raw_data", columnDefinition = "TEXT")
	private String rawData;

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
	public String getProvider() { return provider; }
	public void setProvider(String provider) { this.provider = provider; }
	public String getTxnRef() { return txnRef; }
	public void setTxnRef(String txnRef) { this.txnRef = txnRef; }
	public BigDecimal getAmount() { return amount; }
	public void setAmount(BigDecimal amount) { this.amount = amount; }
	public String getBankCode() { return bankCode; }
	public void setBankCode(String bankCode) { this.bankCode = bankCode; }
	public String getTransactionNo() { return transactionNo; }
	public void setTransactionNo(String transactionNo) { this.transactionNo = transactionNo; }
	public String getResponseCode() { return responseCode; }
	public void setResponseCode(String responseCode) { this.responseCode = responseCode; }
	public String getStatus() { return status; }
	public void setStatus(String status) { this.status = status; }
	public PaymentKind getKind() { return kind; }
	public void setKind(PaymentKind kind) { this.kind = kind; }
	public String getNote() { return note; }
	public void setNote(String note) { this.note = note; }
	public String getCreatedBy() { return createdBy; }
	public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
	public String getRawData() { return rawData; }
	public void setRawData(String rawData) { this.rawData = rawData; }
	public LocalDateTime getCreatedAt() { return createdAt; }
	public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
	public LocalDateTime getUpdatedAt() { return updatedAt; }
	public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
