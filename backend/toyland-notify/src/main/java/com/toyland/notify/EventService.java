package com.toyland.notify;

import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Nhan mot su kien tu toyland-api: ghi nhan eventId (chong xu ly trung) roi tao cac luot gui.
 * Toan bo trong mot giao dich: hoac co du "da nhan" va cac luot gui, hoac khong co gi.
 */
@Service
public class EventService {

	public enum Result { ACCEPTED, DUPLICATE }

	private final ProcessedEventRepository processed;
	private final DeliveryRepository deliveries;
	private final ObjectMapper mapper;

	public EventService(ProcessedEventRepository processed, DeliveryRepository deliveries, ObjectMapper mapper) {
		this.processed = processed;
		this.deliveries = deliveries;
		this.mapper = mapper;
	}

	@Transactional
	public Result accept(JsonNode event) throws Exception {
		String eventId = event.path("eventId").asText("");
		String type = event.path("type").asText("");
		if (eventId.isBlank() || eventId.length() > 36 || type.isBlank()) {
			throw new IllegalArgumentException("Thieu eventId hoac type");
		}
		if (processed.existsById(eventId)) {
			return Result.DUPLICATE;
		}
		try {
			processed.saveAndFlush(new ProcessedEvent(eventId, type));
		} catch (DataIntegrityViolationException dup) {
			return Result.DUPLICATE; // hai yeu cau trung den cung luc: chi mot yeu cau thang
		}
		JsonNode email = event.get("email");
		if (email != null && email.hasNonNull("to")) {
			Delivery d = new Delivery();
			d.setEventId(eventId);
			d.setChannel(Delivery.Channel.EMAIL);
			d.setTarget(email.get("to").asText());
			d.setPayload(mapper.writeValueAsString(email));
			deliveries.save(d);
		}
		JsonNode webhook = event.get("webhook");
		if (webhook != null && webhook.hasNonNull("url") && webhook.hasNonNull("secret")) {
			Delivery d = new Delivery();
			d.setEventId(eventId);
			d.setChannel(Delivery.Channel.WEBHOOK);
			d.setTarget(webhook.get("url").asText());
			d.setSecret(webhook.get("secret").asText());
			// than gui doi tac: eventId + type + thoi diem + du lieu rieng cua doi tac (khong co email, HTML cua khach)
			d.setPayload(mapper.writeValueAsString(Map.of("eventId", eventId, "type", type,
					"occurredAt", event.path("occurredAt").asText(""), "data", webhook.path("data"))));
			deliveries.save(d);
		}
		return Result.ACCEPTED;
	}
}
