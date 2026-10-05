package com.toyland.user;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.*;

/** So dia chi giao hang (ma dia gioi theo GHN). */
@Entity
@Table(name = "addresses")
public class Address {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(name = "receiver_name", nullable = false, length = 100)
	private String receiverName;

	@Column(name = "phone", nullable = false, length = 20)
	private String phone;

	@Column(name = "province_id")
	private Integer provinceId;

	@Column(name = "province_name", nullable = false)
	private String provinceName;

	@Column(name = "district_id")
	private Integer districtId;

	@Column(name = "district_name", nullable = false)
	private String districtName;

	@Column(name = "ward_code", length = 20)
	private String wardCode;

	@Column(name = "ward_name", nullable = false)
	private String wardName;

	@Column(name = "detail", nullable = false)
	private String detail;

	@Column(name = "is_default", nullable = false)
	private boolean defaultAddress;

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
	public User getUser() { return user; }
	public void setUser(User user) { this.user = user; }
	public String getReceiverName() { return receiverName; }
	public void setReceiverName(String receiverName) { this.receiverName = receiverName; }
	public String getPhone() { return phone; }
	public void setPhone(String phone) { this.phone = phone; }
	public Integer getProvinceId() { return provinceId; }
	public void setProvinceId(Integer provinceId) { this.provinceId = provinceId; }
	public String getProvinceName() { return provinceName; }
	public void setProvinceName(String provinceName) { this.provinceName = provinceName; }
	public Integer getDistrictId() { return districtId; }
	public void setDistrictId(Integer districtId) { this.districtId = districtId; }
	public String getDistrictName() { return districtName; }
	public void setDistrictName(String districtName) { this.districtName = districtName; }
	public String getWardCode() { return wardCode; }
	public void setWardCode(String wardCode) { this.wardCode = wardCode; }
	public String getWardName() { return wardName; }
	public void setWardName(String wardName) { this.wardName = wardName; }
	public String getDetail() { return detail; }
	public void setDetail(String detail) { this.detail = detail; }
	public boolean isDefaultAddress() { return defaultAddress; }
	public void setDefaultAddress(boolean defaultAddress) { this.defaultAddress = defaultAddress; }
	public LocalDateTime getCreatedAt() { return createdAt; }
	public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
	public LocalDateTime getUpdatedAt() { return updatedAt; }
	public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
