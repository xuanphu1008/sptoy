package com.toyland;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.toyland.common.util.TokenHasher;
import com.toyland.common.util.ValidationPatterns;
import com.toyland.loyalty.LoyaltyRules;
import com.toyland.notification.NotificationService;
import com.toyland.payment.AdminPaymentService;
import com.toyland.common.exception.ApiException;
import com.toyland.review.KeywordModeration;

/** Unit test cho cac chuc nang mo rong: diem thuong, kiem duyet tu khoa, dinh dang tien trong thong bao. */
class NewFeaturesLogicTest {

	private final LoyaltyRules rules = new LoyaltyRules(10_000, 100, 50);

	@Test
	void tierByLifetimePoints() {
		assertEquals("MEMBER", LoyaltyRules.tierOf(0).code());
		assertEquals("MEMBER", LoyaltyRules.tierOf(499).code());
		assertEquals("SILVER", LoyaltyRules.tierOf(500).code());
		assertEquals("GOLD", LoyaltyRules.tierOf(1865).code());
		assertEquals("DIAMOND", LoyaltyRules.tierOf(10_000).code());
		assertEquals("SILVER", LoyaltyRules.nextTier(0).code());
		assertNull(LoyaltyRules.nextTier(4000));
	}

	@Test
	void earnPointsWithTierMultiplier() {
		// 1.259.000d -> 125 diem goc
		assertEquals(125, rules.pointsToEarn(new BigDecimal("1259000"), 0));
		assertEquals(137, rules.pointsToEarn(new BigDecimal("1259000"), 600)); // Bac x1.10
		assertEquals(156, rules.pointsToEarn(new BigDecimal("1259000"), 1500)); // Vang x1.25
		assertEquals(187, rules.pointsToEarn(new BigDecimal("1259000"), 4000)); // Kim cuong x1.50
		assertEquals(0, rules.pointsToEarn(new BigDecimal("9999"), 0));
		assertEquals(0, rules.pointsToEarn(BigDecimal.ZERO, 0));
		assertEquals(0, rules.pointsToEarn(null, 0));
	}

	@Test
	void redeemIsCappedByBalanceAndPercent() {
		// don 200.000d: toi da 50% = 100.000d = 1000 diem
		assertEquals(1000, rules.maxRedeemable(5000, new BigDecimal("200000")));
		assertEquals(300, rules.maxRedeemable(300, new BigDecimal("200000")));
		assertEquals(0, rules.maxRedeemable(0, new BigDecimal("200000")));
		assertEquals(0, rules.maxRedeemable(500, BigDecimal.ZERO));
		assertEquals(new BigDecimal("30000"), rules.valueOf(300));
	}

	@Test
	void rulesAreClampedToSafeValues() {
		LoyaltyRules weird = new LoyaltyRules(0, 0, 150);
		assertEquals(1, weird.earnPerAmount());
		assertEquals(1, weird.pointValue());
		assertEquals(100, weird.maxRedeemPercent());
	}

	@Test
	void bannedKeywordsMatchWholeWordsWithoutAccents() {
		KeywordModeration m = new KeywordModeration(List.of("lừa đảo", "Hàng giả", "dm", "fake"));
		assertEquals(List.of("lua dao", "hang gia"), m.find("Hàng GIẢ, shop LỪA   ĐẢO!"));
		assertEquals(List.of("fake"), m.find("Đồ FAKE 100%"));
		assertTrue(m.find("Admin shop tư vấn nhiệt tình, hàng chính hãng").isEmpty(), "khong bat 'dm' trong 'admin'");
		assertTrue(m.find("Bé rất thích, giao hàng nhanh").isEmpty());
		assertTrue(m.find(null).isEmpty());
		assertTrue(new KeywordModeration(List.of()).find("lừa đảo").isEmpty());
		assertEquals("hang dom", KeywordModeration.normalizeKeyword("  Hàng   DỞM "));
	}

	@Test
	void vndFormatInNotifications() {
		assertEquals("1.250.000đ", NotificationService.vnd(new BigDecimal("1250000")));
		assertEquals("999đ", NotificationService.vnd(new BigDecimal("999")));
		assertEquals("0đ", NotificationService.vnd(null));
		assertEquals("-50.000đ", NotificationService.vnd(new BigDecimal("-50000")));
		assertEquals("1.000đ", NotificationService.vnd(new BigDecimal("999.6")));
	}

	@Test
	void emailVerificationTokenIsRandomAndHashed() {
		String a = TokenHasher.newToken();
		String b = TokenHasher.newToken();
		assertEquals(43, a.length());
		assertTrue(a.matches("^[A-Za-z0-9_-]+$"), "token dung duoc truc tiep tren URL");
		assertNotEquals(a, b);
		// vector chuan cua SHA-256
		assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad", TokenHasher.sha256("abc"));
		assertEquals(64, TokenHasher.sha256(a).length());
		assertNotEquals(a, TokenHasher.sha256(a));
	}

	@Test
	void emailPattern() {
		assertTrue("nxphus1082004@gmail.com".matches(ValidationPatterns.EMAIL));
		assertTrue("a_b@truong.edu.vn".matches(ValidationPatterns.EMAIL));
		assertFalse("abc@gmail".matches(ValidationPatterns.EMAIL));
		assertFalse("abc@@gmail.com".matches(ValidationPatterns.EMAIL));
		assertFalse("ab c@gmail.com".matches(ValidationPatterns.EMAIL));
	}

	@Test
	void paymentReconciliationPeriodAndLabels() {
		AdminPaymentService.Period p = AdminPaymentService.period(null, null);
		assertEquals(LocalDate.now(), p.to());
		assertEquals(LocalDate.now().minusDays(29), p.from());
		assertThrows(ApiException.class, () -> AdminPaymentService.period(LocalDate.of(2026, 9, 30), LocalDate.of(2026, 9, 1)));
		assertThrows(ApiException.class, () -> AdminPaymentService.period(LocalDate.of(2024, 1, 1), LocalDate.of(2026, 1, 1)));
		assertEquals("Chuyển khoản", AdminPaymentService.label("BANK_TRANSFER"));
		assertEquals("Thu hộ COD", AdminPaymentService.label("COD"));
		assertEquals("Ví MoMo", AdminPaymentService.label("MOMO"));
	}
}
