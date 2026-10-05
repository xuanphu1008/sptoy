package com.toyland.common.exception;

import java.time.LocalDateTime;
import java.util.Map;

/** Cau truc loi thong nhat tra ve client. */
public record ErrorResponse(LocalDateTime timestamp, int status, String code, String message, String path,
		Map<String, String> details) {

	public static ErrorResponse of(int status, String code, String message, String path) {
		return new ErrorResponse(LocalDateTime.now(), status, code, message, path, null);
	}
}
