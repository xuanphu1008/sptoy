package com.toyland.auth;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.*;

import com.toyland.user.User;

/** Token dat lai mat khau (het han sau 15 phut). */
@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetToken {

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

	@Column(name = "used", nullable = false)
	private boolean used;

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
	public boolean isUsed() { return used; }
	public void setUsed(boolean used) { this.used = used; }
	public LocalDateTime getCreatedAt() { return createdAt; }
	public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
