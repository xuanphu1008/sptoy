package com.toyland.idempotency;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.toyland.common.exception.ApiException;
import com.toyland.common.util.TokenHasher;

/** Kiem thu logic Idempotency-Key voi kho gia lap (khong can CSDL). */
class IdempotencyServiceTest {

	record Resp(long orderId) {
	}

	private IdempotencyStore store;
	private IdempotencyService service;

	@BeforeEach
	void setUp() {
		store = mock(IdempotencyStore.class);
		service = new IdempotencyService(store, new ObjectMapper());
	}

	@Test
	void khongCoKhoaThiChayBinhThuong() {
		AtomicInteger calls = new AtomicInteger();
		Resp r = service.execute(null, "user:1", "x", Resp.class, () -> new Resp(calls.incrementAndGet()));
		assertEquals(1, r.orderId());
		verifyNoInteractions(store);
	}

	@Test
	void khoaMoiChayMotLanVaLuuKetQua() {
		when(store.find("k1", "user:1")).thenReturn(Optional.empty());
		when(store.reserve(eq("k1"), eq("user:1"), anyString(), any())).thenReturn(true);
		Resp r = service.execute("k1", "user:1", "body", Resp.class, () -> new Resp(7));
		assertEquals(7, r.orderId());
		verify(store).complete(eq("k1"), eq("user:1"), eq(201), contains("7"));
	}

	@Test
	void guiLaiCungKhoaCungNoiDungNhanKetQuaCuKhongChayLai() {
		IdempotencyKey saved = new IdempotencyKey();
		saved.setRequestHash(TokenHasher.sha256("body"));
		saved.setState(IdempotencyKey.State.COMPLETED);
		saved.setResponseBody("{\"orderId\":7}");
		when(store.find("k1", "user:1")).thenReturn(Optional.of(saved));
		AtomicInteger calls = new AtomicInteger();
		Resp r = service.execute("k1", "user:1", "body", Resp.class, () -> new Resp(calls.incrementAndGet()));
		assertEquals(7, r.orderId());
		assertEquals(0, calls.get(), "thao tac khong duoc chay lai");
	}

	@Test
	void cungKhoaNoiDungKhacBi409() {
		IdempotencyKey saved = new IdempotencyKey();
		saved.setRequestHash(TokenHasher.sha256("body-cu"));
		saved.setState(IdempotencyKey.State.COMPLETED);
		saved.setResponseBody("{\"orderId\":7}");
		when(store.find("k1", "user:1")).thenReturn(Optional.of(saved));
		ApiException e = assertThrows(ApiException.class, () -> service.execute("k1", "user:1", "body-moi", Resp.class, () -> new Resp(1)));
		assertEquals("IDEMPOTENCY_KEY_REUSED", e.getCode());
		assertEquals(409, e.getStatus().value());
	}

	@Test
	void dangXuLyTra409() {
		IdempotencyKey saved = new IdempotencyKey();
		saved.setRequestHash(TokenHasher.sha256("body"));
		when(store.find("k1", "user:1")).thenReturn(Optional.of(saved));
		ApiException e = assertThrows(ApiException.class, () -> service.execute("k1", "user:1", "body", Resp.class, () -> new Resp(1)));
		assertEquals("IDEMPOTENCY_IN_PROGRESS", e.getCode());
	}

	@Test
	void thaoTacLoiThiGoKhoDeThuLai() {
		when(store.find("k1", "user:1")).thenReturn(Optional.empty());
		when(store.reserve(eq("k1"), eq("user:1"), anyString(), any())).thenReturn(true);
		assertThrows(ApiException.class, () -> service.execute("k1", "user:1", "body", Resp.class, () -> {
			throw ApiException.badRequest("CART_INVALID", "Gio hang het hang");
		}));
		verify(store).release("k1", "user:1");
		verify(store, never()).complete(any(), any(), anyInt(), any());
	}

	@Test
	void khoaKhongHopLeBi400() {
		assertThrows(ApiException.class, () -> service.execute("khoa co khoang trang", "user:1", "x", Resp.class, () -> new Resp(1)));
	}
}
