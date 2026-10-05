package com.toyland.review;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import com.toyland.common.util.TextUtils;

/**
 * So khop tu khoa cam (logic thuan, khong phu thuoc Spring -> kiem thu don vi duoc).
 * Van ban va tu khoa deu duoc bo dau + chu thuong; so khop theo NGUYEN TU (khong bat "dm" trong "admin").
 */
public final class KeywordModeration {

	private final List<String> keywords;
	private final List<Pattern> patterns;

	public KeywordModeration(List<String> rawKeywords) {
		this.keywords = new ArrayList<>();
		this.patterns = new ArrayList<>();
		for (String k : rawKeywords) {
			String n = normalizeKeyword(k);
			if (!n.isEmpty()) {
				keywords.add(n);
				patterns.add(Pattern.compile("(^|[^a-z0-9])" + Pattern.quote(n) + "($|[^a-z0-9])"));
			}
		}
	}

	/** "  Hàng   GIẢ " -> "hang gia" */
	public static String normalizeKeyword(String k) {
		return TextUtils.normalize(k == null ? "" : k);
	}

	/** Cac tu khoa cam xuat hien trong van ban (rong = hop le). */
	public List<String> find(String text) {
		if (text == null || text.isBlank() || keywords.isEmpty()) {
			return List.of();
		}
		String n = TextUtils.normalize(text);
		Set<String> hit = new LinkedHashSet<>();
		for (int i = 0; i < patterns.size(); i++) {
			if (patterns.get(i).matcher(n).find()) {
				hit.add(keywords.get(i));
			}
		}
		return new ArrayList<>(hit);
	}
}
