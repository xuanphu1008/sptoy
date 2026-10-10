package com.toyland.idempotency;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cac thao tac ghi dau vet chay trong giao dich RIENG (REQUIRES_NEW): viec "giu cho" khoa phai commit ngay,
 * khong phu thuoc vao giao dich dat hang ben ngoai (vi giao dich do co the rollback).
 */
@Component
class IdempotencyStore {

	private final IdempotencyKeyRepository repository;
	private final org.springframework.jdbc.core.JdbcTemplate jdbc;

	IdempotencyStore(IdempotencyKeyRepository repository, org.springframework.jdbc.core.JdbcTemplate jdbc) {
		this.repository = repository;
		this.jdbc = jdbc;
	}

	@Transactional(readOnly = true)
	Optional<IdempotencyKey> find(String key, String subject) {
		return repository.findByKeyAndSubject(key, subject);
	}

	/**
	 * Giu cho khoa bang INSERT IGNORE tren ket noi rieng (tu commit): khi hai yeu cau cung luc tranh nhau mot khoa,
	 * rang buoc UNIQUE chi cho mot yeu cau chen duoc (so dong = 1), yeu cau con lai nhan 0 ma KHONG nem loi
	 * (nem loi trong giao dich se danh dau rollback-only va gay 500).
	 */
	boolean reserve(String key, String subject, String hash, LocalDateTime expiresAt) {
		return jdbc.update("INSERT IGNORE INTO idempotency_keys (idem_key, subject, request_hash, state, created_at, expires_at) "
				+ "VALUES (?, ?, ?, 'IN_PROGRESS', NOW(), ?)", key, subject, hash, java.sql.Timestamp.valueOf(expiresAt)) == 1;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	void complete(String key, String subject, int status, String body) {
		repository.findByKeyAndSubject(key, subject).ifPresent(k -> {
			k.setState(IdempotencyKey.State.COMPLETED);
			k.setResponseStatus(status);
			k.setResponseBody(body);
		});
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	void release(String key, String subject) {
		repository.findByKeyAndSubject(key, subject).ifPresent(repository::delete);
	}

	@Transactional
	int purgeExpired() {
		return repository.deleteExpired(LocalDateTime.now());
	}
}
