package com.toyland.notify;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;

/** Kiem tra luong nhan su kien (chong trung) va gui webhook (thu lai, hang chet, chu ky) tren CSDL H2 + may chu HTTP gia lap doi tac. */
@DataJpaTest
@Import({ EventService.class, ObjectMapper.class })
@TestPropertySource(properties = { "spring.jpa.hibernate.ddl-auto=create-drop",
		"spring.datasource.url=jdbc:h2:mem:notify;MODE=MySQL;DB_CLOSE_DELAY=-1" })
class DeliveryFlowTest {

	@Autowired EventService events;
	@Autowired DeliveryRepository deliveries;
	@Autowired ObjectMapper mapper;

	private HttpServer partner;
	private final AtomicInteger hits = new AtomicInteger();
	private final AtomicInteger failFirst = new AtomicInteger();
	private final AtomicReference<String> lastSignature = new AtomicReference<>();
	private final AtomicReference<String> lastTimestamp = new AtomicReference<>();
	private final AtomicReference<String> lastBody = new AtomicReference<>();

	@BeforeEach
	void startPartner() throws Exception {
		partner = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
		partner.createContext("/hook", ex -> {
			hits.incrementAndGet();
			lastSignature.set(ex.getRequestHeaders().getFirst("X-Toyland-Signature"));
			lastTimestamp.set(ex.getRequestHeaders().getFirst("X-Toyland-Timestamp"));
			lastBody.set(new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
			int code = failFirst.getAndDecrement() > 0 ? 500 : 200;
			ex.sendResponseHeaders(code, -1);
			ex.close();
		});
		partner.start();
	}

	@AfterEach
	void stop() {
		partner.stop(0);
	}

	private String url() {
		return "http://127.0.0.1:" + partner.getAddress().getPort() + "/hook";
	}

	private com.fasterxml.jackson.databind.JsonNode event(String id) throws Exception {
		return mapper.readTree("{\"eventId\":\"" + id + "\",\"type\":\"order.status_changed\",\"occurredAt\":\"2026-10-11T10:00:00\","
				+ "\"email\":{\"to\":\"a@b.vn\",\"subject\":\"s\",\"html\":\"<p>x</p>\"},"
				+ "\"webhook\":{\"url\":\"" + url() + "\",\"secret\":\"s3cret\",\"data\":{\"orderCode\":\"TL1\",\"status\":\"CONFIRMED\"}}}");
	}

	private DeliveryWorker worker(int maxAttempts) {
		@SuppressWarnings("unchecked")
		ObjectProvider<JavaMailSender> mail = Mockito.mock(ObjectProvider.class);
		DeliveryWorker w = new DeliveryWorker(deliveries, mail, mapper);
		ReflectionTestUtils.setField(w, "mailEnabled", false);
		ReflectionTestUtils.setField(w, "mailFrom", "x");
		ReflectionTestUtils.setField(w, "maxAttempts", maxAttempts);
		ReflectionTestUtils.setField(w, "baseSeconds", 1L);
		ReflectionTestUtils.setField(w, "maxSeconds", 2L);
		return w;
	}

	@Test
	void nhanTrungChiXuLyMotLan() throws Exception {
		assertEquals(EventService.Result.ACCEPTED, events.accept(event("e-1")));
		assertEquals(EventService.Result.DUPLICATE, events.accept(event("e-1")));
		assertEquals(2, deliveries.findByEventId("e-1").size(), "1 email + 1 webhook, khong nhan doi");
	}

	@Test
	void webhookGuiThanhCongKemChuKyDungVaBodyKhongLoEmail() throws Exception {
		events.accept(event("e-2"));
		DeliveryWorker w = worker(3);
		deliveries.findByEventId("e-2").forEach(w::process);
		assertEquals(1, hits.get());
		String ts = lastTimestamp.get();
		assertEquals("sha256=" + Signature.hmacHex("s3cret", ts + "." + lastBody.get()), lastSignature.get());
		assertTrue(lastBody.get().contains("\"orderCode\":\"TL1\""));
		assertTrue(!lastBody.get().contains("a@b.vn") && !lastBody.get().contains("<p>"), "khong lo noi dung email cho doi tac");
		deliveries.findByEventId("e-2").forEach(d -> assertEquals(Delivery.Status.SENT, d.getStatus()));
	}

	@Test
	void doiTacLoiThiThuLaiRoiThanhCong() throws Exception {
		failFirst.set(1);
		events.accept(event("e-3"));
		DeliveryWorker w = worker(5);
		Delivery d = deliveries.findByEventId("e-3").stream().filter(x -> x.getChannel() == Delivery.Channel.WEBHOOK).findFirst().orElseThrow();
		w.process(d);
		assertEquals(Delivery.Status.PENDING, d.getStatus());
		assertEquals(1, d.getAttempts());
		assertEquals(500, d.getLastHttpStatus());
		assertNotNull(d.getLastError());
		w.process(d);
		assertEquals(Delivery.Status.SENT, d.getStatus());
		assertEquals(2, hits.get());
	}

	@Test
	void quaSoLanThiVaoHangChet() throws Exception {
		failFirst.set(99);
		events.accept(event("e-4"));
		DeliveryWorker w = worker(3);
		Delivery d = deliveries.findByEventId("e-4").stream().filter(x -> x.getChannel() == Delivery.Channel.WEBHOOK).findFirst().orElseThrow();
		for (int i = 0; i < 3; i++) {
			w.process(d);
		}
		assertEquals(Delivery.Status.DEAD, d.getStatus());
		assertEquals(3, hits.get());
	}
}
