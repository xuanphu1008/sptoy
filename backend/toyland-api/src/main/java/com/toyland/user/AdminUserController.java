package com.toyland.user;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.toyland.common.dto.MessageResponse;
import com.toyland.common.dto.PageResponse;
import com.toyland.user.dto.UserDtos.AdminUserDto;
import com.toyland.user.dto.UserDtos.AdminUserRequest;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "14. Quản trị - Tài khoản")
@RestController
@RequestMapping("/api/v1/admin/users")
public class AdminUserController {

	private final AdminUserService service;

	public AdminUserController(AdminUserService service) {
		this.service = service;
	}

	@GetMapping
	public PageResponse<AdminUserDto> list(@RequestParam(required = false) String keyword,
			@RequestParam(required = false) Role role, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		return service.list(keyword, role, page, size);
	}

	@GetMapping("/{id}")
	public AdminUserDto get(@PathVariable Long id) {
		return service.get(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public AdminUserDto create(@Valid @RequestBody AdminUserRequest r) {
		return service.create(r);
	}

	@PutMapping("/{id}")
	public AdminUserDto update(@PathVariable Long id, @Valid @RequestBody AdminUserRequest r) {
		return service.update(id, r);
	}

	@PutMapping("/{id}/unlock")
	public MessageResponse unlock(@PathVariable Long id) {
		service.unlock(id);
		return new MessageResponse("Đã mở khoá đăng nhập");
	}

	@DeleteMapping("/{id}")
	public MessageResponse delete(@PathVariable Long id) {
		service.delete(id);
		return new MessageResponse("Đã xoá tài khoản");
	}
}
