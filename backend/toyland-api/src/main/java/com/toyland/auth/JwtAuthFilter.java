package com.toyland.auth;

import java.io.IOException;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/** Doc "Authorization: Bearer <token>" va dat nguoi dung vao SecurityContext. */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

	private final JwtService jwtService;
	private final CustomUserDetailsService userDetailsService;

	public JwtAuthFilter(JwtService jwtService, CustomUserDetailsService userDetailsService) {
		this.jwtService = jwtService;
		this.userDetailsService = userDetailsService;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		String header = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (header != null && header.startsWith("Bearer ") && SecurityContextHolder.getContext().getAuthentication() == null) {
			String username = jwtService.validateAndGetUsername(header.substring(7));
			if (username != null) {
				try {
					UserDetails ud = userDetailsService.loadUserByUsername(username);
					// Chi kiem tra enabled: viec khoa 15 phut do nhap sai mat khau chi chan DANG NHAP moi,
					// khong da phien dang dung cua chu tai khoan (tranh bi nguoi khac co tinh nhap sai de "kick")
					if (ud.isEnabled()) {
						UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(ud, null, ud.getAuthorities());
						auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
						SecurityContextHolder.getContext().setAuthentication(auth);
					}
				} catch (UsernameNotFoundException ignored) {
					// tai khoan da bi xoa -> coi nhu chua dang nhap
				}
			}
		}
		chain.doFilter(request, response);
	}
}
