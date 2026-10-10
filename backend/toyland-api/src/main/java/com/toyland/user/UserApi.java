package com.toyland.user;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Giao dien cong bo cua mien Tai khoan (Identity) cho cac mien khac.
 * Mien khac KHONG duoc dung truc tiep UserRepository / AddressRepository; moi truy cap
 * du lieu nguoi dung di qua interface nay (ranh gioi dich vu, xem ArchitectureTest).
 */
public interface UserApi {

	Optional<User> findById(Long userId);

	/** Nhan vien + quan tri dang hoat dong (nhan thong bao noi bo). */
	List<User> activeStaff();

	/** Dia chi cua chinh nguoi dung (chong IDOR: sai chu so huu -> rong). */
	Optional<Address> findOwnAddress(Long addressId, Long userId);

	int pointsOf(Long userId);

	/** Tru diem nguyen tu; tra ve false neu khong du diem. */
	boolean deductPoints(Long userId, int points);

	void addPoints(Long userId, int points, int lifetimePoints);

	long countCustomersCreatedBetween(LocalDateTime from, LocalDateTime to);
}
