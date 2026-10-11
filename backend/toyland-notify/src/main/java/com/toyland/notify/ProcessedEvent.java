package com.toyland.notify;

import java.time.LocalDateTime;

import jakarta.persistence.*;

/** Su kien da nhan. Khoa chinh = eventId nen nhan trung (do ben gui thu lai) khong xu ly hai lan: consumer idempotent. */
@Entity
@Table(name = "processed_events")
public class ProcessedEvent {

	@Id
	@Column(name = "event_id", length = 36)
	private String eventId;

	@Column(name = "type", nullable = false, length = 60)
	private String type;

	@Column(name = "received_at", nullable = false)
	private LocalDateTime receivedAt = LocalDateTime.now();

	protected ProcessedEvent() {
	}

	public ProcessedEvent(String eventId, String type) {
		this.eventId = eventId;
		this.type = type;
	}

	public String getEventId() { return eventId; }
	public String getType() { return type; }
	public LocalDateTime getReceivedAt() { return receivedAt; }
}
