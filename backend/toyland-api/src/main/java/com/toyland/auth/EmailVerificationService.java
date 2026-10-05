package com.toyland.auth;

import java.time.Duration;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.toyland.common.exception.ApiException;
import com.toyland.common.util.AfterCommit;
import com.toyland.common.util.TokenHasher;
import com.toyland.notification.MailService;
import com.toyland.user.Role;
import com.toyland.user.User;

/**
 * Xac nhan email bang link (tuong tu MustVerifyEmail cua Laravel).
 * <ol>
 * <li>Dang ky / doi email -> sinh token ngau nhien, luu SHA-256 + email + han dung, gui link
 * {@code {frontend}/verify-email?token=...} qua email.</li>
 * <li>Khach bam link -> frontend goi {@code POST /auth/verify-email} -> dat users.email_verified_at.</li>
 * <li>Chua xac nhan: van dang nhap, xem hang, them gio duoc nhung KHONG dat hang / danh gia
 * (giong middleware "verified" cua Laravel).</li>
 * </ol>
 */
@Service
@Transactional
public class EmailVerificationService {

	/** Gui lai toi da 1 lan / 60 giay (Laravel: throttle:6,1). */
	static final int RESEND_COOLDOWN_SECONDS = 60;

	private final EmailVerificationTokenRepository tokenRepository;
	private final MailService mailService;
	private final CurrentUserService currentUser;

	@Value("${app.auth.email-verify-hours:24}")
	private int validHours;

	@Value("${app.frontend-url}")
	private String frontendUrl;

	public EmailVerificationService(EmailVerificationTokenRepository tokenRepository, MailService mailService,
			CurrentUserService currentUser) {
		this.tokenRepository = tokenRepository;
		this.mailService = mailService;
		this.currentUser = currentUser;
	}

	public record VerifyResult(boolean alreadyVerified, String email) {
	}

	/** Chan thao tac khi khach hang chua xac nhan email (nhan vien / quan tri khong bi chan). */
	public static void requireVerified(User u, String action) {
		if (u.getRole() == Role.CUSTOMER && !u.isEmailVerified()) {
			throw new ApiException(HttpStatus.FORBIDDEN, "EMAIL_NOT_VERIFIED",
					"Vui lòng xác nhận email " + u.getEmail() + " trước khi " + action
							+ ". Kiểm tra hộp thư hoặc bấm \"Gửi lại email xác nhận\".");
		}
	}

	/** Tao link moi (huy link cu) va gui email SAU KHI giao dich commit. */
	public void sendLink(User u) {
		tokenRepository.deletePendingOfUser(u.getId());
		String raw = TokenHasher.newToken();
		EmailVerificationToken t = new EmailVerificationToken();
		t.setUser(u);
		t.setEmail(u.getEmail());
		t.setTokenHash(TokenHasher.sha256(raw));
		t.setExpiresAt(LocalDateTime.now().plusHours(validHours));
		tokenRepository.save(t);
		String link = frontendUrl + "/verify-email?token=" + raw;
		String email = u.getEmail();
		String name = u.getFullName();
		int hours = validHours;
		AfterCommit.run(() -> mailService.sendEmailVerification(email, name, link, hours));
	}

	/** Khach dang dang nhap bam "Gui lai email xac nhan". */
	public String resendForCurrentUser() {
		User u = currentUser.require();
		resend(u);
		return u.getEmail();
	}

	void resend(User u) {
		if (u.isEmailVerified()) {
			throw ApiException.badRequest("ALREADY_VERIFIED", "Email của bạn đã được xác nhận");
		}
		tokenRepository.findTopByUserIdOrderByIdDesc(u.getId()).ifPresent(last -> {
			long waited = Duration.between(last.getCreatedAt(), LocalDateTime.now()).getSeconds();
			if (waited < RESEND_COOLDOWN_SECONDS) {
				throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, "TOO_MANY_REQUESTS",
						"Vui lòng đợi " + (RESEND_COOLDOWN_SECONDS - waited) + " giây rồi gửi lại");
			}
		});
		sendLink(u);
	}

	/** Xu ly link: token dung 1 lan, con han va email chua bi doi. */
	public VerifyResult verify(String rawToken) {
		EmailVerificationToken t = tokenRepository.findByHashWithUser(TokenHasher.sha256(rawToken.trim()))
				.orElseThrow(() -> ApiException.badRequest("INVALID_TOKEN",
						"Liên kết xác nhận không hợp lệ hoặc đã được thay bằng liên kết mới hơn"));
		User u = t.getUser();
		if (t.getUsedAt() != null) {
			if (u.isEmailVerified() && u.getEmail().equalsIgnoreCase(t.getEmail())) {
				return new VerifyResult(true, u.getEmail()); // bam lai link cu: bao da xac nhan (giong Laravel)
			}
			throw ApiException.badRequest("INVALID_TOKEN", "Liên kết xác nhận đã được sử dụng");
		}
		if (!u.getEmail().equalsIgnoreCase(t.getEmail())) {
			throw ApiException.badRequest("INVALID_TOKEN", "Liên kết không còn hiệu lực vì email tài khoản đã thay đổi");
		}
		if (t.getExpiresAt().isBefore(LocalDateTime.now())) {
			throw ApiException.badRequest("TOKEN_EXPIRED", "Liên kết xác nhận đã hết hạn, vui lòng gửi lại email xác nhận");
		}
		t.setUsedAt(LocalDateTime.now());
		boolean already = u.isEmailVerified();
		if (!already) {
			u.setEmailVerifiedAt(LocalDateTime.now());
		}
		return new VerifyResult(already, u.getEmail());
	}
}
