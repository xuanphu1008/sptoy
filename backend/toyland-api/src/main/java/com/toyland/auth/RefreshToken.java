package com.toyland.auth;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.*;

import com.toyland.user.User;

/** Refresh token (luu DB de thu hoi duoc). */
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(name = "token", nullable = false, length = 100)
	private String token;

	@Column(name = "expires_at", nullable = false)
	private LocalDateTime expiresAt;

	@Column(name = "revoked", nullable = false)
	private boolean revoked;

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
	public User getUser() { return user; }
	public void setUser(User user) { this.user = user; }
	public String getToken() { return token; }
	public void setToken(String token) { this.token = token; }
	public LocalDateTime getExpiresAt() { return expiresAt; }
	public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
	public boolean isRevoked() { return revoked; }
	public void setRevoked(boolean revoked) { this.revoked = revoked; }
	public LocalDateTime getCreatedAt() { return createdAt; }
	public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
