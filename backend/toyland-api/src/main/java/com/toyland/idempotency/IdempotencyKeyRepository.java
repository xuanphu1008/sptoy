package com.toyland.idempotency;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKey, Long> {

	Optional<IdempotencyKey> findByKeyAndSubject(String key, String subject);

	@Modifying
	@Query("DELETE FROM IdempotencyKey k WHERE k.expiresAt < :now")
	int deleteExpired(@Param("now") LocalDateTime now);
}
