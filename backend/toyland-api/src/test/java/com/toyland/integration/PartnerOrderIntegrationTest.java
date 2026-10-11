package com.toyland.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.toyland.catalog.Category;
import com.toyland.catalog.CategoryRepository;
import com.toyland.catalog.Product;
import com.toyland.catalog.ProductRepository;
import com.toyland.catalog.ProductVariant;
import com.toyland.catalog.ProductVariantRepository;
import com.toyland.partner.Partner;
import com.toyland.partner.PartnerRepository;
import com.toyland.partner.PartnerSigner;

/**
 * Kiem thu tich hop tren H2 (che do MySQL): di qua bo loc xac thuc HMAC that, idempotency, dat don va tru kho.
 * Moi test dung SKU rieng nen khong phu thuoc thu tu chay.
 */
@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:toy;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE;NON_KEYWORDS=VALUE,KEY,YEAR,MONTH,DAY",
		"spring.datasource.username=sa", "spring.datasource.password=",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.jpa.hibernate.ddl-auto=create-drop",
		"spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
		"app.outbox.relay-delay-ms=3600000" })
@AutoConfigureMockMvc
class PartnerOrderIntegrationTest {

	@Autowired MockMvc mvc;
	@Autowired ObjectMapper json;
	@Autowired PartnerRepository partners;
	@Autowired CategoryRepository categories;
	@Autowired ProductRepository products;
	@Autowired ProductVariantRepository variants;
	@Autowired com.toyland.outbox.OutboxEventRepository outbox;
	@Autowired com.toyland.outbox.OutboxService outboxService;

	Partner a;
	Partner b;

	@BeforeEach
	void setUp() {
		a = partner("Dai ly A");
		b = partner("Dai ly B");
	}

	private Partner partner(String name) {
		Partner p = new Partner();
		p.setName(name);
		p.setApiKey("tlp_" + UUID.randomUUID().toString().replace("-", "").substring(0, 20));
		p.setSecret(UUID.randomUUID().toString().replace("-", ""));
		p.setRateLimitPerMin(6000);
		return partners.save(p);
	}

	private String variant(int stock) {
		Category c = new Category();
		c.setName("Loai " + UUID.randomUUID());
		c.setSlug("loai-" + UUID.randomUUID());
		c = categories.save(c);
		Product p = new Product();
		p.setName("San pham test");
		p.setSlug("sp-" + UUID.randomUUID());
		p.setCategory(c);
		p = products.save(p);
		ProductVariant v = new ProductVariant();
		v.setProduct(p);
		String sku = "T" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
		v.setSku(sku);
		v.setPrice(new BigDecimal("100000"));
		v.setStockQuantity(stock);
		v.setDefaultVariant(true);
		variants.save(v);
		return sku;
	}

	private int stockOf(String sku) {
		return variants.findAll().stream().filter(v -> sku.equals(v.getSku())).findFirst().orElseThrow().getStockQuantity();
	}

	private String body(String ref, String sku, int qty) throws Exception {
		return json.writeValueAsString(java.util.Map.of("externalRef", ref, "receiverName", "Nguyen Van A", "phone", "0901234567",
				"province", "Ha Noi", "district", "Cau Giay", "ward", "Dich Vong", "addressDetail", "So 1 ngo 2",
				"items", List.of(java.util.Map.of("sku", sku, "quantity", qty))));
	}

	private MvcResult call(Partner who, String method, String path, String key, String body) throws Exception {
		String ts = String.valueOf(System.currentTimeMillis() / 1000);
		byte[] raw = body == null ? new byte[0] : body.getBytes(StandardCharsets.UTF_8);
		String sig = PartnerSigner.sign(who.getSecret(), PartnerSigner.canonical(ts, method, path, raw));
		var rb = "POST".equals(method) ? post(path) : get(path);
		rb.header("X-Api-Key", who.getApiKey()).header("X-Timestamp", ts).header("X-Signature", sig);
		if (key != null) {
			rb.header("Idempotency-Key", key);
		}
		if (body != null) {
			rb.contentType(MediaType.APPLICATION_JSON).content(raw);
		}
		return mvc.perform(rb).andReturn();
	}

	private JsonNode read(MvcResult r) throws Exception {
		return json.readTree(r.getResponse().getContentAsString(StandardCharsets.UTF_8));
	}

	@Test
	void datDon_truKho_vaGuiLaiCungKhoaThiKhongTaoDonThuHai() throws Exception {
		String sku = variant(5);
		String key = "key-" + UUID.randomUUID();
		String body = body("REF-" + UUID.randomUUID(), sku, 2);

		MvcResult first = call(a, "POST", "/api/partner/v1/orders", key, body);
		assertEquals(201, first.getResponse().getStatus());
		assertEquals(3, stockOf(sku), "đặt 2 trong 5 thì còn 3");

		MvcResult replay = call(a, "POST", "/api/partner/v1/orders", key, body);
		assertEquals(read(first).path("orderCode").asText(), read(replay).path("orderCode").asText(), "gửi lại cùng khóa phải nhận đúng đơn cũ");
		assertEquals(3, stockOf(sku), "gửi lại không được trừ kho lần nữa");
	}

