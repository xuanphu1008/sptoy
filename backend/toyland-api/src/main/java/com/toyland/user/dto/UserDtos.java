package com.toyland.user.dto;

import com.toyland.common.util.ValidationPatterns;
import java.time.LocalDateTime;

import com.toyland.user.Role;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class UserDtos {

	private UserDtos() {
	}

	public record ProfileDto(Long id, String username, String fullName, String email, String phone, Role role,
			LocalDateTime createdAt, LocalDateTime consentAt, boolean emailVerified) {
	}

	public record ProfileRequest(@NotBlank(message = "Vui lòng nhập họ tên") @Size(max = 100) String fullName,
			@NotBlank(message = "Vui lòng nhập email") @Email(regexp = ValidationPatterns.EMAIL, message = "Email không hợp lệ, ví dụ đúng: ten.ho2004@gmail.com") String email,
			@Pattern(regexp = "^$|^0\\d{9,10}$", message = "Số điện thoại không hợp lệ") String phone) {
	}

	public record ChangePasswordRequest(@NotBlank(message = "Nhập mật khẩu hiện tại") String currentPassword,
			@NotBlank @Size(min = 6, max = 100, message = "Mật khẩu mới tối thiểu 6 ký tự") String newPassword) {
	}

	public record DeleteAccountRequest(@NotBlank(message = "Nhập mật khẩu để xác nhận") String password) {
	}

	public record AddressDto(Long id, String receiverName, String phone, Integer provinceId, String provinceName,
			Integer districtId, String districtName, String wardCode, String wardName, String detail, boolean isDefault,
			String fullAddress) {
	}

	public record AddressRequest(@NotBlank(message = "Nhập tên người nhận") @Size(max = 100) String receiverName,
			@NotBlank(message = "Nhập số điện thoại") @Pattern(regexp = "^0\\d{9,10}$", message = "Số điện thoại không hợp lệ") String phone,
			Integer provinceId, @NotBlank(message = "Chọn Tỉnh/Thành phố") String provinceName,
			Integer districtId, @NotBlank(message = "Chọn Quận/Huyện") String districtName,
			@Size(max = 20) String wardCode, @NotBlank(message = "Chọn Phường/Xã") String wardName,
			@NotBlank(message = "Nhập số nhà, tên đường") @Size(max = 255) String detail, boolean isDefault) {
	}

	public record AdminUserDto(Long id, String username, String fullName, String email, String phone, Role role,
			boolean enabled, boolean locked, LocalDateTime lockedUntil, long orderCount, LocalDateTime createdAt,
			int points, int lifetimePoints, String tier, boolean emailVerified) {
	}

	public record AdminUserRequest(
			@NotBlank @Size(min = 4, max = 50) @Pattern(regexp = "^[a-zA-Z0-9._]+$", message = "Tên đăng nhập không hợp lệ") String username,
			@Size(max = 100) String password,
			@NotBlank @Size(max = 100) String fullName,
			@NotBlank @Email(regexp = ValidationPatterns.EMAIL, message = "Email không hợp lệ, ví dụ đúng: ten.ho2004@gmail.com") String email,
			@Pattern(regexp = "^$|^0\\d{9,10}$", message = "Số điện thoại không hợp lệ") String phone,
			@NotNull Role role, boolean enabled) {
	}
}
