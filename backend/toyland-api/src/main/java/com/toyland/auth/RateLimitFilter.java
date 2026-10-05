package com.toyland.auth;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Gioi han tan suat (rate limit) theo dia chi IP cho cac API cong khai de bi lam dung:
 * dang nhap, dang ky, quen/dat lai mat khau, xac nhan email, lam moi token, tra cuu don hang.
 * <p>
 * Thuat toan cua so truot (sliding window) luu trong bo nho: moi cap (quy tac, IP) giu danh sach
 * thoi diem goi trong cua so; vuot nguong -> tra 429 kem header Retry-After.
 * Bo sung cho co che khoa tai khoan 15 phut sau 5 lan sai mat khau (khoa theo tai khoan,
 * con bo loc nay chan theo IP - vd. thu mat khau tren nhieu tai khoan khac nhau).
 * <p>
 * Dung IP ket noi thuc (getRemoteAddr), KHONG tin header X-Forwarded-For vi ke tan cong tu dat duoc.
 * Tat ca nguong co gia tri mac dinh, co the ghi de bang bien moi truong (vd. APP_RATE_LIMIT_LOGIN_MAX).
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

	private record Rule(String name, String method, String path, int max, long windowMillis) {
	}

	private final boolean enabled;
	private final List<Rule> rules;
	private final Map<String, Deque<Long>> hits = new ConcurrentHashMap<>();
	private volatile long lastCleanup = System.currentTimeMillis();

	public RateLimitFilter(
			@Value("${app.rate-limit.enabled:true}") boolean enabled,
			@Value("${app.rate-limit.login.max:20}") int loginMax,
			@Value("${app.rate-limit.login.window-seconds:60}") long loginWin,
			@Value("${app.rate-limit.register.max:5}") int registerMax,
			@Value("${app.rate-limit.register.window-seconds:600}") long registerWin,
			@Value("${app.rate-limit.forgot-password.max:5}") int forgotMax,
			@Value("${app.rate-limit.forgot-password.window-seconds:900}") long forgotWin,
			@Value("${app.rate-limit.reset-password.max:10}") int resetMax,
			@Value("${app.rate-limit.reset-password.window-seconds:900}") long resetWin,
			@Value("${app.rate-limit.verify-email.max:20}") int verifyMax,
			@Value("${app.rate-limit.verify-email.window-seconds:600}") long verifyWin,
			@Value("${app.rate-limit.refresh.max:30}") int refreshMax,
			@Value("${app.rate-limit.refresh.window-seconds:60}") long refreshWin,
			@Value("${app.rate-limit.order-track.max:20}") int trackMax,
			@Value("${app.rate-limit.order-track.window-seconds:60}") long trackWin) {
		this.enabled = enabled;
		this.rules = List.of(
				new Rule("login", "POST", "/api/v1/auth/login", loginMax, loginWin * 1000),
				new Rule("register", "POST", "/api/v1/auth/register", registerMax, registerWin * 1000),
				new Rule("forgot", "POST", "/api/v1/auth/forgot-password", forgotMax, forgotWin * 1000),
				new Rule("reset", "POST", "/api/v1/auth/reset-password", resetMax, resetWin * 1000),
				new Rule("verify", "POST", "/api/v1/auth/verify-email", verifyMax, verifyWin * 1000),
				new Rule("refresh", "POST", "/api/v1/auth/refresh", refreshMax, refreshWin * 1000),
				new Rule("track", "GET", "/api/v1/orders/track", trackMax, trackWin * 1000));
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		return !enabled || findRule(request) == null;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		Rule rule = findRule(request);
		long now = System.currentTimeMillis();
		cleanupIfNeeded(now);

		String key = rule.name() + "|" + clientIp(request);
		Deque<Long> q = hits.computeIfAbsent(key, k -> new ArrayDeque<>());
		long retryAfterMillis = 0;
		synchronized (q) {
			while (!q.isEmpty() && now - q.peekFirst() >= rule.windowMillis()) {
				q.pollFirst();
			}
			if (q.size() >= rule.max()) {
				retryAfterMillis = q.isEmpty() ? rule.windowMillis() : rule.windowMillis() - (now - q.peekFirst());
			} else {
				q.addLast(now);
			}
		}

		if (retryAfterMillis > 0) {
			long seconds = Math.max(1, (retryAfterMillis + 999) / 1000);
			response.setStatus(429);
			response.setHeader("Retry-After", String.valueOf(seconds));
			response.setContentType(MediaType.APPLICATION_JSON_VALUE);
			response.setCharacterEncoding("UTF-8");
			response.getWriter().write(String.format(
					"{\"status\":429,\"code\":\"TOO_MANY_REQUESTS\",\"message\":\"%s\",\"path\":\"%s\"}",
					"Bạn thao tác quá nhanh, vui lòng thử lại sau " + humanize(seconds),
					request.getRequestURI()));
			return;
		}
		chain.doFilter(request, response);
	}

	private Rule findRule(HttpServletRequest request) {
		String path = request.getRequestURI().substring(request.getContextPath().length());
		if (path.length() > 1 && path.endsWith("/")) {
			path = path.substring(0, path.length() - 1);
		}
		String method = request.getMethod();
		for (Rule r : rules) {
			if (r.method().equalsIgnoreCase(method) && r.path().equals(path)) {
				return r;
			}
		}
		return null;
	}

	private static String clientIp(HttpServletRequest request) {
		String ip = request.getRemoteAddr();
		return "0:0:0:0:0:0:0:1".equals(ip) ? "127.0.0.1" : ip;
	}

	private static String humanize(long seconds) {
		return seconds < 60 ? seconds + " giây" : ((seconds + 59) / 60) + " phút";
	}

	/** Don cac khoa da het han moi 5 phut de bo nho khong phinh ra. */
	private void cleanupIfNeeded(long now) {
		if (now - lastCleanup < 300_000) {
			return;
		}
		lastCleanup = now;
		long maxWindow = rules.stream().mapToLong(Rule::windowMillis).max().orElse(0);
		hits.entrySet().removeIf(e -> {
			Deque<Long> q = e.getValue();
			synchronized (q) {
				Long last = q.peekLast();
				return last == null || now - last >= maxWindow;
			}
		});
	}
}
