package com.toyland.common.util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

/** Xu ly chuoi tieng Viet: bo dau (tim kiem khong dau), tao slug URL. */
public final class TextUtils {

	private static final Pattern MARKS = Pattern.compile("\\p{M}+");
	private static final Pattern NON_SLUG = Pattern.compile("[^a-z0-9]+");
	private static final Pattern SPACES = Pattern.compile("\\s+");

	private TextUtils() {
	}

	/** "Búp bê Đẹp" -> "bup be dep" (khop voi cot products.search_text). */
	public static String normalize(String input) {
		if (input == null) {
			return "";
		}
		String s = input.toLowerCase(Locale.ROOT).replace('đ', 'd');
		s = Normalizer.normalize(s, Normalizer.Form.NFD);
		s = MARKS.matcher(s).replaceAll("");
		return SPACES.matcher(s).replaceAll(" ").trim();
	}

	/** "LEGO City Trung tâm" -> "lego-city-trung-tam". */
	public static String slugify(String input) {
		String s = NON_SLUG.matcher(normalize(input)).replaceAll("-");
		s = s.replaceAll("^-+|-+$", "");
		return s.isEmpty() ? "item" : s;
	}

	public static boolean isBlank(String s) {
		return s == null || s.isBlank();
	}

	public static String trimToNull(String s) {
		return isBlank(s) ? null : s.trim();
	}

	/** "Nguyễn Văn Khách" -> "N***h" de hien thi an danh trong danh gia. */
	public static String maskName(String name) {
		if (isBlank(name)) {
			return "Khách hàng";
		}
		String n = name.trim();
		if (n.length() <= 2) {
			return n.charAt(0) + "*";
		}
		return n.charAt(0) + "***" + n.charAt(n.length() - 1);
	}
}
