package com.toyland.auth;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.toyland.auth.dto.AuthDtos.AuthResponse;
import com.toyland.auth.dto.AuthDtos.ForgotPasswordRequest;
import com.toyland.auth.dto.AuthDtos.LoginRequest;
import com.toyland.auth.dto.AuthDtos.RefreshRequest;
import com.toyland.auth.dto.AuthDtos.RegisterRequest;
import com.toyland.auth.dto.AuthDtos.ResetPasswordRequest;
import com.toyland.auth.dto.AuthDtos.UserSummary;
import com.toyland.auth.dto.AuthDtos.VerifyEmailRequest;
import com.toyland.auth.dto.AuthDtos.VerifyEmailResponse;
import com.toyland.common.dto.MessageResponse;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "01. Xác thực")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

	private final AuthService authService;
	private final CurrentUserService currentUser;
	private final EmailVerificationService emailVerification;

	public AuthController(AuthService authService, CurrentUserService currentUser, EmailVerificationService emailVerification) {
		this.authService = authService;
		this.currentUser = currentUser;
		this.emailVerification = emailVerification;
	}

	@PostMapping("/register")
	@ResponseStatus(HttpStatus.CREATED)
	public AuthResponse register(@Valid @RequestBody RegisterRequest req) {
		return authService.register(req);
	}

	@PostMapping("/login")
	public AuthResponse login(@Valid @RequestBody LoginRequest req) {
		return authService.login(req);
	}

	@PostMapping("/refresh")
	public AuthResponse refresh(@Valid @RequestBody RefreshRequest req) {
		return authService.refresh(req.refreshToken());
	}

	@PostMapping("/logout")
	public MessageResponse logout(@RequestBody(required = false) RefreshRequest req) {
		authService.logout(req == null ? null : req.refreshToken());
		return new MessageResponse("Đã đăng xuất");
	}

	@PostMapping("/forgot-password")
	public MessageResponse forgot(@Valid @RequestBody ForgotPasswordRequest req) {
		authService.forgotPassword(req.email());
		return new MessageResponse("Nếu email tồn tại trong hệ thống, chúng tôi đã gửi hướng dẫn đặt lại mật khẩu");
	}

	@PostMapping("/reset-password")
	public MessageResponse reset(@Valid @RequestBody ResetPasswordRequest req) {
		authService.resetPassword(req.token(), req.newPassword());
		return new MessageResponse("Đặt lại mật khẩu thành công, vui lòng đăng nhập");
	}

	/** Khach bam link trong email: /verify-email?token=... (cong khai, khong can dang nhap) */
	@PostMapping("/verify-email")
	public VerifyEmailResponse verifyEmail(@Valid @RequestBody VerifyEmailRequest req) {
		EmailVerificationService.VerifyResult r = emailVerification.verify(req.token());
		return new VerifyEmailResponse(r.alreadyVerified(), r.email(), r.alreadyVerified()
				? "Email này đã được xác nhận trước đó" : "Xác nhận email thành công! Bạn đã có thể đặt hàng.");
	}

	/** Gui lai link xac nhan (can dang nhap, toi da 1 lan / 60 giay) */
	@PostMapping("/resend-verification")
	public MessageResponse resendVerification() {
		String email = emailVerification.resendForCurrentUser();
		return new MessageResponse("Đã gửi lại liên kết xác nhận tới " + email);
	}

	@GetMapping("/me")
	public UserSummary me() {
		return AuthService.toSummary(currentUser.require());
	}
}
