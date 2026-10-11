package com.toyland.payment;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Ham tien ich ky / kiem tra chu ky MoMo (HMAC-SHA256) theo tai lieu MoMo Payment API v2
 * (https://developers.momo.vn). Chuoi ky la cac cap "ten=gia tri" xep theo thu tu chu cai cua ten,
 * noi bang "&", gia tri giu nguyen (khong URL-encode).
 */
public final class MomoUtil {

	/** Thu tu truong khi ky yeu cau tao giao dich. */
	public static final String[] CREATE_FIELDS = { "accessKey", "amount", "extraData", "ipnUrl", "orderId", "orderInfo", "partnerCode",
			"redirectUrl", "requestId", "requestType" };

	/** Thu tu truong khi kiem tra ket qua MoMo tra ve (redirect + IPN). */
	public static final String[] RESULT_FIELDS = { "accessKey", "amount", "extraData", "message", "orderId", "orderInfo", "orderType",
			"partnerCode", "payType", "requestId", "responseTime", "resultCode", "transId" };

	/** Thu tu truong khi ky yeu cau truy van trang thai giao dich. */
	public static final String[] QUERY_FIELDS = { "accessKey", "orderId", "partnerCode", "requestId" };

	/** Thu tu truong khi ky yeu cau hoan tien. */
	public static final String[] REFUND_FIELDS = { "accessKey", "amount", "description", "orderId", "partnerCode", "requestId", "transId" };

	private MomoUtil() {
	}

	public static String hmacSHA256(String key, String data) {
		try {
			Mac mac = Mac.getInstance("HmacSHA256");
			mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
			byte[] bytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
			StringBuilder sb = new StringBuilder(bytes.length * 2);
			for (byte b : bytes) {
				sb.append(String.format("%02x", b & 0xff));
			}
			return sb.toString();
		} catch (Exception e) {
			throw new IllegalStateException("Khong the tao chu ky HMAC-SHA256", e);
		}
	}

	/** Ghep chuoi ky theo dung thu tu truong cho truoc; truong thieu duoc coi la chuoi rong (dung quy uoc MoMo). */
	public static String rawSignature(String[] fields, Map<String, String> values) {
		StringBuilder sb = new StringBuilder();
		for (String f : fields) {
			if (sb.length() > 0) {
				sb.append('&');
			}
			String v = values.get(f);
			sb.append(f).append('=').append(v == null ? "" : v);
		}
		return sb.toString();
	}

	/** So sanh chu ky trong thoi gian khong doi (tranh do chu ky qua thoi gian phan hoi). */
	public static boolean sameSignature(String a, String b) {
		if (a == null || b == null) {
			return false;
		}
		return java.security.MessageDigest.isEqual(a.toLowerCase().getBytes(StandardCharsets.UTF_8),
				b.toLowerCase().getBytes(StandardCharsets.UTF_8));
	}
}
