package com.toyland.supplier;

import java.util.List;

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
import com.toyland.supplier.SupplierDtos.SupplierDto;
import com.toyland.supplier.SupplierDtos.SupplierOption;
import com.toyland.supplier.SupplierDtos.SupplierRequest;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "17. Quản trị - Nhà cung cấp")
@RestController
@RequestMapping("/api/v1/admin/suppliers")
public class AdminSupplierController {

	private final SupplierService service;

	public AdminSupplierController(SupplierService service) {
		this.service = service;
	}

	@GetMapping
	public PageResponse<SupplierDto> list(@RequestParam(required = false) String keyword,
			@RequestParam(required = false) Boolean active, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		return service.list(keyword, active, page, size);
	}

	/** Danh sach rut gon (dang giao dich) cho o chon tren phieu nhap. */
	@GetMapping("/options")
	public List<SupplierOption> options() {
		return service.options();
	}

	@GetMapping("/{id}")
	public SupplierDto get(@PathVariable Long id) {
		return service.get(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public SupplierDto create(@Valid @RequestBody SupplierRequest r) {
		return service.save(null, r);
	}

	@PutMapping("/{id}")
	public SupplierDto update(@PathVariable Long id, @Valid @RequestBody SupplierRequest r) {
		return service.save(id, r);
	}

	@DeleteMapping("/{id}")
	public MessageResponse delete(@PathVariable Long id) {
		service.delete(id);
		return new MessageResponse("Đã xoá nhà cung cấp");
	}
}
