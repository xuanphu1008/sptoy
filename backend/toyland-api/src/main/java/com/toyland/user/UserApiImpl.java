package com.toyland.user;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

/** Hien thuc noi bo cua {@link UserApi}; chi mien user duoc dung repository cua minh. */
@Service
class UserApiImpl implements UserApi {

	private final UserRepository userRepository;
	private final AddressRepository addressRepository;

	UserApiImpl(UserRepository userRepository, AddressRepository addressRepository) {
		this.userRepository = userRepository;
		this.addressRepository = addressRepository;
	}

	@Override
	public Optional<User> findById(Long userId) {
		return userRepository.findById(userId);
	}

	@Override
	public List<User> activeStaff() {
		return userRepository.findByRoleInAndEnabledTrueAndDeletedFalse(List.of(Role.ADMIN, Role.STAFF));
	}

	@Override
	public Optional<Address> findOwnAddress(Long addressId, Long userId) {
		return addressRepository.findOwn(addressId, userId);
	}

	@Override
	public int pointsOf(Long userId) {
		return userRepository.pointsOf(userId);
	}

	@Override
	public boolean deductPoints(Long userId, int points) {
		return userRepository.deductPoints(userId, points) > 0;
	}

	@Override
	public void addPoints(Long userId, int points, int lifetimePoints) {
		userRepository.addPoints(userId, points, lifetimePoints);
	}

	@Override
	public long countCustomersCreatedBetween(LocalDateTime from, LocalDateTime to) {
		return userRepository.countByRoleAndCreatedAtBetween(Role.CUSTOMER, from, to);
	}
}
