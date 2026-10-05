package com.toyland.common.util;

import jakarta.servlet.http.HttpServletRequest;

public final class ClientIp {

	private ClientIp() {
	}

	public static String of(HttpServletRequest req) {
		String xff = req.getHeader("X-Forwarded-For");
		if (xff != null && !xff.isBlank()) {
			return xff.split(",")[0].trim();
		}
		String ip = req.getRemoteAddr();
		return "0:0:0:0:0:0:0:1".equals(ip) ? "127.0.0.1" : ip;
	}
}
