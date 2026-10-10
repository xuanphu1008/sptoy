package com.toyland.partner;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/** Kiem thu cong xac thuc API doi tac: chu ky HMAC, chong phat lai, han muc, doi tac bi tat. */
class PartnerAuthFilterTest {

	private PartnerRepository repo;
	private PartnerAuthFilter filter;
	private Partner partner;

	@BeforeEach
	void setUp() {
		repo = mock(PartnerRepository.class);
		partner = new Partner();
		partner.setName("Doi tac A");
		partner.setApiKey("tlp_abc");
		partner.setSecret("s3cret");
		partner.setRateLimitPerMin(3);
		when(repo.findByApiKey("tlp_abc")).thenReturn(Optional.of(partner));
		filter = new PartnerAuthFilter(repo);
	}

	private MockHttpServletRequest signed(String method, String uri, String query, long ts, String secret) {
		MockHttpServletRequest r = new MockHttpServletRequest(method, uri);
		r.setQueryString(query);
		String full = uri + (query != null ? "?" + query : "");
		r.addHeader("X-Api-Key", "tlp_abc");
		r.addHeader("X-Timestamp", String.valueOf(ts));
		r.addHeader("X-Signature", PartnerSigner.sign(secret, PartnerSigner.canonical(String.valueOf(ts), method, full, new byte[0])));
		return r;
	}

	private MockHttpServletResponse run(MockHttpServletRequest req) throws Exception {
		MockHttpServletResponse res = new MockHttpServletResponse();
		filter.doFilter(req, res, new MockFilterChain());
		return res;
	}

	@Test
	void chuKyDungDuocChapNhan() throws Exception {
		MockHttpServletResponse res = run(signed("GET", "/api/partner/v1/products", "page=0&size=5", Instant.now().getEpochSecond(), "s3cret"));
		assertEquals(200, res.getStatus());
		verify(repo).save(any(Partner.class));
	}

	@Test
	void chuKySaiBiTuChoi() throws Exception {
		MockHttpServletResponse res = run(signed("GET", "/api/partner/v1/products", null, Instant.now().getEpochSecond(), "sai-secret"));
		assertEquals(401, res.getStatus());
		assertTrue(res.getContentAsString().contains("PARTNER_AUTH_FAILED"));
	}

	@Test
	void doiQueryStringLamHongChuKy() throws Exception {
		long ts = Instant.now().getEpochSecond();
		MockHttpServletRequest r = signed("GET", "/api/partner/v1/products", "size=5", ts, "s3cret");
		r.setQueryString("size=100"); // ke tan cong sua tham so sau khi ky
		assertEquals(401, run(r).getStatus());
	}

	@Test
	void timestampCuBiTuChoiChongPhatLai() throws Exception {
		MockHttpServletResponse res = run(signed("GET", "/api/partner/v1/products", null, Instant.now().getEpochSecond() - 600, "s3cret"));
		assertEquals(401, res.getStatus());
		assertTrue(res.getContentAsString().contains("PARTNER_TIMESTAMP_EXPIRED"));
	}

	@Test
	void thieuHeaderBiTuChoi() throws Exception {
		MockHttpServletResponse res = run(new MockHttpServletRequest("GET", "/api/partner/v1/products"));
		assertEquals(401, res.getStatus());
		assertTrue(res.getContentAsString().contains("PARTNER_AUTH_MISSING"));
	}

	@Test
	void doiTacBiTatBiTuChoi() throws Exception {
		partner.setActive(false);
		assertEquals(401, run(signed("GET", "/api/partner/v1/products", null, Instant.now().getEpochSecond(), "s3cret")).getStatus());
	}

	@Test
	void vuotHanMucTra429() throws Exception {
		long ts = Instant.now().getEpochSecond();
		for (int i = 0; i < 3; i++) {
			assertEquals(200, run(signed("GET", "/api/partner/v1/products", null, ts, "s3cret")).getStatus());
		}
		MockHttpServletResponse res = run(signed("GET", "/api/partner/v1/products", null, ts, "s3cret"));
		assertEquals(429, res.getStatus());
		assertEquals("60", res.getHeader("Retry-After"));
	}

	@Test
	void duongDanNgoaiPartnerKhongBiChan() throws Exception {
		assertEquals(200, run(new MockHttpServletRequest("GET", "/api/v1/products")).getStatus());
	}
}
