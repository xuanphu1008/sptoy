package com.toyland.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import com.toyland.auth.StompAuthInterceptor;

/**
 * WebSocket + STOMP cho thong bao thoi gian thuc.
 * Client ket noi ws://host:8080/ws, gui header "Authorization: Bearer <accessToken>" trong khung CONNECT,
 * roi SUBSCRIBE /user/queue/notifications (moi nguoi 1 hang doi rieng).
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

	private final StompAuthInterceptor stompAuthInterceptor;

	@Value("${app.cors.allowed-origins}")
	private String allowedOrigins;

	public WebSocketConfig(StompAuthInterceptor stompAuthInterceptor) {
		this.stompAuthInterceptor = stompAuthInterceptor;
	}

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/ws").setAllowedOriginPatterns(allowedOrigins.split(","));
	}

	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		registry.enableSimpleBroker("/topic", "/queue");
		registry.setApplicationDestinationPrefixes("/app");
		registry.setUserDestinationPrefix("/user");
	}

	@Override
	public void configureClientInboundChannel(ChannelRegistration registration) {
		registration.interceptors(stompAuthInterceptor);
	}
}
