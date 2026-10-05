package com.toyland.user;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByUsername(String username);

	/** Nhan vien + quan tri dang hoat dong (nhan thong bao van hanh). */
	List<User> findByRoleInAndEnabledTrueAndDeletedFalse(Collection<Role> roles);

	/** Tru diem nguyen tu: tra ve 0 neu khong du diem (chong tieu diem 2 lan khi dat 2 don cung luc). */
	@Modifying(flushAutomatically = true)
	@Query("UPDATE User u SET u.points = u.points - :p WHERE u.id = :id AND u.points >= :p")
	int deductPoints(@Param("id") Long id, @Param("p") int points);

	/** Cong diem (lifetime = phan cong vao tong tich luy, 0 khi chi hoan diem). */
	@Modifying(flushAutomatically = true)
	@Query("UPDATE User u SET u.points = u.points + :p, u.lifetimePoints = u.lifetimePoints + :lifetime WHERE u.id = :id")
	int addPoints(@Param("id") Long id, @Param("p") int points, @Param("lifetime") int lifetime);

	@Query("SELECT u.points FROM User u WHERE u.id = :id")
	int pointsOf(@Param("id") Long id);

	Optional<User> findByEmailIgnoreCase(String email);

	boolean existsByUsername(String username);

	boolean existsByEmailIgnoreCase(String email);

	boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

	long countByRoleAndDeletedFalse(Role role);

	long countByRoleAndCreatedAtBetween(Role role, LocalDateTime from, LocalDateTime to);

	@Query("SELECT u FROM User u WHERE u.deleted = false "
			+ "AND (:role IS NULL OR u.role = :role) "
			+ "AND (:kw IS NULL OR LOWER(u.username) LIKE LOWER(CONCAT('%', :kw, '%')) "
			+ "     OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :kw, '%')) "
			+ "     OR LOWER(u.email) LIKE LOWER(CONCAT('%', :kw, '%')) "
			+ "     OR u.phone LIKE CONCAT('%', :kw, '%'))")
	Page<User> search(@Param("kw") String keyword, @Param("role") Role role, Pageable pageable);
}
