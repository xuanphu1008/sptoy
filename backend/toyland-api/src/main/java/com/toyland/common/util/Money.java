package com.toyland.common.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Tien te VND: lam tron den 1.000 dong. */
public final class Money {

	private static final BigDecimal THOUSAND = BigDecimal.valueOf(1000);

	private Money() {
	}

	public static BigDecimal roundThousand(BigDecimal v) {
		return v.divide(THOUSAND, 0, RoundingMode.HALF_UP).multiply(THOUSAND);
	}

	/** Lam tron XUONG den 1.000d (gia khuyen mai khong vuot gia goc). */
	public static BigDecimal roundThousandDown(BigDecimal v) {
		return v.divide(THOUSAND, 0, RoundingMode.DOWN).multiply(THOUSAND);
	}

	public static BigDecimal percentOf(BigDecimal base, int percent) {
		return base.multiply(BigDecimal.valueOf(percent)).divide(BigDecimal.valueOf(100), 0, RoundingMode.DOWN);
	}

	public static BigDecimal nz(BigDecimal v) {
		return v == null ? BigDecimal.ZERO : v;
	}

	public static BigDecimal min(BigDecimal a, BigDecimal b) {
		return a.compareTo(b) <= 0 ? a : b;
	}
}
