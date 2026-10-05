package com.toyland.common.exception;

import org.springframework.http.HttpStatus;

/** Loi nghiep vu co ma HTTP + ma loi (code) de client xu ly. */
public class ApiException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	private final HttpStatus status;
	private final String code;

	public ApiException(HttpStatus status, String code, String message) {
		super(message);
		this.status = status;
		this.code = code;
	}

	public HttpStatus getStatus() { return status; }
	public String getCode() { return code; }

	public static ApiException notFound(String what, Object id) {
		return new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Không tìm thấy " + what + (id != null ? " (" + id + ")" : ""));
	}

	public static ApiException badRequest(String message) {
		return new ApiException(HttpStatus.BAD_REQUEST, "BAD_REQUEST", message);
	}

	public static ApiException badRequest(String code, String message) {
		return new ApiException(HttpStatus.BAD_REQUEST, code, message);
	}

	public static ApiException conflict(String message) {
		return new ApiException(HttpStatus.CONFLICT, "CONFLICT", message);
	}

	public static ApiException forbidden(String message) {
		return new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", message);
	}
}
