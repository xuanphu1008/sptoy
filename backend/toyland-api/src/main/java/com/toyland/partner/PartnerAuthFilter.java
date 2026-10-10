package com.toyland.partner;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Cong xac thuc cho /api/partner/**. Thu tu kiem tra: header day du -> do tuoi timestamp (5 phut)
 * -> tim doi tac theo X-Api-Key -> doi tac con hoat dong -> kiem tra chu ky HMAC -> han muc so yeu cau/phut.
 * Moi loi tra ve JSON cung dang ErrorResponse voi ma loi rieng (PARTNER_*), khong lo ly do cu the khi sai khoa/chu ky.
 */
@Component
public class PartnerAuthFilter extends OncePerRequestFilter {

	public static final String ATTR_PARTNER = "toyland.partner";
	static final long MAX_SKEW_SECONDS = 300;
	private static final Logger log = LoggerFactory.getLogger(PartnerAuthFilter.class);

	private final PartnerRepository partners;
	private final Map<String, Deque<Long>> hits = new ConcurrentHashMap<>();

	public PartnerAuthFilter(PartnerRepository partners) {
		this.partners = partners;
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		return !request.getRequestURI().startsWith("/api/partner/");
	}

	@Override
	protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
			throws ServletException, IOException {
		String apiKey = req.getHeader("X-Api-Key");
		String ts = req.getHeader("X-Timestamp");
		String sig = req.getHeader("X-Signature");
		if (apiKey == null || ts == null || sig == null) {
			deny(req, res, 401, "PARTNER_AUTH_MISSING", "Thiếu một trong các header X-Api-Key, X-Timestamp, X-Signature");
			return;
		}
		long tsSec;
		try {
			tsSec = Long.parseLong(ts.trim());
		} catch (NumberFormatException e) {
			deny(req, res, 401, "PARTNER_TIMESTAMP_INVALID", "X-Timestamp phải là số giây Unix");
			return;
		}
		if (Math.abs(Instant.now().getEpochSecond() - tsSec) > MAX_SKEW_SECONDS) {
			deny(req, res, 401, "PARTNER_TIMESTAMP_EXPIRED", "X-Timestamp lệch quá 5 phút so với máy chủ");
			return;
		}
		Partner partner = partners.findByApiKey(apiKey.trim()).orElse(null);
		if (partner == null || !partner.isActive()) {
			deny(req, res, 401, "PARTNER_AUTH_FAILED", "Khóa API hoặc chữ ký không hợp lệ");
			return;
		}
		byte[] body = req.getInputStream().readAllBytes();
		String uri = req.getRequestURI() + (req.getQueryString() != null ? "?" + req.getQueryString() : "");
		String expected = PartnerSigner.sign(partner.getSecret(), PartnerSigner.canonical(ts.trim(), req.getMethod(), uri, body));
		if (!PartnerSigner.constantTimeEquals(expected, sig.trim().toLowerCase())) {
			log.warn("Partner {} gui chu ky sai ({} {})", partner.getId(), req.getMethod(), req.getRequestURI());
			deny(req, res, 401, "PARTNER_AUTH_FAILED", "Khóa API hoặc chữ ký không hợp lệ");
			return;
		}
		if (!allow(partner)) {
			res.setHeader("Retry-After", "60");
			deny(req, res, 429, "PARTNER_RATE_LIMITED", "Vượt hạn mức " + partner.getRateLimitPerMin() + " yêu cầu/phút");
			return;
		}
		partner.setLastUsedAt(LocalDateTime.now());
		partners.save(partner);
		req.setAttribute(ATTR_PARTNER, partner);
		chain.doFilter(new CachedBodyRequest(req, body), res);
	}

	/** Cua so truot 60 giay theo tung doi tac. */
	private boolean allow(Partner p) {
		long now = System.currentTimeMillis();
		Deque<Long> q = hits.computeIfAbsent(p.getApiKey(), k -> new ArrayDeque<>());
		synchronized (q) {
			while (!q.isEmpty() && now - q.peekFirst() > 60_000) {
				q.pollFirst();
			}
			if (q.size() >= p.getRateLimitPerMin()) {
				return false;
			}
			q.addLast(now);
			return true;
		}
	}

	private static void deny(HttpServletRequest req, HttpServletResponse res, int status, String code, String msg) throws IOException {
		res.setStatus(status);
		res.setContentType(MediaType.APPLICATION_JSON_VALUE);
		res.setCharacterEncoding("UTF-8");
		res.getWriter().write(String.format("{\"status\":%d,\"code\":\"%s\",\"message\":\"%s\",\"path\":\"%s\"}", status, code,
				msg.replace("\"", "'"), req.getRequestURI()));
	}

	/** Cho phep controller doc lai than yeu cau sau khi bo loc da doc de ky. */
	private static final class CachedBodyRequest extends HttpServletRequestWrapper {
		private final byte[] body;

		CachedBodyRequest(HttpServletRequest request, byte[] body) {
			super(request);
			this.body = body;
		}

		@Override
		public ServletInputStream getInputStream() {
			ByteArrayInputStream in = new ByteArrayInputStream(body);
			return new ServletInputStream() {
				@Override public boolean isFinished() { return in.available() == 0; }
				@Override public boolean isReady() { return true; }
				@Override public void setReadListener(ReadListener l) { }
				@Override public int read() { return in.read(); }
			};
		}
	}
}
