package com.toyland.payment;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.toyland.audit.AuditService;
import com.toyland.common.dto.PageResponse;
import com.toyland.payment.AdminPaymentService.Period;
import com.toyland.payment.dto.PaymentDtos.CodRow;
import com.toyland.payment.dto.PaymentDtos.CollectRequest;
import com.toyland.payment.dto.PaymentDtos.OrderLedger;
import com.toyland.payment.dto.PaymentDtos.PaymentSummary;
import com.toyland.payment.dto.PaymentDtos.RefundQueueRow;
import com.toyland.payment.dto.PaymentDtos.RefundRequest;
import com.toyland.payment.dto.PaymentDtos.TransactionDto;
import com.toyland.report.ExcelExporter;
import com.toyland.report.ExcelExporter.Col;
import com.toyland.report.ExcelExporter.Kind;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/** Thanh toan & doi soat (ADMIN + STAFF). */
@Tag(name = "Quản trị - Thanh toán & đối soát")
@RestController
@RequestMapping("/api/v1/admin/payments")
public class AdminPaymentController {

	private static final MediaType XLSX = MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
	private static final DateTimeFormatter D = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	private static final DateTimeFormatter F = DateTimeFormatter.ofPattern("yyyyMMdd");

	private final AdminPaymentService service;
	private final AuditService audit;

	public AdminPaymentController(AdminPaymentService service, AuditService audit) {
		this.service = service;
		this.audit = audit;
	}

	@GetMapping("/summary")
	public PaymentSummary summary(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
		return service.summary(from, to);
	}

	/** So cai thu - chi: loc theo cong (MOMO/COD/CASH/BANK_TRANSFER), trang thai, chieu tien, khoang ngay, tu khoa. */
	@GetMapping("/transactions")
	public PageResponse<TransactionDto> transactions(@RequestParam(required = false) String provider,
			@RequestParam(required = false) String status, @RequestParam(required = false) PaymentKind kind,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
			@RequestParam(required = false) String keyword, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		return service.transactions(provider, status, kind, from, to, keyword, page, size);
	}

	@GetMapping("/transactions/export")
	public ResponseEntity<byte[]> export(@RequestParam(required = false) String provider, @RequestParam(required = false) String status,
			@RequestParam(required = false) PaymentKind kind,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
			@RequestParam(required = false) String keyword) {
		Period p = AdminPaymentService.period(from, to);
		List<TransactionDto> rows = service.exportRows(provider, status, kind, p.from(), p.to(), keyword);
		List<Col<TransactionDto>> cols = List.of(
				new Col<TransactionDto>("Thời gian", Kind.DATETIME, TransactionDto::createdAt),
				new Col<TransactionDto>("Mã đơn", Kind.TEXT, TransactionDto::orderCode),
				new Col<TransactionDto>("Khách hàng", Kind.TEXT, TransactionDto::customerName),
				new Col<TransactionDto>("Loại", Kind.TEXT, t -> t.kind() == PaymentKind.REFUND ? "Hoàn tiền" : "Thu tiền"),
				new Col<TransactionDto>("Hình thức", Kind.TEXT, t -> AdminPaymentService.label(t.provider())),
				new Col<TransactionDto>("Số tiền (đ)", Kind.MONEY, t -> t.kind() == PaymentKind.REFUND ? t.amount().negate() : t.amount()),
				new Col<TransactionDto>("Trạng thái", Kind.TEXT, TransactionDto::status),
				new Col<TransactionDto>("Ngân hàng", Kind.TEXT, TransactionDto::bankCode),
				new Col<TransactionDto>("Mã giao dịch", Kind.TEXT, TransactionDto::transactionNo),
				new Col<TransactionDto>("Mã tham chiếu", Kind.TEXT, TransactionDto::txnRef),
				new Col<TransactionDto>("Ghi chú", Kind.TEXT, TransactionDto::note),
				new Col<TransactionDto>("Người ghi nhận", Kind.TEXT, TransactionDto::createdBy));
		byte[] file = ExcelExporter.export("SỔ THU - CHI THANH TOÁN", "Từ " + p.from().format(D) + " đến " + p.to().format(D), cols, rows, null);
		String filename = "so-thu-chi-" + p.from().format(F) + "-" + p.to().format(F) + ".xlsx";
		audit.log("EXPORT", "PAYMENT", filename, "Xuất Excel sổ thu - chi (" + p.from().format(D) + " - " + p.to().format(D) + ")");
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(filename, StandardCharsets.UTF_8).build().toString())
				.contentType(XLSX)
				.body(file);
	}

	@GetMapping("/refund-queue")
	public List<RefundQueueRow> refundQueue() {
		return service.refundQueue();
	}

	@GetMapping("/cod-outstanding")
	public PageResponse<CodRow> codOutstanding(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
		return service.codOutstanding(page, size);
	}

	@GetMapping("/orders/{orderId}")
	public OrderLedger orderLedger(@PathVariable Long orderId) {
		return service.orderLedger(orderId);
	}

	@PostMapping("/orders/{orderId}/collect")
	public OrderLedger collect(@PathVariable Long orderId, @Valid @RequestBody CollectRequest r) {
		return service.collect(orderId, r);
	}

	@PostMapping("/orders/{orderId}/refund")
	public OrderLedger refund(@PathVariable Long orderId, @Valid @RequestBody RefundRequest r) {
		return service.refund(orderId, r);
	}
}
