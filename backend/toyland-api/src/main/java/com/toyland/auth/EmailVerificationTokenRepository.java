package com.toyland.auth;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {

	@Query("SELECT t FROM EmailVerificationToken t JOIN FETCH t.user WHERE t.tokenHash = :h")
	Optional<EmailVerificationToken> findByHashWithUser(@Param("h") String tokenHash);

	Optional<EmailVerificationToken> findTopByUserIdOrderByIdDesc(Long userId);

	/** Gui link moi -> huy cac link cu chua dung (chi link moi nhat co hieu luc). */
	@Modifying(flushAutomatically = true)
	@Query("DELETE FROM EmailVerificationToken t WHERE t.user.id = :uid AND t.usedAt IS NULL")
	int deletePendingOfUser(@Param("uid") Long userId);
}
