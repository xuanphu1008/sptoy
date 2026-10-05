package com.toyland.auth;

import java.time.LocalDateTime;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.toyland.user.User;
import com.toyland.user.UserRepository;

/** Nap nguoi dung tu CSDL cho Spring Security. Role X -> authority ROLE_X. */
@Service
public class CustomUserDetailsService implements UserDetailsService {

	private final UserRepository userRepository;

	public CustomUserDetailsService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User u = userRepository.findByUsername(username)
				.orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy tài khoản " + username));
		boolean locked = u.getLockedUntil() != null && u.getLockedUntil().isAfter(LocalDateTime.now());
		return org.springframework.security.core.userdetails.User.withUsername(u.getUsername())
				.password(u.getPassword())
				.roles(u.getRole().name())
				.disabled(!u.isEnabled() || u.isDeleted())
				.accountLocked(locked)
				.build();
	}
}
