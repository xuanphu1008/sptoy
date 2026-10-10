package com.toyland.partner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Quy uoc ky yeu cau cua API doi tac (hop dong, ghi trong docs/api-contract):
 * <pre>
 * canonical = timestamp + "\n" + METHOD + "\n" + path[?query] + "\n" + hex(SHA-256(body))
 * X-Signature = hex(HMAC-SHA256(secret, canonical))
 * </pre>
 * timestamp la so giay Unix; may chu chi chap nhan lech toi da 5 phut (chong phat lai - replay).
 */
public final class PartnerSigner {

	private PartnerSigner() {
	}

	public static String canonical(String timestamp, String method, String pathWithQuery, byte[] body) {
		return timestamp + "\n" + method.toUpperCase() + "\n" + pathWithQuery + "\n" + sha256Hex(body == null ? new byte[0] : body);
	}

	public static String sign(String secret, String canonical) {
		try {
			Mac mac = Mac.getInstance("HmacSHA256");
			mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
			return HexFormat.of().formatHex(mac.doFinal(canonical.getBytes(StandardCharsets.UTF_8)));
		} catch (Exception e) {
			throw new IllegalStateException("Khong ky duoc HMAC", e);
		}
	}

	/** So sanh thoi gian khong doi, chong tan cong do thoi gian (timing attack). */
	public static boolean constantTimeEquals(String a, String b) {
		return MessageDigest.isEqual(a.getBytes(StandardCharsets.UTF_8), b.getBytes(StandardCharsets.UTF_8));
	}

	static String sha256Hex(byte[] data) {
		try {
			return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(data));
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}
}
