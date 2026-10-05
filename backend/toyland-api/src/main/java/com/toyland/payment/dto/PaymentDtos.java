package com.toyland.payment.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.toyland.order.OrderStatus;
import com.toyland.order.PaymentMethod;
import com.toyland.order.PaymentStatus;
import com.toyland.payment.PaymentKind;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** DTO cua module Thanh toan & doi soat (danh cho nhan vien / quan tri). */
public final class PaymentDtos {

	private PaymentDtos() {
	}

	/** 1 dong trong so cai thu - chi. */
	public record TransactionDto(Long id, Long orderId, String orderCode, String customerName, String provider, PaymentKind kind,
			BigDecimal amount, String status, String bankCode, String transactionNo, String responseCode, String txnRef, String note,
			String createdBy, LocalDateTime createdAt) {
	}

	/** So lieu doi soat trong ky + cac hang doi hien tai. */
	public record PaymentSummary(LocalDate from, LocalDate to, BigDecimal momoIn, BigDecimal codIn, BigDecimal otherIn,
			BigDecimal totalIn, BigDecimal refunded, BigDecimal net, long successCount, long momoFailed, double momoSuccessRate,
			long refundPendingCount, BigDecimal refundPendingAmount, long codOutstandingCount, BigDecimal codOutstandingAmount,
			List<DailyFlow> daily) {
	}

	public record DailyFlow(LocalDate date, BigDecimal in, BigDecimal out) {
	}

	/** Don dang cho hoan tien. */
	public record RefundQueueRow(Long orderId, String orderCode, String customerName, String phone, OrderStatus orderStatus,
			PaymentMethod paymentMethod, BigDecimal total, BigDecimal paid, BigDecimal refunded, BigDecimal remaining, String reason,
			LocalDateTime since) {
	}

	/** Don COD chua thu tien. */
	public record CodRow(Long orderId, String orderCode, String customerName, String phone, OrderStatus orderStatus, BigDecimal total,
			LocalDateTime createdAt) {
	}

	/** Toan bo tinh hinh thanh toan cua 1 don (hien trong trang chi tiet don cua admin). */
	public record OrderLedger(Long orderId, String orderCode, PaymentMethod paymentMethod, PaymentStatus paymentStatus, BigDecimal total,
			BigDecimal paid, BigDecimal refunded, BigDecimal refundRemaining, boolean canCollect, boolean canRefund,
			List<TransactionDto> transactions) {
	}

	/** Ghi nhan da thu tien (don COD: khach tra tien mat tai cua hang / chuyen khoan truoc). */
	public record CollectRequest(
			@NotBlank @Pattern(regexp = "CASH|BANK_TRANSFER", message = "Hình thức: CASH hoặc BANK_TRANSFER") String method,
			@Size(max = 50) String reference,
			@Size(max = 300) String note) {
	}

	/** Ghi nhan da hoan tien cho khach. */
	public record RefundRequest(
			@NotNull(message = "Nhập số tiền hoàn") @DecimalMin(value = "1000", message = "Số tiền hoàn tối thiểu 1.000đ") BigDecimal amount,
			@NotBlank @Pattern(regexp = "BANK_TRANSFER|CASH|MOMO", message = "Hình thức: BANK_TRANSFER, CASH hoặc MOMO") String method,
			@Size(max = 50) String reference,
			@Size(max = 300) String note) {
	}
}
