package com.toyland.auth.dto;

import com.toyland.common.util.ValidationPatterns;
import com.toyland.user.Role;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Cac DTO cua module xac thuc. */
public final class AuthDtos {

	private AuthDtos() {
	}

	public record LoginRequest(
			@NotBlank(message = "Vui lòng nhập tên đăng nhập") String username,
			@NotBlank(message = "Vui lòng nhập mật khẩu") String password) {
	}

	public record RegisterRequest(
			@NotBlank(message = "Vui lòng nhập tên đăng nhập")
			@Size(min = 4, max = 50, message = "Tên đăng nhập từ 4 đến 50 ký tự")
			@Pattern(regexp = "^[a-zA-Z0-9._]+$", message = "Tên đăng nhập chỉ gồm chữ không dấu, số, dấu chấm, gạch dưới")
			String username,
			@NotBlank(message = "Vui lòng nhập mật khẩu") @Size(min = 6, max = 100, message = "Mật khẩu tối thiểu 6 ký tự")
			String password,
			@NotBlank(message = "Vui lòng nhập họ tên") @Size(max = 100) String fullName,
			@NotBlank(message = "Vui lòng nhập email") @Email(regexp = ValidationPatterns.EMAIL, message = "Email không hợp lệ, ví dụ đúng: ten.ho2004@gmail.com") String email,
			@Pattern(regexp = "^$|^0\\d{9,10}$", message = "Số điện thoại không hợp lệ") String phone,
			@AssertTrue(message = "Bạn cần đồng ý với điều khoản và chính sách bảo vệ dữ liệu cá nhân") boolean agreeTerms) {
	}

	public record RefreshRequest(@NotBlank String refreshToken) {
	}

	public record ForgotPasswordRequest(@NotBlank(message = "Vui lòng nhập email") @Email(regexp = ValidationPatterns.EMAIL, message = "Email không hợp lệ, ví dụ đúng: ten.ho2004@gmail.com") String email) {
	}

	public record ResetPasswordRequest(
			@NotBlank String token,
			@NotBlank @Size(min = 6, max = 100, message = "Mật khẩu tối thiểu 6 ký tự") String newPassword) {
	}

	public record UserSummary(Long id, String username, String fullName, String email, String phone, Role role,
			boolean emailVerified) {
	}

	public record VerifyEmailRequest(@NotBlank(message = "Thiếu mã xác nhận") @Size(max = 100) String token) {
	}

	public record VerifyEmailResponse(boolean alreadyVerified, String email, String message) {
	}

	public record AuthResponse(String accessToken, String refreshToken, String tokenType, long expiresIn, UserSummary user) {
	}
}
