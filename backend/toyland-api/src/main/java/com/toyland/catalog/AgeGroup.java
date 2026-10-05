package com.toyland.catalog;

/** Nhom tuoi de loc san pham (theo do tuoi bat dau phu hop). Tham chieu MyKingdom. */
public enum AgeGroup {
	BABY("0-12 tháng", 0, 12),
	TODDLER("1-3 tuổi", 12, 36),
	PRESCHOOL("3-6 tuổi", 36, 72),
	SCHOOL("6-12 tuổi", 72, 144),
	TEEN("12+ tuổi", 144, 10000);

	private final String label;
	private final int fromMonths;
	private final int toMonthsExclusive;

	AgeGroup(String label, int fromMonths, int toMonthsExclusive) {
		this.label = label;
		this.fromMonths = fromMonths;
		this.toMonthsExclusive = toMonthsExclusive;
	}

	public String getLabel() { return label; }
	public int getFromMonths() { return fromMonths; }
	public int getToMonthsExclusive() { return toMonthsExclusive; }

	/** "3+", "18 tháng+", "3-6 tuổi"... */
	public static String labelOf(int minMonths, Integer maxMonths) {
		String min = minMonths < 24 && minMonths % 12 != 0 ? minMonths + " tháng" : String.valueOf(minMonths / 12);
		if (maxMonths == null) {
			return minMonths == 0 ? "0+" : min + "+";
		}
		String max = maxMonths < 24 && maxMonths % 12 != 0 ? maxMonths + " tháng" : String.valueOf(maxMonths / 12);
		return min + "-" + max + " tuổi";
	}
}
