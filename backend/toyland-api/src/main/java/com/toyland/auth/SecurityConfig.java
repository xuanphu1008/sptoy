package com.toyland.auth;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.servlet.http.HttpServletResponse;

/**
 * Bao mat JWT stateless + phan quyen 3 vai tro:
 * ADMIN (toan quyen), STAFF (don hang, kho, san pham, danh gia), CUSTOMER.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

	private final JwtAuthFilter jwtAuthFilter;
	private final RateLimitFilter rateLimitFilter;
	private final com.toyland.partner.PartnerAuthFilter partnerAuthFilter;

	@Value("${app.cors.allowed-origins}")
	private String allowedOrigins;

	public SecurityConfig(JwtAuthFilter jwtAuthFilter, RateLimitFilter rateLimitFilter,
			com.toyland.partner.PartnerAuthFilter partnerAuthFilter) {
		this.partnerAuthFilter = partnerAuthFilter;
		this.jwtAuthFilter = jwtAuthFilter;
		this.rateLimitFilter = rateLimitFilter;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.csrf(csrf -> csrf.disable())
			.cors(Customizer.withDefaults())
			.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(auth -> auth
				.requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/error", "/uploads/**").permitAll()
				// WebSocket handshake: xac thuc bang JWT o khung STOMP CONNECT (StompAuthInterceptor)
				.requestMatchers("/ws", "/ws/**").permitAll()
				.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
				// API doi tac: tu xac thuc bang khoa API + chu ky HMAC (PartnerAuthFilter), khong dung JWT
				.requestMatchers("/api/partner/**").permitAll()
				// Xac thuc
				.requestMatchers(HttpMethod.POST, "/api/v1/auth/register", "/api/v1/auth/login", "/api/v1/auth/refresh",
						"/api/v1/auth/logout", "/api/v1/auth/forgot-password", "/api/v1/auth/reset-password",
						"/api/v1/auth/verify-email").permitAll()
				// Du lieu cong khai cua cua hang
				.requestMatchers(HttpMethod.GET, "/api/v1/home", "/api/v1/products/**", "/api/v1/categories/**",
						"/api/v1/brands/**", "/api/v1/franchises/**", "/api/v1/banners", "/api/v1/vouchers/available",
						"/api/v1/shipping/**", "/api/v1/orders/track", "/api/v1/payments/momo/return").permitAll()
				// MoMo goi IPN server-to-server (xac thuc bang chu ky HMAC, khong co JWT)
				.requestMatchers(HttpMethod.POST, "/api/v1/payments/momo/ipn").permitAll()
				.requestMatchers(HttpMethod.POST, "/api/v1/cart/preview").permitAll()
				// STAFF can xem cay danh muc (ke ca danh muc an) de sua san pham
				.requestMatchers(HttpMethod.GET, "/api/v1/admin/categories").hasAnyRole("ADMIN", "STAFF")
				// Chi ADMIN
				.requestMatchers("/api/v1/admin/users/**", "/api/v1/admin/vouchers/**", "/api/v1/admin/campaigns/**",
						"/api/v1/admin/banners/**", "/api/v1/admin/categories/**", "/api/v1/admin/brands/**",
						"/api/v1/admin/franchises/**", "/api/v1/admin/reports/**", "/api/v1/admin/audit-logs/**",
						"/api/v1/admin/banned-keywords/**", "/api/v1/admin/loyalty/**", "/api/v1/admin/partners/**").hasRole("ADMIN")
				// ADMIN + STAFF
				.requestMatchers("/api/v1/admin/**").hasAnyRole("ADMIN", "STAFF")
				.anyRequest().authenticated())
			.exceptionHandling(ex -> ex
				.authenticationEntryPoint((req, res, e) -> writeError(res, 401, "UNAUTHORIZED",
						"Bạn cần đăng nhập để thực hiện chức năng này", req.getRequestURI()))
				.accessDeniedHandler((req, res, e) -> writeError(res, 403, "FORBIDDEN",
						"Bạn không có quyền truy cập chức năng này", req.getRequestURI())))
			// Gioi han tan suat theo IP chay truoc JWT (sau CorsFilter nen phan hoi 429 van co header CORS)
			.addFilterBefore(partnerAuthFilter, UsernamePasswordAuthenticationFilter.class)
			.addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
			.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}

	private static void writeError(HttpServletResponse res, int status, String code, String message, String path)
			throws java.io.IOException {
		res.setStatus(status);
		res.setContentType(MediaType.APPLICATION_JSON_VALUE);
		res.setCharacterEncoding("UTF-8");
		res.getWriter().write(String.format("{\"status\":%d,\"code\":\"%s\",\"message\":\"%s\",\"path\":\"%s\"}",
				status, code, message, path));
	}

	/** Khong dang ky JwtAuthFilter lan 2 o cap Servlet. */
	@Bean
	public FilterRegistrationBean<JwtAuthFilter> jwtFilterRegistration(JwtAuthFilter filter) {
		FilterRegistrationBean<JwtAuthFilter> reg = new FilterRegistrationBean<>(filter);
		reg.setEnabled(false);
		return reg;
	}

	/** Tuong tu: RateLimitFilter chi chay trong chuoi bao mat, khong chay them o cap Servlet. */
	@Bean
	public FilterRegistrationBean<RateLimitFilter> rateLimitFilterRegistration(RateLimitFilter filter) {
		FilterRegistrationBean<RateLimitFilter> reg = new FilterRegistrationBean<>(filter);
		reg.setEnabled(false);
		return reg;
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
		config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		config.setAllowedHeaders(List.of("*"));
		config.setAllowCredentials(true);
		// cho phep trinh duyet doc ten file khi tai bao cao Excel
		config.setExposedHeaders(List.of(HttpHeaders.CONTENT_DISPOSITION));
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);
		return source;
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}

	/** PartnerAuthFilter chi chay trong chuoi bao mat. */
	@Bean
	public FilterRegistrationBean<com.toyland.partner.PartnerAuthFilter> partnerFilterRegistration(com.toyland.partner.PartnerAuthFilter filter) {
		FilterRegistrationBean<com.toyland.partner.PartnerAuthFilter> reg = new FilterRegistrationBean<>(filter);
		reg.setEnabled(false);
		return reg;
	}
}