	@Test
	void cungKhoaNhungNoiDungKhac_biTuChoi409() throws Exception {
		String sku = variant(10);
		String key = "key-" + UUID.randomUUID();
		call(a, "POST", "/api/partner/v1/orders", key, body("REF-" + UUID.randomUUID(), sku, 1));
		MvcResult r = call(a, "POST", "/api/partner/v1/orders", key, body("REF-" + UUID.randomUUID(), sku, 3));
		assertEquals(409, r.getResponse().getStatus());
		assertEquals("IDEMPOTENCY_KEY_REUSED", read(r).path("code").asText());
		assertEquals(9, stockOf(sku));
	}

	@Test
	void trungExternalRefVoiKhoaMoi_biTuChoi409() throws Exception {
		String sku = variant(10);
		String ref = "REF-" + UUID.randomUUID();
		assertEquals(201, call(a, "POST", "/api/partner/v1/orders", "k1-" + UUID.randomUUID(), body(ref, sku, 1)).getResponse().getStatus());
		MvcResult r = call(a, "POST", "/api/partner/v1/orders", "k2-" + UUID.randomUUID(), body(ref, sku, 1));
		assertEquals(409, r.getResponse().getStatus());
		assertEquals("PARTNER_REF_DUPLICATE", read(r).path("code").asText());
		assertEquals(9, stockOf(sku), "đơn trùng không được trừ kho");
	}

	@Test
	void thieuIdempotencyKey_biTuChoi400() throws Exception {
		MvcResult r = call(a, "POST", "/api/partner/v1/orders", null, body("REF-" + UUID.randomUUID(), variant(3), 1));
		assertEquals(400, r.getResponse().getStatus());
		assertEquals("IDEMPOTENCY_KEY_REQUIRED", read(r).path("code").asText());
	}

	@Test
	void doiTacKhongXemDuocDonCuaDoiTacKhac() throws Exception {
		String sku = variant(3);
		MvcResult created = call(a, "POST", "/api/partner/v1/orders", "k-" + UUID.randomUUID(), body("REF-" + UUID.randomUUID(), sku, 1));
		String code = read(created).path("orderCode").asText();

		assertEquals(200, call(a, "GET", "/api/partner/v1/orders/" + code, null, null).getResponse().getStatus());
		int other = call(b, "GET", "/api/partner/v1/orders/" + code, null, null).getResponse().getStatus();
		assertTrue(other == 404 || other == 403, "đối tác B không được thấy đơn của A, nhưng nhận " + other);
		assertEquals(404, other, "trả 404 để không lộ việc mã đơn có tồn tại");
	}

	@Test
	void saiChuKy_biTuChoi401() throws Exception {
		String ts = String.valueOf(System.currentTimeMillis() / 1000);
		var res = mvc.perform(get("/api/partner/v1/products").header("X-Api-Key", a.getApiKey()).header("X-Timestamp", ts)
				.header("X-Signature", "00".repeat(32))).andReturn().getResponse();
		assertEquals(401, res.getStatus());
	}

	@Test
	void haiDonTranhNhauMotCaiCuoiCung_chiMotDonThanhCong() throws Exception {
		String sku = variant(1);
		int threads = 6;
		ExecutorService pool = Executors.newFixedThreadPool(threads);
		CountDownLatch start = new CountDownLatch(1);
		List<Future<Integer>> results = new ArrayList<>();
		for (int i = 0; i < threads; i++) {
			final String ref = "REF-" + UUID.randomUUID();
			final String key = "k-" + UUID.randomUUID();
			results.add(pool.submit(() -> {
				start.await();
				return call(a, "POST", "/api/partner/v1/orders", key, body(ref, sku, 1)).getResponse().getStatus();
			}));
		}
		start.countDown();
		int created = 0;
		for (Future<Integer> f : results) {
			int s = f.get();
			if (s == 201) {
				created++;
			} else {
				assertTrue(s == 422 || s == 409, "thua cuộc phải là 422/409 chứ không phải lỗi máy chủ, nhận " + s);
			}
		}
		pool.shutdown();
		assertEquals(1, created, "chỉ 1 đơn được thành công khi chỉ còn 1 sản phẩm");
		assertEquals(0, stockOf(sku), "tồn kho không được âm");
	}

	@Test
	void datDonGhiSuKienOutboxCungGiaoDich() throws Exception {
		String sku = variant(2);
		MvcResult created = call(a, "POST", "/api/partner/v1/orders", "k-" + UUID.randomUUID(), body("REF-" + UUID.randomUUID(), sku, 1));
		String code = read(created).path("orderCode").asText();
		boolean found = outbox.findTop20ByOrderByIdDesc().stream()
				.anyMatch(e -> "order.placed".equals(e.getType()) && code.equals(e.getAggregateId()) && a.getId().equals(e.getPartnerId()));
		assertTrue(found, "đơn đã tạo thì phải có sự kiện order.placed trong outbox");
	}

	@Test
	void ghiOutboxNgoaiGiaoDichBiChan() {
		org.junit.jupiter.api.Assertions.assertThrows(org.springframework.transaction.IllegalTransactionStateException.class,
				() -> outboxService.publish("order.placed", "X", null, null, java.util.Map.of("a", 1)));
	}
}
