package com.toyland.promotion;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VoucherRepository extends JpaRepository<Voucher, Long> {

	Optional<Voucher> findByCodeIgnoreCase(String code);

	boolean existsByCodeIgnoreCase(String code);

	boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);

	List<Voucher> findAllByOrderByIdDesc();

	@Query("SELECT v FROM Voucher v WHERE v.active = true AND v.startAt <= :now AND v.endAt >= :now "
			+ "AND (v.usageLimit IS NULL OR v.usedCount < v.usageLimit) ORDER BY v.minOrderValue ASC")
	List<Voucher> findAvailable(@Param("now") LocalDateTime now);

	/** Tang luot dung co dieu kien (an toan khi nhieu don dat cung luc). Tra ve so dong cap nhat (0 = da het luot). */
	@Modifying
	@Query("UPDATE Voucher v SET v.usedCount = v.usedCount + 1 WHERE v.id = :id AND (v.usageLimit IS NULL OR v.usedCount < v.usageLimit)")
	int incrementUsage(@Param("id") Long id);
}
