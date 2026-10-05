package com.toyland.common.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Sinh token ngau nhien cho link gui qua email va bam SHA-256 de luu CSDL.
 * CSDL chi giu ban bam -> neu lo CSDL, ke gian cung khong dung lai duoc link (giong cach Laravel luu token reset).
 */
public final class TokenHasher {

	private static final SecureRandom RANDOM = new SecureRandom();

	private TokenHasher() {
	}

	/** 32 byte ngau nhien, ma hoa Base64 URL-safe (43 ky tu, dung duoc truc tiep tren URL). */
	public static String newToken() {
		byte[] b = new byte[32];
		RANDOM.nextBytes(b);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(b);
	}

	/** SHA-256 dang hex chu thuong (64 ky tu). */
	public static String sha256(String raw) {
		try {
			byte[] d = MessageDigest.getInstance("SHA-256").digest(raw.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(d);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
		}
	}
}
