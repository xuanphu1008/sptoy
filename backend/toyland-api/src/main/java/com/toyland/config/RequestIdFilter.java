package com.toyland.config;

import java.io.IOException;
import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Gan ma truy vet cho moi yeu cau: lay tu tieu de X-Request-Id neu client gui (chi nhan ky tu an toan,
 * toi da 64), neu khong thi tu sinh. Ma nay vao MDC de moi dong log cua yeu cau mang cung mot "requestId"
 * va duoc tra lai trong tieu de phan hoi, giup doi chieu log giua doi tac va ToyLand.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestIdFilter extends OncePerRequestFilter {

	public static final String HEADER = "X-Request-Id";
	public static final String MDC_KEY = "requestId";

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		String id = request.getHeader(HEADER);
		if (id == null || !id.matches("[A-Za-z0-9._-]{1,64}")) {
			id = UUID.randomUUID().toString();
		}
		MDC.put(MDC_KEY, id);
		response.setHeader(HEADER, id);
		try {
			chain.doFilter(request, response);
		} finally {
			MDC.remove(MDC_KEY);
		}
	}
}
