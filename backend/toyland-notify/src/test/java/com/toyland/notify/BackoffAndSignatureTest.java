package com.toyland.notify;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class BackoffAndSignatureTest {

	@Test
	void backoffTangDanVaCoTran() {
		long prev = 0;
		for (int attempt = 1; attempt <= 4; attempt++) {
			long s = Backoff.seconds(attempt, 10, 600);
			long min = 10L << (attempt - 1);
			assertTrue(s >= min, "lan " + attempt + " phai cho it nhat " + min + "s, thuc te " + s);
			assertTrue(s <= min + min / 4, "jitter toi da 25%: " + s);
			assertTrue(s > prev);
			prev = s;
		}
		assertEquals(600, Backoff.seconds(30, 10, 600), "khong vuot tran");
	}

	@Test
	void chuKyHmacKhopVoiGiaTriThamChieu() {
		// HMAC-SHA256("key", "The quick brown fox jumps over the lazy dog") theo RFC 4231 / Wikipedia
		assertEquals("f7bc83f430538424b13298e6aa6fb143ef4d59a14946175997479dbc2d1a3cd8",
				Signature.hmacHex("key", "The quick brown fox jumps over the lazy dog"));
	}

	@Test
	void soSanhChuKy() {
		assertTrue(Signature.equal("abc", "abc"));
		assertFalse(Signature.equal("abc", "abd"));
		assertNotEquals(Signature.hmacHex("a", "x"), Signature.hmacHex("b", "x"));
	}
}
