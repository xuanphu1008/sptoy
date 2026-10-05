package com.toyland.auth;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.toyland.audit.AuditService;
import com.toyland.auth.dto.AuthDtos.AuthResponse;
import com.toyland.auth.dto.AuthDtos.LoginRequest;
import com.toyland.auth.dto.AuthDtos.RegisterRequest;
import com.toyland.auth.dto.AuthDtos.UserSummary;
import com.toyland.common.exception.ApiException;
import com.toyland.common.util.TextUtils;
import com.toyland.common.util.TokenHasher;
import com.toyland.notification.MailService;
import com.toyland.user.Role;
import com.toyland.user.User;
import com.toyland.user.UserRepository;

@Service
public class AuthService {

	private static final int MAX_FAILED = 5;
	private static final int LOCK_MINUTES = 15;

	private final UserRepository userRepository;
	private final RefreshTokenRepository refreshTokenRepository;
	private final PasswordResetTokenRepository resetTokenRepository;
	private final PasswordEncoder passwordEncoder;
	private final AuthenticationManager authenticationManager;
	private final JwtService jwtService;
	private final MailService mailService;
	private final AuditService audit;
	private final EmailVerificationService emailVerification;

	@Value("${app.jwt.refresh-expiration-days}")
	private int refreshDays;

	@Value("${app.frontend-url}")
	private String frontendUrl;

	public AuthService(UserRepository userRepository, RefreshTokenRepository refreshTokenRepository,
			PasswordResetTokenRepository resetTokenRepository, PasswordEncoder passwordEncoder,
			AuthenticationManager authenticationManager, JwtService jwtService, MailService mailService, AuditService audit,
			EmailVerificationService emailVerification) {
		this.userRepository = userRepository;
		this.refreshTokenRepository = refreshTokenRepository;
		this.resetTokenRepository = resetTokenRepository;
		this.passwordEncoder = passwordEncoder;
		this.authenticationManager = authenticationManager;
		this.jwtService = jwtService;
		this.mailService = mailService;
		this.audit = audit;
		this.emailVerification = emailVerification;
	}

	public static UserSummary toSummary(User u) {
		return new UserSummary(u.getId(), u.getUsername(), u.getFullName(), u.getEmail(), u.getPhone(), u.getRole(),
				u.isEmailVerified());
	}

	@Transactional
	public AuthResponse register(RegisterRequest req) {
		if (userRepository.existsByUsername(req.username())) {
			throw ApiException.conflict("Tên đăng nhập đã tồn tại");
		}
		if (userRepository.existsByEmailIgnoreCase(req.email())) {
			throw ApiException.conflict("Email đã được sử dụng");
		}
		User u = new User();
		u.setUsername(req.username());
		u.setPassword(passwordEncoder.encode(req.password()));
		u.setFullName(req.fullName().trim());
		u.setEmail(req.email().trim());
		u.setPhone(TextUtils.trimToNull(req.phone()));
		u.setRole(Role.CUSTOMER);
		u.setConsentAt(LocalDateTime.now()); // luu thoi diem dong y xu ly du lieu ca nhan
		u.setEmailVerifiedAt(null); // cho khach bam link trong email
		u = userRepository.save(u);
		emailVerification.sendLink(u);
		// Van dang nhap ngay (giong Laravel Breeze) nhung bi chan dat hang / danh gia cho den khi xac nhan
		return issueTokens(u);
	}

