package com.toyland.common.exception;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	private ResponseEntity<ErrorResponse> build(HttpStatus status, String code, String message, HttpServletRequest req) {
		return ResponseEntity.status(status).body(ErrorResponse.of(status.value(), code, message, req.getRequestURI()));
	}

	@ExceptionHandler(ApiException.class)
	public ResponseEntity<ErrorResponse> handleApi(ApiException ex, HttpServletRequest req) {
		return build(ex.getStatus(), ex.getCode(), ex.getMessage(), req);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
		Map<String, String> details = new LinkedHashMap<>();
		ex.getBindingResult().getFieldErrors().forEach(fe -> details.putIfAbsent(fe.getField(), fe.getDefaultMessage()));
		ex.getBindingResult().getGlobalErrors().forEach(ge -> details.putIfAbsent(ge.getObjectName(), ge.getDefaultMessage()));
		return ResponseEntity.badRequest().body(new ErrorResponse(LocalDateTime.now(), 400, "VALIDATION_ERROR",
				"Dữ liệu không hợp lệ", req.getRequestURI(), details));
	}

	@ExceptionHandler({ HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
			MissingServletRequestParameterException.class })
	public ResponseEntity<ErrorResponse> handleBadInput(Exception ex, HttpServletRequest req) {
		return build(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "Dữ liệu gửi lên sai định dạng hoặc thiếu tham số", req);
	}

	@ExceptionHandler(BadCredentialsException.class)
	public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex, HttpServletRequest req) {
		return build(HttpStatus.UNAUTHORIZED, "BAD_CREDENTIALS", "Sai tên đăng nhập hoặc mật khẩu", req);
	}

	@ExceptionHandler(DisabledException.class)
	public ResponseEntity<ErrorResponse> handleDisabled(DisabledException ex, HttpServletRequest req) {
		return build(HttpStatus.FORBIDDEN, "ACCOUNT_DISABLED", "Tài khoản đã bị khoá", req);
	}

	@ExceptionHandler(LockedException.class)
	public ResponseEntity<ErrorResponse> handleLocked(LockedException ex, HttpServletRequest req) {
		return build(HttpStatus.FORBIDDEN, "ACCOUNT_LOCKED",
				"Tài khoản tạm khoá 15 phút do nhập sai mật khẩu nhiều lần", req);
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ErrorResponse> handleDenied(AccessDeniedException ex, HttpServletRequest req) {
		return build(HttpStatus.FORBIDDEN, "FORBIDDEN", "Bạn không có quyền thực hiện thao tác này", req);
	}

	@ExceptionHandler(ObjectOptimisticLockingFailureException.class)
	public ResponseEntity<ErrorResponse> handleOptimistic(ObjectOptimisticLockingFailureException ex, HttpServletRequest req) {
		return build(HttpStatus.CONFLICT, "CONCURRENT_UPDATE",
				"Dữ liệu vừa được người khác cập nhật (ví dụ tồn kho thay đổi). Vui lòng thử lại.", req);
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ErrorResponse> handleIntegrity(DataIntegrityViolationException ex, HttpServletRequest req) {
		log.warn("Data integrity: {}", ex.getMostSpecificCause().getMessage());
		return build(HttpStatus.CONFLICT, "DATA_CONFLICT", "Dữ liệu bị trùng hoặc đang được sử dụng ở nơi khác", req);
	}

	@ExceptionHandler(MaxUploadSizeExceededException.class)
	public ResponseEntity<ErrorResponse> handleUploadSize(MaxUploadSizeExceededException ex, HttpServletRequest req) {
		return build(HttpStatus.BAD_REQUEST, "FILE_TOO_LARGE", "File vượt quá dung lượng cho phép (5MB)", req);
	}

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<ErrorResponse> handleMethod(HttpRequestMethodNotSupportedException ex, HttpServletRequest req) {
		return build(HttpStatus.METHOD_NOT_ALLOWED, "METHOD_NOT_ALLOWED", "Phương thức " + ex.getMethod() + " không được hỗ trợ", req);
	}

	@ExceptionHandler(NoResourceFoundException.class)
	public ResponseEntity<ErrorResponse> handleNoResource(NoResourceFoundException ex, HttpServletRequest req) {
		return build(HttpStatus.NOT_FOUND, "NOT_FOUND", "Không tìm thấy đường dẫn", req);
	}

	/** Validation o cap tham so method (vd List<@Valid ...>) cua Spring 6.1+. */
	@ExceptionHandler(HandlerMethodValidationException.class)
	public ResponseEntity<ErrorResponse> handleMethodValidation(HandlerMethodValidationException ex, HttpServletRequest req) {
		return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Dữ liệu không hợp lệ", req);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleOther(Exception ex, HttpServletRequest req) {
		// Cac exception chuan cua Spring MVC (415, 406, ...) -> giu nguyen ma loi HTTP
		if (ex instanceof org.springframework.web.ErrorResponse er) {
			HttpStatus status = HttpStatus.resolve(er.getStatusCode().value());
			if (status != null && status.is4xxClientError()) {
				return build(status, "REQUEST_ERROR", "Yêu cầu không hợp lệ (" + status.value() + ")", req);
			}
		}
		log.error("Loi khong mong muon tai {}", req.getRequestURI(), ex);
		return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Lỗi máy chủ, vui lòng thử lại sau", req);
	}
}
