package com.toyland.notify;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

/** Giao dien noi bo cua toyland-notify. Moi yeu cau phai ky HMAC bang notify.token (X-Internal-Timestamp, X-Internal-Signature). */
@RestController
@RequestMapping("/internal")
public class EventController {

	private static final long MAX_SKEW_SECONDS = 300;

	private final EventService events;
	private final DeliveryRepository deliveries;
	private final ObjectMapper mapper;

	@Value("${notify.token:}")
	private String token;

	public EventController(EventService events, DeliveryRepository deliveries, ObjectMapper mapper) {
		this.events = events;
		this.deliveries = deliveries;
		this.mapper = mapper;
	}

	@PostMapping("/events")
	public ResponseEntity<Map<String, Object>> receive(@RequestHeader(value = "X-Internal-Timestamp", required = false) String ts,
			@RequestHeader(value = "X-Internal-Signature", required = false) String sig, @RequestBody String body) throws Exception {
		if (!authentic(ts, sig, body)) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "UNAUTHORIZED"));
		}
		try {
			EventService.Result r = events.accept(mapper.readTree(body));
			return ResponseEntity.status(r == EventService.Result.DUPLICATE ? HttpStatus.OK : HttpStatus.ACCEPTED)
					.body(Map.of("status", r.name().toLowerCase()));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
		}
	}

	@GetMapping("/deliveries")
	public ResponseEntity<Object> list(@RequestHeader(value = "X-Internal-Timestamp", required = false) String ts,
			@RequestHeader(value = "X-Internal-Signature", required = false) String sig,
			@RequestParam(defaultValue = "DEAD") Delivery.Status status) {
		if (!authentic(ts, sig, "")) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		List<Delivery> list = deliveries.findTop50ByStatusOrderByIdDesc(status);
		return ResponseEntity.ok(Map.of("pending", deliveries.countByStatus(Delivery.Status.PENDING),
				"sent", deliveries.countByStatus(Delivery.Status.SENT), "dead", deliveries.countByStatus(Delivery.Status.DEAD),
				"items", list.stream().map(d -> Map.of("id", d.getId(), "eventId", d.getEventId(), "channel", d.getChannel(),
						"target", d.getTarget(), "attempts", d.getAttempts(), "lastError", String.valueOf(d.getLastError()))).toList()));
	}

	@PostMapping("/deliveries/{id}/retry")
	public ResponseEntity<Map<String, String>> retry(@RequestHeader(value = "X-Internal-Timestamp", required = false) String ts,
			@RequestHeader(value = "X-Internal-Signature", required = false) String sig, @PathVariable Long id) {
		if (!authentic(ts, sig, "")) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		return deliveries.findById(id).map(d -> {
			d.setStatus(Delivery.Status.PENDING);
			d.setAttempts(0);
			d.setNextAttemptAt(java.time.LocalDateTime.now());
			deliveries.save(d);
			return ResponseEntity.ok(Map.of("status", "queued"));
		}).orElseGet(() -> ResponseEntity.notFound().build());
	}

	private boolean authentic(String ts, String sig, String body) {
		if (token == null || token.isBlank() || ts == null || sig == null) {
			return false;
		}
		try {
			if (Math.abs(System.currentTimeMillis() / 1000 - Long.parseLong(ts.trim())) > MAX_SKEW_SECONDS) {
				return false;
			}
		} catch (NumberFormatException e) {
			return false;
		}
		return Signature.equal(Signature.hmacHex(token, ts.trim() + "." + body), sig.trim().toLowerCase());
	}
}
