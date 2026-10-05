package com.toyland.user;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.*;

/** Nguoi dung he thong (ADMIN / STAFF / CUSTOMER). DynamicUpdate: chi ghi cot thay doi (diem thuong cap nhat bang cau lenh rieng). */
@Entity
@org.hibernate.annotations.DynamicUpdate
@Table(name = "users")
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "username", nullable = false, length = 50)
	private String username;

	@Column(name = "password", nullable = false, length = 100)
	private String password;

	@Column(name = "full_name", nullable = false, length = 100)
	private String fullName;

	@Column(name = "email", nullable = false, length = 100)
	private String email;

	@Column(name = "phone", length = 20)
	private String phone;

	@Enumerated(EnumType.STRING)
	@Column(name = "role", nullable = false, length = 20)
	private Role role = Role.CUSTOMER;

	@Column(name = "enabled", nullable = false)
	private boolean enabled = true;

	@Column(name = "failed_login_attempts", nullable = false)
	private int failedLoginAttempts;

	@Column(name = "locked_until")
	private LocalDateTime lockedUntil;

	@Column(name = "consent_at")
	private LocalDateTime consentAt;

	/** Thoi diem xac nhan email (NULL = chua xac nhan) - giong email_verified_at cua Laravel */
	@Column(name = "email_verified_at")
	private LocalDateTime emailVerifiedAt;

	@Column(name = "deleted", nullable = false)
	private boolean deleted;

	/** Diem thuong hien co (chi doi qua LoyaltyService). */
	@Column(name = "points", nullable = false)
	private int points;

	/** Tong diem da tich luy tu truoc den nay -> hang thanh vien. */
	@Column(name = "lifetime_points", nullable = false)
	private int lifetimePoints;

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
	public String getUsername() { return username; }
	public void setUsername(String username) { this.username = username; }
	public String getPassword() { return password; }
	public void setPassword(String password) { this.password = password; }
	public String getFullName() { return fullName; }
	public void setFullName(String fullName) { this.fullName = fullName; }
	public String getEmail() { return email; }
	public void setEmail(String email) { this.email = email; }
	public String getPhone() { return phone; }
	public void setPhone(String phone) { this.phone = phone; }
	public Role getRole() { return role; }
	public void setRole(Role role) { this.role = role; }
	public boolean isEnabled() { return enabled; }
	public void setEnabled(boolean enabled) { this.enabled = enabled; }
	public int getFailedLoginAttempts() { return failedLoginAttempts; }
	public void setFailedLoginAttempts(int failedLoginAttempts) { this.failedLoginAttempts = failedLoginAttempts; }
	public LocalDateTime getLockedUntil() { return lockedUntil; }
	public void setLockedUntil(LocalDateTime lockedUntil) { this.lockedUntil = lockedUntil; }
	public LocalDateTime getConsentAt() { return consentAt; }
	public void setConsentAt(LocalDateTime consentAt) { this.consentAt = consentAt; }
	public LocalDateTime getEmailVerifiedAt() { return emailVerifiedAt; }
	public void setEmailVerifiedAt(LocalDateTime emailVerifiedAt) { this.emailVerifiedAt = emailVerifiedAt; }
	public boolean isEmailVerified() { return emailVerifiedAt != null; }
	public boolean isDeleted() { return deleted; }
	public void setDeleted(boolean deleted) { this.deleted = deleted; }
	public LocalDateTime getCreatedAt() { return createdAt; }
	public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
	public LocalDateTime getUpdatedAt() { return updatedAt; }
	public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
	public int getPoints() { return points; }
	public void setPoints(int points) { this.points = points; }
	public int getLifetimePoints() { return lifetimePoints; }
	public void setLifetimePoints(int lifetimePoints) { this.lifetimePoints = lifetimePoints; }
}
