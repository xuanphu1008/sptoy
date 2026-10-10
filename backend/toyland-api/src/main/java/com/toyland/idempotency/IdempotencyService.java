package com.toyland.idempotency;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.function.Supplier;

import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.toyland.common.exception.ApiException;
import com.toyland.common.util.TokenHasher;

/**
 * Bao dam mot thao tac ghi (vd dat hang) chi co hieu luc MOT lan du client gui lai nhieu lan
 * (bam dup nut, mang chap chon, trinh duyet tu gui lai, doi tac retry).
 *
 * Quy tac (theo y tuong cua IETF draft "Idempotency-Key HTTP Header"):
 *  - Khong gui khoa  : chay binh thuong, khong bao ve.
 *  - Khoa moi        : giu cho khoa -> chay thao tac -> luu ket qua.
 *  - Khoa cu, cung than yeu cau, da xong : tra ve ket qua da luu, KHONG chay lai.
 *  - Khoa cu, dang chay        : 409 IDEMPOTENCY_IN_PROGRESS (client doi roi gui lai).
 *  - Khoa cu, than yeu cau khac: 409 IDEMPOTENCY_KEY_REUSED.
 *  - Thao tac nem loi          : go khoa de client co the thu lai cung khoa.
 */
@Service
public class IdempotencyService {

	public static final String HEADER = "Idempotency-Key";
	private static final int MAX_KEY_LENGTH = 80;
	private static final long TTL_HOURS = 24;

	private final IdempotencyStore store;
	private final ObjectMapper mapper;

	public IdempotencyService(IdempotencyStore store, ObjectMapper mapper) {
		this.store = store;
		this.mapper = mapper;
	}

	public <T> T execute(String key, String subject, String requestFingerprint, Class<T> type, Supplier<T> action) {
		if (key == null || key.isBlank()) {
			return action.get();
		}
		String k = key.trim();
		if (k.length() > MAX_KEY_LENGTH || !k.matches("[A-Za-z0-9._:-]+")) {
			throw ApiException.badRequest("IDEMPOTENCY_KEY_INVALID", "Idempotency-Key chỉ gồm chữ, số, . _ : - và tối đa 80 ký tự");
		}
		String hash = TokenHasher.sha256(requestFingerprint == null ? "" : requestFingerprint);

		Optional<IdempotencyKey> existing = store.find(k, subject);
		if (existing.isPresent()) {
			return replay(existing.get(), hash, type);
		}
		if (!store.reserve(k, subject, hash, LocalDateTime.now().plusHours(TTL_HOURS))) {
			// co yeu cau khac vua giu khoa giua luc ta kiem tra va giu cho
			return store.find(k, subject).map(e -> replay(e, hash, type)).orElseThrow(
					() -> new ApiException(HttpStatus.CONFLICT, "IDEMPOTENCY_IN_PROGRESS", "Yêu cầu đang được xử lý, vui lòng thử lại sau giây lát"));
		}
		T result;
		try {
			result = action.get();
		} catch (RuntimeException e) {
			store.release(k, subject);
			throw e;
		}
		try {
			store.complete(k, subject, HttpStatus.CREATED.value(), mapper.writeValueAsString(result));
		} catch (JsonProcessingException e) {
			store.release(k, subject);
		}
		return result;
	}

	private <T> T replay(IdempotencyKey e, String hash, Class<T> type) {
		if (!e.getRequestHash().equals(hash)) {
			throw new ApiException(HttpStatus.CONFLICT, "IDEMPOTENCY_KEY_REUSED",
					"Idempotency-Key này đã được dùng cho một yêu cầu khác, hãy tạo khóa mới");
		}
		if (e.getState() != IdempotencyKey.State.COMPLETED || e.getResponseBody() == null) {
			throw new ApiException(HttpStatus.CONFLICT, "IDEMPOTENCY_IN_PROGRESS", "Yêu cầu đang được xử lý, vui lòng thử lại sau giây lát");
		}
		try {
			return mapper.readValue(e.getResponseBody(), type);
		} catch (JsonProcessingException ex) {
			throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "IDEMPOTENCY_REPLAY_FAILED", "Không đọc lại được kết quả đã lưu");
		}
	}

	/** Don dau vet het han (24 gio) moi dem */
	@Scheduled(cron = "0 15 3 * * *")
	public void purgeExpired() {
		store.purgeExpired();
	}
}
