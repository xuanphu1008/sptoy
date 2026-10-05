package com.toyland.auth;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.toyland.user.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

/** Tao va kiem tra access token (JWT, HS512). */
@Service
public class JwtService {

	private final SecretKey key;
	private final long accessExpirationMs;

	public JwtService(@Value("${app.jwt.secret}") String secret,
			@Value("${app.jwt.access-expiration-ms}") long accessExpirationMs) {
		this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
		this.accessExpirationMs = accessExpirationMs;
	}

	public String generateAccessToken(User user) {
		Date now = new Date();
		return Jwts.builder()
				.subject(user.getUsername())
				.claim("uid", user.getId())
				.claim("role", user.getRole().name())
				.issuedAt(now)
				.expiration(new Date(now.getTime() + accessExpirationMs))
				.signWith(key)
				.compact();
	}

	public long getAccessExpirationSeconds() {
		return accessExpirationMs / 1000;
	}

	/** Tra ve username neu token hop le, nguoc lai null. */
	public String validateAndGetUsername(String token) {
		try {
			Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
			return claims.getSubject();
		} catch (JwtException | IllegalArgumentException e) {
			return null;
		}
	}
}
