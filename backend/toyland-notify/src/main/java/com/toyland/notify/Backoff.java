package com.toyland.notify;

import java.util.concurrent.ThreadLocalRandom;

/** Thoi gian cho truoc lan thu lai: tang gap doi moi lan, co tran, cong them nhieu ngau nhien (jitter) de tranh don dap. */
public final class Backoff {

	private Backoff() {
	}

	/** @param attempt so lan da that bai (>= 1) */
	public static long seconds(int attempt, long baseSeconds, long maxSeconds) {
		long exp = baseSeconds << Math.min(Math.max(attempt - 1, 0), 20);
		long capped = Math.min(exp, maxSeconds);
		long jitter = ThreadLocalRandom.current().nextLong(0, Math.max(1, capped / 4) + 1);
		return Math.min(capped + jitter, maxSeconds);
	}
}
