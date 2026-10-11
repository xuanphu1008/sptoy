package com.toyland.notify;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.mail.internet.MimeMessage;

/**
 * Lay cac luot gui den han va gui (email qua SMTP, webhook qua HTTP). Thanh cong: SENT. That bai: tang so lan,
 * hen gio thu lai theo exponential backoff + jitter; vuot so lan toi da: DEAD (hang chet) de nguoi van hanh xem va gui lai.
 */
@Component
public class DeliveryWorker {

	private static final Logger log = LoggerFactory.getLogger(DeliveryWorker.class);

	private final DeliveryRepository repository;
	private final ObjectProvider<JavaMailSender> mailSender;
	private final ObjectMapper mapper;
	private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(4)).build();

	@Value("${notify.mail.enabled:false}")
	private boolean mailEnabled;
	@Value("${notify.mail.from:ToyLand <no-reply@toyland.vn>}")
	private String mailFrom;
	@Value("${notify.retry.max-attempts:6}")
	private int maxAttempts;
	@Value("${notify.retry.base-seconds:10}")
	private long baseSeconds;
	@Value("${notify.retry.max-seconds:600}")
	private long maxSeconds;

	public DeliveryWorker(DeliveryRepository repository, ObjectProvider<JavaMailSender> mailSender, ObjectMapper mapper) {
		this.repository = repository;
		this.mailSender = mailSender;
		this.mapper = mapper;
	}

	@Scheduled(fixedDelayString = "${notify.worker.delay-ms:3000}", initialDelay = 5000)
	public void run() {
		List<Delivery> due = repository.findDue(LocalDateTime.now(), PageRequest.of(0, 20));
		for (Delivery d : due) {
			process(d);
		}
	}

	void process(Delivery d) {
		try {
			if (d.getChannel() == Delivery.Channel.EMAIL) {
				sendEmail(d);
			} else {
				sendWebhook(d);
			}
			d.setStatus(Delivery.Status.SENT);
			d.setSentAt(LocalDateTime.now());
			d.setLastError(null);
			log.info("Da gui {} den {} (su kien {})", d.getChannel(), d.getTarget(), d.getEventId());
		} catch (Exception e) {
			d.setAttempts(d.getAttempts() + 1);
			String msg = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
			d.setLastError(msg.length() > 480 ? msg.substring(0, 480) : msg);
			if (d.getAttempts() >= maxAttempts) {
				d.setStatus(Delivery.Status.DEAD);
				log.error("{} den {} that bai {} lan, chuyen vao hang chet: {}", d.getChannel(), d.getTarget(), d.getAttempts(), msg);
			} else {
				long wait = Backoff.seconds(d.getAttempts(), baseSeconds, maxSeconds);
				d.setNextAttemptAt(LocalDateTime.now().plusSeconds(wait));
				log.warn("{} den {} that bai lan {}: {}; thu lai sau {}s", d.getChannel(), d.getTarget(), d.getAttempts(), msg, wait);
			}
		}
		repository.save(d);
	}

	private void sendEmail(Delivery d) throws Exception {
		JsonNode m = mapper.readTree(d.getPayload());
		if (!mailEnabled) {
			log.info("[MAIL tat] To: {} | Subject: {}", m.get("to").asText(), m.get("subject").asText());
			return;
		}
		JavaMailSender sender = mailSender.getIfAvailable();
		if (sender == null) {
			throw new IllegalStateException("Chua cau hinh SMTP");
		}
		MimeMessage msg = sender.createMimeMessage();
		MimeMessageHelper h = new MimeMessageHelper(msg, true, "UTF-8");
		h.setFrom(mailFrom);
		h.setTo(m.get("to").asText());
		h.setSubject(m.get("subject").asText());
		h.setText(m.get("html").asText(), true);
		sender.send(msg); // loi SMTP se nem exception -> thu lai
	}

	private void sendWebhook(Delivery d) throws Exception {
		String ts = String.valueOf(System.currentTimeMillis() / 1000);
		String sig = Signature.hmacHex(d.getSecret(), ts + "." + d.getPayload());
		HttpRequest req = HttpRequest.newBuilder(URI.create(d.getTarget())).timeout(Duration.ofSeconds(10))
				.header("Content-Type", "application/json")
				.header("X-Toyland-Event-Id", d.getEventId())
				.header("X-Toyland-Timestamp", ts)
				.header("X-Toyland-Signature", "sha256=" + sig)
				.POST(HttpRequest.BodyPublishers.ofString(d.getPayload())).build();
		HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
		d.setLastHttpStatus(res.statusCode());
		if (res.statusCode() < 200 || res.statusCode() >= 300) {
			throw new IllegalStateException("Doi tac tra HTTP " + res.statusCode());
		}
	}
}
