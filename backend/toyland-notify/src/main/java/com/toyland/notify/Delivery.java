package com.toyland.notify;

import java.time.LocalDateTime;

import jakarta.persistence.*;

/** Mot lan gui: mot email hoac mot webhook. Co so lan thu, lan thu ke tiep, loi gan nhat; qua so lan thi vao hang chet (DEAD). */
@Entity
@Table(name = "deliveries")
public class Delivery {

	public enum Channel { EMAIL, WEBHOOK }

	public enum Status { PENDING, SENT, DEAD }

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "event_id", nullable = false, length = 36)
	private String eventId;

	@Enumerated(EnumType.STRING)
	@Column(name = "channel", nullable = false, length = 10)
	private Channel channel;

	/** Email: dia chi nhan. Webhook: URL. */
	@Column(name = "target", nullable = false, length = 300)
	private String target;

	/** Email: JSON {to, subject, html}. Webhook: than yeu cau JSON se gui di. */
	@Column(name = "payload", nullable = false, columnDefinition = "MEDIUMTEXT")
	private String payload;

	/** Chi webhook: secret cua doi tac de ky HMAC moi lan gui (ky lai ca khi thu lai). */
	@Column(name = "secret", length = 120)
	private String secret;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 10)
	private Status status = Status.PENDING;

	@Column(name = "attempts", nullable = false)
	private int attempts;

	@Column(name = "next_attempt_at", nullable = false)
	private LocalDateTime nextAttemptAt = LocalDateTime.now();

	@Column(name = "last_error", length = 500)
	private String lastError;

	@Column(name = "last_http_status")
	private Integer lastHttpStatus;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt = LocalDateTime.now();

	@Column(name = "sent_at")
	private LocalDateTime sentAt;

	public Long getId() { return id; }
	public String getEventId() { return eventId; }
	public void setEventId(String eventId) { this.eventId = eventId; }
	public Channel getChannel() { return channel; }
	public void setChannel(Channel channel) { this.channel = channel; }
	public String getTarget() { return target; }
	public void setTarget(String target) { this.target = target; }
	public String getPayload() { return payload; }
	public void setPayload(String payload) { this.payload = payload; }
	public String getSecret() { return secret; }
	public void setSecret(String secret) { this.secret = secret; }
	public Status getStatus() { return status; }
	public void setStatus(Status status) { this.status = status; }
	public int getAttempts() { return attempts; }
	public void setAttempts(int attempts) { this.attempts = attempts; }
	public LocalDateTime getNextAttemptAt() { return nextAttemptAt; }
	public void setNextAttemptAt(LocalDateTime nextAttemptAt) { this.nextAttemptAt = nextAttemptAt; }
	public String getLastError() { return lastError; }
	public void setLastError(String lastError) { this.lastError = lastError; }
	public Integer getLastHttpStatus() { return lastHttpStatus; }
	public void setLastHttpStatus(Integer lastHttpStatus) { this.lastHttpStatus = lastHttpStatus; }
	public LocalDateTime getCreatedAt() { return createdAt; }
	public LocalDateTime getSentAt() { return sentAt; }
	public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }
}
