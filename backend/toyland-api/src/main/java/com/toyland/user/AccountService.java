package com.toyland.user;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.toyland.auth.CurrentUserService;
import com.toyland.auth.EmailVerificationService;
import com.toyland.auth.RefreshTokenRepository;
import com.toyland.cart.CartItemRepository;
import com.toyland.common.exception.ApiException;
import com.toyland.common.util.TextUtils;
import com.toyland.order.OrderRepository;
import com.toyland.user.dto.UserDtos.AddressDto;
import com.toyland.user.dto.UserDtos.AddressRequest;
import com.toyland.user.dto.UserDtos.ChangePasswordRequest;
import com.toyland.user.dto.UserDtos.ProfileDto;
import com.toyland.user.dto.UserDtos.ProfileRequest;
import com.toyland.wishlist.WishlistRepository;

/** Khach hang tu quan ly tai khoan: ho so, mat khau, so dia chi, xoa tai khoan. */
@Service
@Transactional
public class AccountService {

	private static final int MAX_ADDRESSES = 10;

	private final CurrentUserService currentUser;
	private final UserRepository userRepository;
	private final AddressRepository addressRepository;
	private final RefreshTokenRepository refreshTokenRepository;
	private final OrderRepository orderRepository;
	private final CartItemRepository cartItemRepository;
	private final WishlistRepository wishlistRepository;
	private final PasswordEncoder passwordEncoder;
	private final EmailVerificationService emailVerification;

	public AccountService(CurrentUserService currentUser, UserRepository userRepository, AddressRepository addressRepository,
			RefreshTokenRepository refreshTokenRepository, OrderRepository orderRepository,
			CartItemRepository cartItemRepository, WishlistRepository wishlistRepository, PasswordEncoder passwordEncoder,
			EmailVerificationService emailVerification) {
		this.currentUser = currentUser;
		this.userRepository = userRepository;
		this.addressRepository = addressRepository;
		this.refreshTokenRepository = refreshTokenRepository;
		this.orderRepository = orderRepository;
		this.cartItemRepository = cartItemRepository;
		this.wishlistRepository = wishlistRepository;
		this.passwordEncoder = passwordEncoder;
		this.emailVerification = emailVerification;
	}

	public static ProfileDto toProfile(User u) {
		return new ProfileDto(u.getId(), u.getUsername(), u.getFullName(), u.getEmail(), u.getPhone(), u.getRole(),
				u.getCreatedAt(), u.getConsentAt(), u.isEmailVerified());
	}

	public static AddressDto toDto(Address a) {
		return new AddressDto(a.getId(), a.getReceiverName(), a.getPhone(), a.getProvinceId(), a.getProvinceName(),
				a.getDistrictId(), a.getDistrictName(), a.getWardCode(), a.getWardName(), a.getDetail(), a.isDefaultAddress(),
				a.getDetail() + ", " + a.getWardName() + ", " + a.getDistrictName() + ", " + a.getProvinceName());
	}

	@Transactional(readOnly = true)
	public ProfileDto profile() {
		return toProfile(currentUser.require());
	}

	public ProfileDto updateProfile(ProfileRequest r) {
		User u = currentUser.require();
		if (userRepository.existsByEmailIgnoreCaseAndIdNot(r.email().trim(), u.getId())) {
			throw ApiException.conflict("Email đã được sử dụng");
		}
		u.setFullName(r.fullName().trim());
		String newEmail = r.email().trim();
		boolean emailChanged = !newEmail.equalsIgnoreCase(u.getEmail());
		u.setEmail(newEmail);
		u.setPhone(TextUtils.trimToNull(r.phone()));
		if (emailChanged && u.getRole() == Role.CUSTOMER) {
			// Doi email -> phai xac nhan lai email moi (khong thi xac nhan 1 email that roi doi sang email bat ky)
			u.setEmailVerifiedAt(null);
		}
		u = userRepository.save(u);
		if (emailChanged && u.getRole() == Role.CUSTOMER) {
			emailVerification.sendLink(u);
		}
		return toProfile(u);
	}

