package com.toyland.idempotency;

import java.time.LocalDateTime;

import jakarta.persistence.*;

/**
 * Dau vet cua mot yeu cau da gui kem Idempotency-Key. Cap (key, subject) la duy nhat:
 * cung khoa + cung nguoi dung chi duoc thuc thi mot lan, lan gui lai nhan ve ket qua cu.
 */
@Entity
@Table(name = "idempotency_keys", uniqueConstraints = @UniqueConstraint(name = "uq_idem_key_subject", columnNames = { "idem_key", "subject" }))
public class IdempotencyKey {

	public enum State { IN_PROGRESS, COMPLETED }

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "idem_key", nullable = false, length = 80)
	private String key;

	/** Chu the gui yeu cau (vd "user:12" hoac "partner:3") de hai nguoi dung tinh co trung khoa khong anh huong nhau */
	@Column(name = "subject", nullable = false, length = 60)
	private String subject;

	/** SHA-256 cua phuong thuc + duong dan + than yeu cau, de bat truong hop dung lai khoa cho yeu cau khac */
	@Column(name = "request_hash", nullable = false, length = 64)
	private String requestHash;

	@Enumerated(EnumType.STRING)
	@Column(name = "state", nullable = false, length = 20)
	private State state = State.IN_PROGRESS;

	@Column(name = "response_status")
	private Integer responseStatus;

	@Lob
	@Column(name = "response_body", columnDefinition = "MEDIUMTEXT")
	private String responseBody;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "expires_at", nullable = false)
	private LocalDateTime expiresAt;

	@PrePersist
	protected void onCreate() {
		if (createdAt == null) {
			createdAt = LocalDateTime.now();
		}
	}

	public Long getId() { return id; }
	public String getKey() { return key; }
	public void setKey(String key) { this.key = key; }
	public String getSubject() { return subject; }
	public void setSubject(String subject) { this.subject = subject; }
	public String getRequestHash() { return requestHash; }
	public void setRequestHash(String requestHash) { this.requestHash = requestHash; }
	public State getState() { return state; }
	public void setState(State state) { this.state = state; }
	public Integer getResponseStatus() { return responseStatus; }
	public void setResponseStatus(Integer responseStatus) { this.responseStatus = responseStatus; }
	public String getResponseBody() { return responseBody; }
	public void setResponseBody(String responseBody) { this.responseBody = responseBody; }
	public LocalDateTime getCreatedAt() { return createdAt; }
	public LocalDateTime getExpiresAt() { return expiresAt; }
	public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
}
