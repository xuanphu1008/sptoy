package com.toyland.partner;

import java.time.LocalDateTime;

import jakarta.persistence.*;

/**
 * Doi tac ben thu ba duoc cap quyen goi API dich vu cua ToyLand (/api/partner/v1).
 * Xac thuc: apiKey dinh danh doi tac, secret dung de ky HMAC-SHA256 tung yeu cau (secret khong bao gio gui qua mang).
 */
@Entity
@Table(name = "partners")
public class Partner {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "name", nullable = false, length = 120)
	private String name;

	@Column(name = "api_key", nullable = false, length = 40, unique = true)
	private String apiKey;

	/** Chuoi bi mat dung chung de ky HMAC. May chu phai doc lai duoc nen khong the bam mot chieu nhu mat khau. */
	@Column(name = "secret", nullable = false, length = 80)
	private String secret;

	@Column(name = "active", nullable = false)
	private boolean active = true;

	/** Han muc so yeu cau moi phut */
	@Column(name = "rate_limit_per_min", nullable = false)
	private int rateLimitPerMin = 60;

	/** Dia chi nhan webhook (HTTPS) khi don cua doi tac doi trang thai. De trong = khong nhan webhook. */
	@Column(name = "webhook_url", length = 300)
	private String webhookUrl;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "last_used_at")
	private LocalDateTime lastUsedAt;

	@PrePersist
	protected void onCreate() {
		if (createdAt == null) {
			createdAt = LocalDateTime.now();
		}
	}

	public Long getId() { return id; }
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	public String getApiKey() { return apiKey; }
	public void setApiKey(String apiKey) { this.apiKey = apiKey; }
	public String getSecret() { return secret; }
	public void setSecret(String secret) { this.secret = secret; }
	public boolean isActive() { return active; }
	public void setActive(boolean active) { this.active = active; }
	public int getRateLimitPerMin() { return rateLimitPerMin; }
	public void setRateLimitPerMin(int rateLimitPerMin) { this.rateLimitPerMin = rateLimitPerMin; }
	public String getWebhookUrl() { return webhookUrl; }
	public void setWebhookUrl(String webhookUrl) { this.webhookUrl = webhookUrl; }
	public LocalDateTime getCreatedAt() { return createdAt; }
	public LocalDateTime getLastUsedAt() { return lastUsedAt; }
	public void setLastUsedAt(LocalDateTime lastUsedAt) { this.lastUsedAt = lastUsedAt; }
}
