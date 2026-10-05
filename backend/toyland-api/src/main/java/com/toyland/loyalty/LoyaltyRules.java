package com.toyland.loyalty;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Quy tac khach hang than thiet (logic thuan -> kiem thu don vi duoc).
 *  - Tich diem: moi {@code earnPerAmount} dong tien hang (sau giam gia, khong tinh phi ship) = 1 diem x he so hang.
 *  - Dung diem: 1 diem = {@code pointValue} dong, toi da {@code maxRedeemPercent}% gia tri hang sau voucher.
 *  - Hang thanh vien xet theo tong diem da tich luy (lifetime).
 */
public final class LoyaltyRules {

	public record Tier(String code, String label, int minPoints, BigDecimal multiplier) {
	}

	public static final List<Tier> TIERS = List.of(
			new Tier("MEMBER", "Thành viên", 0, new BigDecimal("1.00")),
			new Tier("SILVER", "Bạc", 500, new BigDecimal("1.10")),
			new Tier("GOLD", "Vàng", 1500, new BigDecimal("1.25")),
			new Tier("DIAMOND", "Kim cương", 4000, new BigDecimal("1.50")));

	private final int earnPerAmount;
	private final int pointValue;
	private final int maxRedeemPercent;

	public LoyaltyRules(int earnPerAmount, int pointValue, int maxRedeemPercent) {
		this.earnPerAmount = Math.max(1, earnPerAmount);
		this.pointValue = Math.max(1, pointValue);
		this.maxRedeemPercent = Math.min(Math.max(0, maxRedeemPercent), 100);
	}

	public int earnPerAmount() { return earnPerAmount; }
	public int pointValue() { return pointValue; }
	public int maxRedeemPercent() { return maxRedeemPercent; }

	public static Tier tierOf(int lifetimePoints) {
		Tier t = TIERS.get(0);
		for (Tier x : TIERS) {
			if (lifetimePoints >= x.minPoints()) {
				t = x;
			}
		}
		return t;
	}

	/** Hang ke tiep (null neu da cao nhat). */
	public static Tier nextTier(int lifetimePoints) {
		for (Tier x : TIERS) {
			if (lifetimePoints < x.minPoints()) {
				return x;
			}
		}
		return null;
	}

	/** Diem nhan duoc cho so tien hang (da tru giam gia). */
	public int pointsToEarn(BigDecimal goodsAmount, int lifetimePoints) {
		if (goodsAmount == null || goodsAmount.signum() <= 0) {
			return 0;
		}
		BigDecimal base = goodsAmount.divide(BigDecimal.valueOf(earnPerAmount), 0, RoundingMode.DOWN);
		return base.multiply(tierOf(lifetimePoints).multiplier()).setScale(0, RoundingMode.DOWN).intValue();
	}

	/** So diem toi da duoc dung cho don co gia tri hang {@code goodsAmount}. */
	public int maxRedeemable(int balance, BigDecimal goodsAmount) {
		if (balance <= 0 || goodsAmount == null || goodsAmount.signum() <= 0) {
			return 0;
		}
		BigDecimal cap = goodsAmount.multiply(BigDecimal.valueOf(maxRedeemPercent)).divide(BigDecimal.valueOf(100L * pointValue), 0,
				RoundingMode.DOWN);
		return (int) Math.min(balance, cap.longValue());
	}

	public BigDecimal valueOf(int points) {
		return BigDecimal.valueOf((long) points * pointValue);
	}
}
