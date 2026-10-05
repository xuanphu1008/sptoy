package com.toyland.auth;

import java.time.LocalDateTime;

import jakarta.persistence.*;

import com.toyland.user.User;

/** Token trong link xac nhan email (chi luu SHA-256, dung 1 lan, co han). */
@Entity
@Table(name = "email_verification_tokens")
public class EmailVerificationToken {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	/** Email duoc xac nhan tai thoi diem gui link (doi email -> link cu vo hieu) */
	@Column(name = "email", nullable = false, length = 100)
	private String email;

	@Column(name = "token_hash", nullable = false, length = 64, unique = true)
	private String tokenHash;

	@Column(name = "expires_at", nullable = false)
	private LocalDateTime expiresAt;

	@Column(name = "used_at")
	private LocalDateTime usedAt;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@PrePersist
	protected void onCreate() {
		if (createdAt == null) {
			createdAt = LocalDateTime.now();
		}
	}

	public Long getId() { return id; }
	public User getUser() { return user; }
	public void setUser(User user) { this.user = user; }
	public String getEmail() { return email; }
	public void setEmail(String email) { this.email = email; }
	public String getTokenHash() { return tokenHash; }
	public void setTokenHash(String tokenHash) { this.tokenHash = tokenHash; }
	public LocalDateTime getExpiresAt() { return expiresAt; }
	public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
	public LocalDateTime getUsedAt() { return usedAt; }
	public void setUsedAt(LocalDateTime usedAt) { this.usedAt = usedAt; }
	public LocalDateTime getCreatedAt() { return createdAt; }
}
