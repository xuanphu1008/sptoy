package com.toyland.user;

import java.time.LocalDateTime;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.toyland.audit.AuditService;
import com.toyland.auth.CurrentUserService;
import com.toyland.auth.RefreshTokenRepository;
import com.toyland.common.dto.PageResponse;
import com.toyland.common.exception.ApiException;
import com.toyland.common.util.TextUtils;
import com.toyland.order.OrderQueryApi;
import com.toyland.user.dto.UserDtos.AdminUserDto;
import com.toyland.user.dto.UserDtos.AdminUserRequest;

@Service
@Transactional
public class AdminUserService {

	private final UserRepository userRepository;
	private final OrderQueryApi orders;
	private final RefreshTokenRepository refreshTokenRepository;
	private final PasswordEncoder passwordEncoder;
	private final CurrentUserService currentUser;
	private final AuditService audit;

	public AdminUserService(UserRepository userRepository, OrderQueryApi orders,
			RefreshTokenRepository refreshTokenRepository, PasswordEncoder passwordEncoder, CurrentUserService currentUser, AuditService audit) {
		this.userRepository = userRepository;
		this.orders = orders;
		this.refreshTokenRepository = refreshTokenRepository;
		this.passwordEncoder = passwordEncoder;
		this.currentUser = currentUser;
		this.audit = audit;
	}

	private AdminUserDto toDto(User u) {
		boolean locked = u.getLockedUntil() != null && u.getLockedUntil().isAfter(LocalDateTime.now());
		return new AdminUserDto(u.getId(), u.getUsername(), u.getFullName(), u.getEmail(), u.getPhone(), u.getRole(),
				u.isEnabled(), locked, u.getLockedUntil(), orders.countOrdersOfUser(u.getId()), u.getCreatedAt(), u.getPoints(),
				u.getLifetimePoints(), com.toyland.loyalty.LoyaltyRules.tierOf(u.getLifetimePoints()).label(), u.isEmailVerified());
	}

	@Transactional(readOnly = true)
	public PageResponse<AdminUserDto> list(String keyword, Role role, int page, int size) {
		String kw = TextUtils.trimToNull(keyword);
		return PageResponse.of(userRepository.search(kw, role, PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 500), Sort.by(Sort.Direction.DESC, "id"))), this::toDto);
	}

	@Transactional(readOnly = true)
	public AdminUserDto get(Long id) {
		return toDto(find(id));
	}

	public AdminUserDto create(AdminUserRequest r) {
		if (TextUtils.isBlank(r.password()) || r.password().length() < 6) {
			throw ApiException.badRequest("Mật khẩu tối thiểu 6 ký tự");
		}
		if (userRepository.existsByUsername(r.username())) {
			throw ApiException.conflict("Tên đăng nhập đã tồn tại");
		}
		if (userRepository.existsByEmailIgnoreCase(r.email())) {
			throw ApiException.conflict("Email đã được sử dụng");
		}
		User u = new User();
		u.setUsername(r.username());
		u.setPassword(passwordEncoder.encode(r.password()));
		apply(u, r);
		u.setEmailVerifiedAt(LocalDateTime.now()); // tai khoan do quan tri tao: coi nhu da xac nhan
		u = userRepository.save(u);
		audit.log("CREATE", "USER", u.getUsername(), "Tạo tài khoản " + u.getUsername() + " (" + u.getRole() + ")");
		return toDto(u);
	}

	public AdminUserDto update(Long id, AdminUserRequest r) {
		User u = find(id);
		User me = currentUser.require();
		if (u.getId().equals(me.getId()) && (r.role() != u.getRole() || !r.enabled())) {
			throw ApiException.badRequest("Không thể tự đổi vai trò hoặc tự khoá tài khoản của mình");
		}
		if (!u.getUsername().equals(r.username()) && userRepository.existsByUsername(r.username())) {
			throw ApiException.conflict("Tên đăng nhập đã tồn tại");
		}
		if (userRepository.existsByEmailIgnoreCaseAndIdNot(r.email(), id)) {
			throw ApiException.conflict("Email đã được sử dụng");
		}
		u.setUsername(r.username());
		if (!TextUtils.isBlank(r.password())) {
			if (r.password().length() < 6) {
				throw ApiException.badRequest("Mật khẩu tối thiểu 6 ký tự");
			}
			u.setPassword(passwordEncoder.encode(r.password()));
			refreshTokenRepository.revokeAllOfUser(u.getId());
		}
		boolean wasEnabled = u.isEnabled();
		apply(u, r);
		if (wasEnabled && !u.isEnabled()) {
			refreshTokenRepository.revokeAllOfUser(u.getId());
		}
		audit.log("UPDATE", "USER", u.getUsername(), "Cập nhật tài khoản " + u.getUsername() + " (vai trò " + u.getRole()
				+ (u.isEnabled() ? "" : ", đã khoá") + (TextUtils.isBlank(r.password()) ? "" : ", đặt lại mật khẩu") + ")");
		return toDto(u);
	}

	public void unlock(Long id) {
		User u = find(id);
		u.setLockedUntil(null);
		u.setFailedLoginAttempts(0);
		audit.log("UNLOCK", "USER", u.getUsername(), "Mở khoá đăng nhập cho " + u.getUsername());
	}

	public void delete(Long id) {
		User u = find(id);
		if (u.getId().equals(currentUser.requireId())) {
			throw ApiException.badRequest("Không thể xoá tài khoản đang đăng nhập");
		}
		if (orders.countOrdersOfUser(id) > 0) {
			throw ApiException.conflict("Tài khoản đã có đơn hàng, hãy khoá tài khoản thay vì xoá");
		}
		userRepository.delete(u);
		audit.log("DELETE", "USER", u.getUsername(), "Xoá tài khoản " + u.getUsername());
	}

	private User find(Long id) {
		return userRepository.findById(id).filter(u -> !u.isDeleted()).orElseThrow(() -> ApiException.notFound("tài khoản", id));
	}

	private void apply(User u, AdminUserRequest r) {
		u.setFullName(r.fullName().trim());
		if (u.getEmail() != null && !u.getEmail().equalsIgnoreCase(r.email().trim())) {
			u.setEmailVerifiedAt(LocalDateTime.now()); // quan tri sua email ho khach (da xac minh qua kenh khac)
		}
		u.setEmail(r.email().trim());
		u.setPhone(TextUtils.trimToNull(r.phone()));
		u.setRole(r.role());
		u.setEnabled(r.enabled());
	}
}
