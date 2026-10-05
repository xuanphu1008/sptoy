package com.toyland.auth;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.toyland.common.exception.ApiException;
import com.toyland.user.User;
import com.toyland.user.UserRepository;

/** Lay nguoi dung dang dang nhap. */
@Service
public class CurrentUserService {

	private final UserRepository userRepository;

	public CurrentUserService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	public User require() {
		String username = currentUsername();
		if (username == null) {
			throw new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Bạn cần đăng nhập");
		}
		return userRepository.findByUsername(username)
				.orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Tài khoản không tồn tại"));
	}

	public Long requireId() {
		return require().getId();
	}

	/** null neu chua dang nhap. */
	public String currentUsername() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
			return null;
		}
		return auth.getName();
	}

	/** Ten nguoi thao tac de ghi nhat ky; "system" neu chay nen. */
	public String actorName() {
		String u = currentUsername();
		return u == null ? "system" : u;
	}
}