	public void changePassword(ChangePasswordRequest r) {
		User u = currentUser.require();
		if (!passwordEncoder.matches(r.currentPassword(), u.getPassword())) {
			throw ApiException.badRequest("WRONG_PASSWORD", "Mật khẩu hiện tại không đúng");
		}
		u.setPassword(passwordEncoder.encode(r.newPassword()));
		userRepository.save(u);
		refreshTokenRepository.revokeAllOfUser(u.getId()); // dang xuat khoi cac thiet bi khac
	}

	/**
	 * Xoa tai khoan (quyen duoc xoa du lieu - Luat BVDLCN). Neu da co don hang: an danh hoa
	 * thong tin ca nhan (giu don de doi soat), nguoc lai xoa han.
	 */
	public void deleteAccount(String password) {
		User u = currentUser.require();
		if (u.getRole() != Role.CUSTOMER) {
			throw ApiException.forbidden("Tài khoản quản trị không thể tự xoá");
		}
		if (!passwordEncoder.matches(password, u.getPassword())) {
			throw ApiException.badRequest("WRONG_PASSWORD", "Mật khẩu không đúng");
		}
		refreshTokenRepository.revokeAllOfUser(u.getId());
		cartItemRepository.deleteAllOfUser(u.getId());
		addressRepository.deleteAllOfUser(u.getId());
		wishlistRepository.deleteAll(wishlistRepository.findAllOfUser(u.getId()));
		if (orderRepository.countOfUser(u.getId()) > 0) {
			u.setUsername("deleted_" + u.getId());
			u.setFullName("Người dùng đã xoá");
			u.setEmail("deleted_" + u.getId() + "@toyland.local");
			u.setPhone(null);
			u.setEnabled(false);
			u.setDeleted(true);
			userRepository.save(u);
		} else {
			userRepository.delete(u);
		}
	}

	// ------------------------------------------------ so dia chi
	@Transactional(readOnly = true)
	public List<AddressDto> addresses() {
		return addressRepository.findAllOfUser(currentUser.requireId()).stream().map(AccountService::toDto).toList();
	}

	public AddressDto saveAddress(Long id, AddressRequest r) {
		User u = currentUser.require();
		Address a;
		if (id == null) {
			if (addressRepository.countOfUser(u.getId()) >= MAX_ADDRESSES) {
				throw ApiException.badRequest("Tối đa " + MAX_ADDRESSES + " địa chỉ");
			}
			a = new Address();
			a.setUser(u);
		} else {
			a = addressRepository.findOwn(id, u.getId()).orElseThrow(() -> ApiException.notFound("địa chỉ", id));
		}
		boolean makeDefault = r.isDefault() || addressRepository.countOfUser(u.getId()) == 0;
		a.setReceiverName(r.receiverName().trim());
		a.setPhone(r.phone().trim());
		a.setProvinceId(r.provinceId());
		a.setProvinceName(r.provinceName().trim());
		a.setDistrictId(r.districtId());
		a.setDistrictName(r.districtName().trim());
		a.setWardCode(TextUtils.trimToNull(r.wardCode()));
		a.setWardName(r.wardName().trim());
		a.setDetail(r.detail().trim());
		a = addressRepository.save(a);
		if (makeDefault) {
			markDefault(u.getId(), a.getId());
		}
		return toDto(a);
	}

	/** Chi 1 dia chi mac dinh cho moi nguoi dung. */
	private void markDefault(Long userId, Long addressId) {
		for (Address x : addressRepository.findAllOfUser(userId)) {
			x.setDefaultAddress(x.getId().equals(addressId));
		}
	}

	public void deleteAddress(Long id) {
		Long uid = currentUser.requireId();
		Address a = addressRepository.findOwn(id, uid).orElseThrow(() -> ApiException.notFound("địa chỉ", id));
		boolean wasDefault = a.isDefaultAddress();
		addressRepository.delete(a);
		addressRepository.flush();
		if (wasDefault) {
			addressRepository.findAllOfUser(uid).stream().findFirst().ifPresent(x -> x.setDefaultAddress(true));
		}
	}

	public void setDefault(Long id) {
		Long uid = currentUser.requireId();
		addressRepository.findOwn(id, uid).orElseThrow(() -> ApiException.notFound("địa chỉ", id));
		markDefault(uid, id);
	}
}
