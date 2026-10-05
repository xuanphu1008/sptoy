package com.toyland.common.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/** Luu List<String> (vd: danh sach URL anh) thanh 1 cot TEXT, moi phan tu 1 dong. */
@Converter
public class StringListConverter implements AttributeConverter<List<String>, String> {

	@Override
	public String convertToDatabaseColumn(List<String> list) {
		if (list == null || list.isEmpty()) {
			return null;
		}
		return String.join("\n", list);
	}

	@Override
	public List<String> convertToEntityAttribute(String data) {
		if (data == null || data.isBlank()) {
			return new ArrayList<>();
		}
		return new ArrayList<>(Arrays.stream(data.split("\n")).map(String::trim).filter(s -> !s.isEmpty()).toList());
	}
}
