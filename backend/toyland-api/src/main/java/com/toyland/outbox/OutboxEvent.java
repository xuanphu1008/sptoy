package com.toyland.outbox;

import java.time.LocalDateTime;

import jakarta.persistence.*;

/**
 * Su kien cho gui (transactional outbox): duoc ghi CUNG GIAO DICH voi nghiep vu sinh ra no,
 * nen "don da luu" va "su kien da ghi" cung thanh cong hoac cung that bai. Bo phat (OutboxRelay) gui di sau.
 */
@Entity
@Table(name = "outbox_events")
public class OutboxEvent {

	public enum Status { NEW, SENT, DEAD }

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "event_id", nullable = false, length = 36, unique = true)
	private String eventId;

	@Column(name = "type", nullable = false, length = 60)
	private String type;

	@Column(name = "aggregate_id", nullable = false, length = 64)
	private String aggregateId;

	@Column(name = "partner_id")
	private Long partnerId;

	@Column(name = "payload", nullable = false, columnDefinition = "TEXT")
	private String payload;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 10)
	private Status status = Status.NEW;

	@Column(name = "attempts", nullable = false)
	private int attempts;

	@Column(name = "next_attempt_at", nullable = false)
	private LocalDateTime nextAttemptAt;

	@Column(name = "last_error", length = 500)
	private String lastError;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "sent_at")
	private LocalDateTime sentAt;

	@PrePersist
	void onCreate() {
		createdAt = LocalDateTime.now();
		if (nextAttemptAt == null) {
			nextAttemptAt = createdAt;
		}
	}

	public Long getId() { return id; }
	public String getEventId() { return eventId; }
	public void setEventId(String eventId) { this.eventId = eventId; }
	public String getType() { return type; }
	public void setType(String type) { this.type = type; }
	public String getAggregateId() { return aggregateId; }
	public void setAggregateId(String aggregateId) { this.aggregateId = aggregateId; }
	public Long getPartnerId() { return partnerId; }
	public void setPartnerId(Long partnerId) { this.partnerId = partnerId; }
	public String getPayload() { return payload; }
	public void setPayload(String payload) { this.payload = payload; }
	public Status getStatus() { return status; }
	public void setStatus(Status status) { this.status = status; }
	public int getAttempts() { return attempts; }
	public void setAttempts(int attempts) { this.attempts = attempts; }
	public LocalDateTime getNextAttemptAt() { return nextAttemptAt; }
	public void setNextAttemptAt(LocalDateTime nextAttemptAt) { this.nextAttemptAt = nextAttemptAt; }
	public String getLastError() { return lastError; }
	public void setLastError(String lastError) { this.lastError = lastError; }
	public LocalDateTime getCreatedAt() { return createdAt; }
	public LocalDateTime getSentAt() { return sentAt; }
	public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }
}
