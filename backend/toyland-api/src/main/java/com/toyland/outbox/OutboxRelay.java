package com.toyland.outbox;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.toyland.notification.MailService;
import com.toyland.notification.MailService.Email;
import com.toyland.partner.PartnerWebhookApi;

/**
 * Bo phat su kien: lay cac su kien den han trong outbox va chuyen sang dich vu thong bao (toyland-notify)
 * bang HTTP co ky HMAC. Loi mang / dich vu thong bao sap thi thu lai voi thoi gian cho tang dan (exponential backoff + jitter);
 * qua so lan toi da thi danh dau DEAD de nguoi quan tri xem lai. Dat hang khong bao gio bi chan boi viec nay.
 * <p>
 * Neu app.notify.url de trong (chua trien khai toyland-notify): thu gui cho khach duoc gui truc tiep bang SMTP noi bo
 * de khong mat chuc nang; webhook doi tac bi bo qua vi can dich vu thong bao.
 */
@Component
public class OutboxRelay {

	private static final Logger log = LoggerFactory.getLogger(OutboxRelay.class);
	private static final long BASE_DELAY_SECONDS = 5;
	private static final long MAX_DELAY_SECONDS = 900;

	private final OutboxEventRepository repository;
	private final PartnerWebhookApi partners;
	private final MailService mailService;
	private final ObjectMapper mapper;
	private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();

	@Value("${app.notify.url:}")
	private String notifyUrl;

	@Value("${app.notify.token:}")
	private String notifyToken;

	@Value("${app.outbox.batch:50}")
	private int batch;

	@Value("${app.outbox.max-attempts:8}")
	private int maxAttempts;

	public OutboxRelay(OutboxEventRepository repository, PartnerWebhookApi partners, MailService mailService, ObjectMapper mapper) {
		this.repository = repository;
		this.partners = partners;
		this.mailService = mailService;
		this.mapper = mapper;
	}

	@Scheduled(fixedDelayString = "${app.outbox.relay-delay-ms:4000}", initialDelay = 20_000)
	public void relay() {
		List<OutboxEvent> due = repository.findDue(LocalDateTime.now(), PageRequest.of(0, batch));
		for (OutboxEvent e : due) {
			try {
				if (notifyUrl == null || notifyUrl.isBlank()) {
					deliverLocally(e);
				} else {
					sendToNotify(e);
				}
				e.setStatus(OutboxEvent.Status.SENT);
				e.setSentAt(LocalDateTime.now());
				e.setLastError(null);
			} catch (Exception ex) {
				fail(e, ex);
			}
			repository.save(e);
		}
	}

	private void fail(OutboxEvent e, Exception ex) {
		e.setAttempts(e.getAttempts() + 1);
		String msg = ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage();
		e.setLastError(msg.length() > 480 ? msg.substring(0, 480) : msg);
		if (e.getAttempts() >= maxAttempts) {
			e.setStatus(OutboxEvent.Status.DEAD);
			log.error("Su kien {} ({}) that bai {} lan, chuyen sang DEAD: {}", e.getEventId(), e.getType(), e.getAttempts(), msg);
			return;
		}
		long delay = Math.min(BASE_DELAY_SECONDS << Math.min(e.getAttempts(), 10), MAX_DELAY_SECONDS);
		long jitterMs = ThreadLocalRandom.current().nextLong(0, 2000);
		e.setNextAttemptAt(LocalDateTime.now().plusSeconds(delay).plusNanos(jitterMs * 1_000_000));
		log.warn("Su kien {} ({}) gui that bai lan {}: {}; thu lai sau {}s", e.getEventId(), e.getType(), e.getAttempts(), msg, delay);
	}

	private void deliverLocally(OutboxEvent e) throws Exception {
		JsonNode payload = mapper.readTree(e.getPayload());
		JsonNode email = payload.get("email");
		if (email != null && !email.isNull()) {
			mailService.deliver(new Email(email.get("to").asText(), email.get("subject").asText(), email.get("html").asText()));
		}
	}

	private void sendToNotify(OutboxEvent e) throws Exception {
		JsonNode payload = mapper.readTree(e.getPayload());
		Map<String, Object> envelope = new LinkedHashMap<>();
		envelope.put("eventId", e.getEventId());
		envelope.put("type", e.getType());
		envelope.put("occurredAt", e.getCreatedAt().toString());
		envelope.put("email", payload.get("email"));
		// dia chi + secret webhook lay luc gui de doi URL co hieu luc ngay, khong luu secret trong bang outbox
		if (e.getPartnerId() != null && payload.hasNonNull("partner")) {
			partners.targetOf(e.getPartnerId()).ifPresent(t -> {
				Map<String, Object> wh = new LinkedHashMap<>();
				wh.put("url", t.url());
				wh.put("secret", t.secret());
				wh.put("data", payload.get("partner"));
				envelope.put("webhook", wh);
			});
		}
		String body = mapper.writeValueAsString(envelope);
		String ts = String.valueOf(System.currentTimeMillis() / 1000);
		HttpRequest req = HttpRequest.newBuilder(URI.create(notifyUrl.replaceAll("/+$", "") + "/internal/events"))
				.timeout(Duration.ofSeconds(10)).header("Content-Type", "application/json")
				.header("X-Internal-Timestamp", ts)
				.header("X-Internal-Signature", Hmac.sha256Hex(notifyToken, ts + "." + body))
				.POST(HttpRequest.BodyPublishers.ofString(body)).build();
		HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
		if (res.statusCode() >= 300) {
			throw new IllegalStateException("toyland-notify tra " + res.statusCode());
		}
	}
}
