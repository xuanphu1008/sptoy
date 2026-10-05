package com.toyland;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.toyland.catalog.AgeGroup;
import com.toyland.common.util.Money;
import com.toyland.common.util.TextUtils;
import com.toyland.order.OrderStatus;
import com.toyland.payment.MomoUtil;

/** Unit test cac logic thuan (khong can CSDL / Spring context). Chay: mvn test */
class PureLogicTest {

	@Test
	void normalizeVietnamese() {
		assertEquals("bup be barbie", TextUtils.normalize("  Búp bê   BARBIE "));
		assertEquals("do choi dieu khien", TextUtils.normalize("Đồ chơi điều khiển"));
		assertEquals("lego-city-trung-tam-thanh-pho-60380", TextUtils.slugify("LEGO City Trung tâm thành phố 60380"));
		assertEquals("N***h", TextUtils.maskName("Nguyễn Văn Khách"));
	}

	@Test
	void money() {
		assertEquals(new BigDecimal("464000"), Money.roundThousand(new BigDecimal("463500")));
		assertEquals(new BigDecimal("91800"), Money.percentOf(new BigDecimal("459000"), 20));
	}

	@Test
	void orderTransitions() {
		assertTrue(OrderStatus.PENDING.canTransitionTo(OrderStatus.CONFIRMED));
		assertTrue(OrderStatus.SHIPPING.canTransitionTo(OrderStatus.DELIVERY_FAILED));
		assertFalse(OrderStatus.PENDING.canTransitionTo(OrderStatus.DELIVERED));
		assertFalse(OrderStatus.COMPLETED.canTransitionTo(OrderStatus.CANCELLED));
		assertTrue(OrderStatus.PENDING_PAYMENT.customerCancellable());
		assertFalse(OrderStatus.CONFIRMED.customerCancellable());
	}

	@Test
	void ageLabels() {
		assertEquals("3+", AgeGroup.labelOf(36, null));
		assertEquals("0+", AgeGroup.labelOf(0, null));
		assertEquals("18 tháng+", AgeGroup.labelOf(18, null));
		assertEquals("3-6 tuổi", AgeGroup.labelOf(36, 72));
	}

	@Test
	void momoSignatureMatchesDocumentedFormat() {
		Map<String, String> p = new LinkedHashMap<>();
		p.put("requestType", "payWithMethod");
		p.put("orderId", "TL1-120000000");
		p.put("requestId", "TL1-120000000");
		p.put("amount", "150000");
		p.put("orderInfo", "Thanh toan don hang TL1");
		p.put("partnerCode", "MOMO");
		p.put("accessKey", "F8BBA842ECF85");
		p.put("redirectUrl", "http://localhost:4200/payment/momo-return");
		p.put("ipnUrl", "http://localhost:8080/api/v1/payments/momo/ipn");
		// extraData khong co -> van phai co "extraData=" trong chuoi ky
		String raw = MomoUtil.rawSignature(MomoUtil.CREATE_FIELDS, p);
		assertEquals("accessKey=F8BBA842ECF85&amount=150000&extraData=&ipnUrl=http://localhost:8080/api/v1/payments/momo/ipn"
				+ "&orderId=TL1-120000000&orderInfo=Thanh toan don hang TL1&partnerCode=MOMO"
				+ "&redirectUrl=http://localhost:4200/payment/momo-return&requestId=TL1-120000000&requestType=payWithMethod", raw);
		// gia tri doi chieu tinh doc lap (Python hmac) voi khoa thu nghiem cong khai cua MoMo
		String sig = MomoUtil.hmacSHA256("K951B6PE1waDMi640xX08PD3vg6EkVlz", raw);
		assertEquals("eb666e14ad9f5b20450a08f0eb6a1fe2818cc8c2412025fb0a5d2b1d0ce5f2ec", sig);
		assertTrue(MomoUtil.sameSignature(sig.toUpperCase(), sig));
		assertFalse(MomoUtil.sameSignature(sig, MomoUtil.hmacSHA256("SAI_KHOA", raw)));
	}
}
