package com.toyland.auth;

import java.security.Principal;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

/**
 * Xac thuc khung STOMP bang JWT (giong JwtAuthFilter cho HTTP):
 *  - CONNECT: bat buoc header Authorization: Bearer <token> hop le -> gan Principal cho phien WebSocket.
 *  - SUBSCRIBE /topic/staff/**: chi ADMIN/STAFF.
 */
@Component
public class StompAuthInterceptor implements ChannelInterceptor {

	private final JwtService jwtService;
	private final CustomUserDetailsService userDetailsService;

	public StompAuthInterceptor(JwtService jwtService, CustomUserDetailsService userDetailsService) {
		this.jwtService = jwtService;
		this.userDetailsService = userDetailsService;
	}

	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		StompHeaderAccessor acc = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
		if (acc == null || acc.getCommand() == null) {
			return message;
		}
		if (StompCommand.CONNECT.equals(acc.getCommand())) {
			String header = acc.getFirstNativeHeader("Authorization");
			String username = header != null && header.startsWith("Bearer ") ? jwtService.validateAndGetUsername(header.substring(7)) : null;
			if (username == null) {
				throw new MessagingException("Token không hợp lệ hoặc đã hết hạn");
			}
			try {
				UserDetails ud = userDetailsService.loadUserByUsername(username);
				if (!ud.isEnabled()) {
					throw new MessagingException("Tài khoản đã bị khoá");
				}
				acc.setUser(new UsernamePasswordAuthenticationToken(ud, null, ud.getAuthorities()));
			} catch (UsernameNotFoundException e) {
				throw new MessagingException("Tài khoản không tồn tại");
			}
		} else if (StompCommand.SUBSCRIBE.equals(acc.getCommand())) {
			String dest = acc.getDestination();
			if (dest != null && dest.startsWith("/topic/staff") && !isStaff(acc.getUser())) {
				throw new MessagingException("Không có quyền theo dõi kênh này");
			}
		}
		return message;
	}

	private static boolean isStaff(Principal p) {
		if (p instanceof UsernamePasswordAuthenticationToken t) {
			return t.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()) || "ROLE_STAFF".equals(a.getAuthority()));
		}
		return false;
	}
}
