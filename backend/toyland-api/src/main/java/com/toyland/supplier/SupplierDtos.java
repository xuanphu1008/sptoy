package com.toyland.supplier;

import com.toyland.common.util.ValidationPatterns;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class SupplierDtos {

	private SupplierDtos() {
	}

	public record SupplierRequest(@Size(max = 30) @Pattern(regexp = "^$|^[A-Za-z0-9_-]+$", message = "Mã chỉ gồm chữ không dấu, số, - và _") String code,
			@NotBlank(message = "Nhập tên nhà cung cấp") @Size(max = 200) String name, @Size(max = 100) String contactName,
			@Size(max = 20) @Pattern(regexp = "^$|^[0-9+ .()-]{8,20}$", message = "Số điện thoại không hợp lệ") String phone,
			@Size(max = 100) @Email(regexp = ValidationPatterns.EMAIL, message = "Email không hợp lệ, ví dụ đúng: ten.ho2004@gmail.com") String email, @Size(max = 300) String address,
			@Size(max = 20) String taxCode, @Size(max = 500) String note, Boolean active) {
	}

	/** Kem so lieu nhap hang: so phieu, tong tien nhap, lan nhap gan nhat. */
	public record SupplierDto(Long id, String code, String name, String contactName, String phone, String email, String address,
			String taxCode, String note, boolean active, long receiptCount, BigDecimal totalImported, LocalDateTime lastReceiptAt,
			LocalDateTime createdAt) {
	}

	public record SupplierOption(Long id, String code, String name) {
	}
}
