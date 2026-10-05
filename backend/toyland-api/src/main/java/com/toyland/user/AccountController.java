package com.toyland.user;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.toyland.common.dto.MessageResponse;
import com.toyland.user.dto.UserDtos.AddressDto;
import com.toyland.user.dto.UserDtos.AddressRequest;
import com.toyland.user.dto.UserDtos.ChangePasswordRequest;
import com.toyland.user.dto.UserDtos.DeleteAccountRequest;
import com.toyland.user.dto.UserDtos.ProfileDto;
import com.toyland.user.dto.UserDtos.ProfileRequest;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "03. Tài khoản của tôi")
@RestController
@RequestMapping("/api/v1/me")
public class AccountController {

	private final AccountService service;

	public AccountController(AccountService service) {
		this.service = service;
	}

	@GetMapping
	public ProfileDto profile() {
		return service.profile();
	}

	@PutMapping
	public ProfileDto update(@Valid @RequestBody ProfileRequest r) {
		return service.updateProfile(r);
	}

	@PutMapping("/password")
	public MessageResponse changePassword(@Valid @RequestBody ChangePasswordRequest r) {
		service.changePassword(r);
		return new MessageResponse("Đổi mật khẩu thành công");
	}

	@PostMapping("/delete")
	public MessageResponse deleteAccount(@Valid @RequestBody DeleteAccountRequest r) {
		service.deleteAccount(r.password());
		return new MessageResponse("Tài khoản đã được xoá");
	}

	@GetMapping("/addresses")
	public List<AddressDto> addresses() {
		return service.addresses();
	}

	@PostMapping("/addresses")
	@ResponseStatus(HttpStatus.CREATED)
	public AddressDto addAddress(@Valid @RequestBody AddressRequest r) {
		return service.saveAddress(null, r);
	}

	@PutMapping("/addresses/{id}")
	public AddressDto updateAddress(@PathVariable Long id, @Valid @RequestBody AddressRequest r) {
		return service.saveAddress(id, r);
	}

	@PutMapping("/addresses/{id}/default")
	public MessageResponse setDefault(@PathVariable Long id) {
		service.setDefault(id);
		return new MessageResponse("Đã đặt làm địa chỉ mặc định");
	}

	@DeleteMapping("/addresses/{id}")
	public MessageResponse deleteAddress(@PathVariable Long id) {
		service.deleteAddress(id);
		return new MessageResponse("Đã xoá địa chỉ");
	}
}