	/**
	 * Dang nhap. KHONG dat @Transactional de viec tang so lan sai duoc luu ngay ca khi nem loi.
	 * Sai 5 lan lien tiep -> khoa 15 phut.
	 */
	public AuthResponse login(LoginRequest req) {
		User u = userRepository.findByUsername(req.username()).orElse(null);
		if (u != null && u.getLockedUntil() != null && u.getLockedUntil().isAfter(LocalDateTime.now())) {
			throw ApiException.forbidden("Tài khoản tạm khoá đến " + u.getLockedUntil().toLocalTime().withNano(0)
					+ " do nhập sai mật khẩu nhiều lần");
		}
		try {
			authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(req.username(), req.password()));
		} catch (BadCredentialsException ex) {
			if (u != null) {
				int failed = u.getFailedLoginAttempts() + 1;
				if (failed >= MAX_FAILED) {
					u.setLockedUntil(LocalDateTime.now().plusMinutes(LOCK_MINUTES));
					u.setFailedLoginAttempts(0);
					userRepository.save(u);
					throw ApiException.forbidden("Sai mật khẩu " + MAX_FAILED + " lần. Tài khoản bị tạm khoá " + LOCK_MINUTES + " phút");
				}
				u.setFailedLoginAttempts(failed);
				userRepository.save(u);
				throw new ApiException(HttpStatus.UNAUTHORIZED, "BAD_CREDENTIALS",
						"Sai tên đăng nhập hoặc mật khẩu (còn " + (MAX_FAILED - failed) + " lần thử)");
			}
			throw ex;
		}
		if (u.getFailedLoginAttempts() > 0 || u.getLockedUntil() != null) {
			u.setFailedLoginAttempts(0);
			u.setLockedUntil(null);
			u = userRepository.save(u);
		}
		if (u.getRole() != com.toyland.user.Role.CUSTOMER) {
			audit.logAs(u.getUsername(), u.getRole().name(), "LOGIN", "USER", u.getUsername(), "Đăng nhập hệ thống");
		}
		return issueTokens(u);
	}

	/** Doi refresh token lay cap token moi (refresh token cu bi thu hoi - token rotation). */
	@Transactional
	public AuthResponse refresh(String token) {
		// CSDL chi luu ban bam SHA-256 cua refresh token -> bam token client gui len roi moi tra cuu
		RefreshToken rt = (token == null ? java.util.Optional.<RefreshToken>empty() : refreshTokenRepository.findByToken(TokenHasher.sha256(token)))
				.orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_REFRESH_TOKEN", "Phiên đăng nhập không hợp lệ"));
		if (rt.isRevoked() || rt.getExpiresAt().isBefore(LocalDateTime.now())) {
			throw new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_REFRESH_TOKEN", "Phiên đăng nhập đã hết hạn, vui lòng đăng nhập lại");
		}
		User u = rt.getUser();
		if (!u.isEnabled() || u.isDeleted()) {
			throw new ApiException(HttpStatus.UNAUTHORIZED, "ACCOUNT_DISABLED", "Tài khoản đã bị khoá");
		}
		rt.setRevoked(true);
		return issueTokens(u);
	}

	@Transactional
	public void logout(String token) {
		if (token != null) {
			refreshTokenRepository.findByToken(TokenHasher.sha256(token)).ifPresent(rt -> rt.setRevoked(true));
		}
	}

	/** Luon tra ve thanh cong de khong lo email nao ton tai. */
	@Transactional
	public void forgotPassword(String email) {
		userRepository.findByEmailIgnoreCase(email).filter(u -> u.isEnabled() && !u.isDeleted()).ifPresent(u -> {
			// Token goc chi nam trong link gui email; CSDL chi luu ban bam SHA-256
			String raw = TokenHasher.newToken();
			PasswordResetToken t = new PasswordResetToken();
			t.setUser(u);
			t.setToken(TokenHasher.sha256(raw));
			t.setExpiresAt(LocalDateTime.now().plusMinutes(15));
			resetTokenRepository.save(t);
			mailService.sendPasswordReset(u.getEmail(), u.getFullName(), frontendUrl + "/reset-password?token=" + raw);
		});
	}

	@Transactional
	public void resetPassword(String token, String newPassword) {
		PasswordResetToken t = (token == null ? java.util.Optional.<PasswordResetToken>empty() : resetTokenRepository.findByToken(TokenHasher.sha256(token)))
				.orElseThrow(() -> ApiException.badRequest("INVALID_TOKEN", "Liên kết đặt lại mật khẩu không hợp lệ"));
		if (t.isUsed() || t.getExpiresAt().isBefore(LocalDateTime.now())) {
			throw ApiException.badRequest("INVALID_TOKEN", "Liên kết đặt lại mật khẩu đã hết hạn hoặc đã được sử dụng");
		}
		User u = t.getUser();
		u.setPassword(passwordEncoder.encode(newPassword));
		u.setFailedLoginAttempts(0);
		u.setLockedUntil(null);
		t.setUsed(true);
		refreshTokenRepository.revokeAllOfUser(u.getId());
	}

	private AuthResponse issueTokens(User u) {
		// Refresh token: 32 byte ngau nhien (SecureRandom); tra token goc cho client, CSDL chi luu ban bam SHA-256
		String raw = TokenHasher.newToken();
		RefreshToken rt = new RefreshToken();
		rt.setUser(u);
		rt.setToken(TokenHasher.sha256(raw));
		rt.setExpiresAt(LocalDateTime.now().plusDays(refreshDays));
		refreshTokenRepository.save(rt);
		return new AuthResponse(jwtService.generateAccessToken(u), raw, "Bearer",
				jwtService.getAccessExpirationSeconds(), toSummary(u));
	}
}
