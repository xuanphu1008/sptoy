package com.toyland.supplier;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

/** Nha cung cap hang hoa (gan voi phieu nhap kho). */
@Entity
@Table(name = "suppliers")
public class Supplier {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "code", nullable = false, length = 30)
	private String code;

	@Column(name = "name", nullable = false, length = 200)
	private String name;

	@Column(name = "contact_name", length = 100)
	private String contactName;

	@Column(name = "phone", length = 20)
	private String phone;

	@Column(name = "email", length = 100)
	private String email;

	@Column(name = "address", length = 300)
	private String address;

	@Column(name = "tax_code", length = 20)
	private String taxCode;

	@Column(name = "note", length = 500)
	private String note;

	@Column(name = "active", nullable = false)
	private boolean active = true;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	@PrePersist
	protected void onCreate() {
		if (createdAt == null) {
			createdAt = LocalDateTime.now();
		}
		updatedAt = LocalDateTime.now();
	}

	@PreUpdate
	protected void onUpdate() {
		updatedAt = LocalDateTime.now();
	}

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }
	public String getCode() { return code; }
	public void setCode(String code) { this.code = code; }
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	public String getContactName() { return contactName; }
	public void setContactName(String contactName) { this.contactName = contactName; }
	public String getPhone() { return phone; }
	public void setPhone(String phone) { this.phone = phone; }
	public String getEmail() { return email; }
	public void setEmail(String email) { this.email = email; }
	public String getAddress() { return address; }
	public void setAddress(String address) { this.address = address; }
	public String getTaxCode() { return taxCode; }
	public void setTaxCode(String taxCode) { this.taxCode = taxCode; }
	public String getNote() { return note; }
	public void setNote(String note) { this.note = note; }
	public boolean isActive() { return active; }
	public void setActive(boolean active) { this.active = active; }
	public LocalDateTime getCreatedAt() { return createdAt; }
	public LocalDateTime getUpdatedAt() { return updatedAt; }
}
