package com.toyland.payment;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.toyland.audit.AuditService;
import com.toyland.auth.CurrentUserService;
import com.toyland.common.dto.PageResponse;
import com.toyland.common.exception.ApiException;
import com.toyland.common.util.TextUtils;
import com.toyland.notification.NotificationService;
import com.toyland.order.Order;
import com.toyland.order.OrderQueryApi;
import com.toyland.order.OrderStatus;
import com.toyland.order.PaymentMethod;
import com.toyland.order.PaymentStatus;
import com.toyland.order.ReturnRequest;
import com.toyland.order.ReturnStatus;
import com.toyland.payment.dto.PaymentDtos.CodRow;
import com.toyland.payment.dto.PaymentDtos.CollectRequest;
import com.toyland.payment.dto.PaymentDtos.DailyFlow;
import com.toyland.payment.dto.PaymentDtos.OrderLedger;
import com.toyland.payment.dto.PaymentDtos.PaymentSummary;
import com.toyland.payment.dto.PaymentDtos.RefundQueueRow;
import com.toyland.payment.dto.PaymentDtos.RefundRequest;
import com.toyland.payment.dto.PaymentDtos.TransactionDto;

/**
 * Nghiep vu "Thanh toan & doi soat" cua nhan vien:
 * xem so cai thu - chi, doi soat theo ky, ghi nhan da thu tien COD, ghi nhan da hoan tien.
 */
@Service
@Transactional
public class AdminPaymentService {

	/** Don COD ma tien hang con dang "nam ngoai" (chua thu). */
	static final List<OrderStatus> COD_OPEN = List.of(OrderStatus.PENDING, OrderStatus.CONFIRMED, OrderStatus.PACKING,
			OrderStatus.SHIPPING, OrderStatus.DELIVERY_FAILED);

	private final PaymentTransactionRepository txnRepository;
	private final PaymentLedgerService ledger;
	private final OrderQueryApi orders;
	private final CurrentUserService currentUser;
	private final NotificationService notificationService;
	private final AuditService audit;
	private final MomoService momoService;

	public AdminPaymentService(PaymentTransactionRepository txnRepository, PaymentLedgerService ledger, OrderQueryApi orders,
			CurrentUserService currentUser, NotificationService notificationService,
			AuditService audit, MomoService momoService) {
		this.txnRepository = txnRepository;
		this.ledger = ledger;
		this.orders = orders;
		this.currentUser = currentUser;
		this.notificationService = notificationService;
		this.audit = audit;
		this.momoService = momoService;
	}

	public record Period(LocalDate from, LocalDate to) {
	}

	/** Mac dinh 30 ngay gan nhat; toi da 1 nam. */
	public static Period period(LocalDate from, LocalDate to) {
		LocalDate t = to != null ? to : LocalDate.now();
		LocalDate f = from != null ? from : t.minusDays(29);
		if (f.isAfter(t)) {
			throw ApiException.badRequest("Ngày bắt đầu phải trước ngày kết thúc");
		}
		if (f.plusYears(1).isBefore(t)) {
			throw ApiException.badRequest("Khoảng thời gian tối đa 1 năm");
		}
		return new Period(f, t);
	}

	public static TransactionDto toDto(PaymentTransaction t) {
		Order o = t.getOrder();
		return new TransactionDto(t.getId(), o.getId(), o.getOrderCode(), o.getReceiverName(), t.getProvider(), t.getKind(),
				t.getAmount(), t.getStatus(), t.getBankCode(), t.getTransactionNo(), t.getResponseCode(), t.getTxnRef(), t.getNote(),
				t.getCreatedBy(), t.getCreatedAt());
	}

	// ------------------------------------------------------------ doc
	@Transactional(readOnly = true)
	public PageResponse<TransactionDto> transactions(String provider, String status, PaymentKind kind, LocalDate from, LocalDate to,
			String keyword, int page, int size) {
		Period p = period(from, to);
		Page<PaymentTransaction> r = txnRepository.search(TextUtils.trimToNull(provider), TextUtils.trimToNull(status), kind,
				p.from().atStartOfDay(), p.to().plusDays(1).atStartOfDay(), like(keyword),
				PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100)));
		return PageResponse.of(r, AdminPaymentService::toDto);
	}

	/** Toi da 5000 dong de xuat Excel. */
	@Transactional(readOnly = true)
	public List<TransactionDto> exportRows(String provider, String status, PaymentKind kind, LocalDate from, LocalDate to, String keyword) {
		Period p = period(from, to);
		return txnRepository.search(TextUtils.trimToNull(provider), TextUtils.trimToNull(status), kind, p.from().atStartOfDay(),
				p.to().plusDays(1).atStartOfDay(), like(keyword), PageRequest.of(0, 5000)).map(AdminPaymentService::toDto).getContent();
	}

	@Transactional(readOnly = true)
	public PaymentSummary summary(LocalDate from, LocalDate to) {
		Period p = period(from, to);
		BigDecimal momo = BigDecimal.ZERO, cod = BigDecimal.ZERO, other = BigDecimal.ZERO, refunded = BigDecimal.ZERO;
		long success = 0, momoOk = 0, momoFailed = 0;
		Map<LocalDate, BigDecimal[]> days = new TreeMap<>();
		for (LocalDate d = p.from(); !d.isAfter(p.to()); d = d.plusDays(1)) {
			days.put(d, new BigDecimal[] { BigDecimal.ZERO, BigDecimal.ZERO });
		}
		for (PaymentTransaction t : txnRepository.inPeriod(p.from().atStartOfDay(), p.to().plusDays(1).atStartOfDay())) {
			boolean isMomo = "MOMO".equals(t.getProvider());
			if (!"SUCCESS".equals(t.getStatus())) {
				if (isMomo && "FAILED".equals(t.getStatus())) {
					momoFailed++;
				}
				continue;
			}
			success++;
			BigDecimal[] day = days.get(t.getCreatedAt().toLocalDate());
			if (t.getKind() == PaymentKind.REFUND) {
				refunded = refunded.add(t.getAmount());
				if (day != null) {
					day[1] = day[1].add(t.getAmount());
				}
				continue;
			}
			if (isMomo) {
				momo = momo.add(t.getAmount());
				momoOk++;
			} else if ("COD".equals(t.getProvider())) {
				cod = cod.add(t.getAmount());
			} else {
				other = other.add(t.getAmount());
			}
			if (day != null) {
				day[0] = day[0].add(t.getAmount());
			}
		}
		BigDecimal totalIn = momo.add(cod).add(other);
		double rate = momoOk + momoFailed == 0 ? 0
				: BigDecimal.valueOf(momoOk * 100.0 / (momoOk + momoFailed)).setScale(1, RoundingMode.HALF_UP).doubleValue();

		List<RefundQueueRow> queue = refundQueue();
		BigDecimal pendingAmount = queue.stream().map(RefundQueueRow::remaining).reduce(BigDecimal.ZERO, BigDecimal::add);
		Object[] codTotals = orders.codOutstandingTotals(COD_OPEN);
		List<DailyFlow> daily = new ArrayList<>();
		days.forEach((d, v) -> daily.add(new DailyFlow(d, v[0], v[1])));
		return new PaymentSummary(p.from(), p.to(), momo, cod, other, totalIn, refunded, totalIn.subtract(refunded), success, momoFailed,
				rate, queue.size(), pendingAmount, ((Number) codTotals[0]).longValue(), new BigDecimal(codTotals[1].toString()), daily);
	}

	@Transactional(readOnly = true)
	public List<RefundQueueRow> refundQueue() {
		List<RefundQueueRow> rows = new ArrayList<>();
		for (Order o : orders.findRefundPending()) {
			BigDecimal[] a = amounts(o);
			String reason = switch (o.getStatus()) {
			case RETURNED -> "Khách trả hàng";
			case CANCELLED -> o.getInternalNote() != null && o.getInternalNote().contains("MoMo") ? "Thanh toán MoMo sau khi đơn đã huỷ"
					: "Huỷ đơn" + (o.getCancelReason() != null ? ": " + o.getCancelReason() : "");
			default -> "Cần hoàn tiền";
			};
			rows.add(new RefundQueueRow(o.getId(), o.getOrderCode(), o.getReceiverName(), o.getPhone(), o.getStatus(), o.getPaymentMethod(),
					o.getTotalAmount(), a[0], a[1], a[2], reason, o.getUpdatedAt()));
		}
		return rows;
	}

	@Transactional(readOnly = true)
	public PageResponse<CodRow> codOutstanding(int page, int size) {
		Page<Order> r = orders.findCodOutstanding(COD_OPEN, PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100)));
		return PageResponse.of(r, o -> new CodRow(o.getId(), o.getOrderCode(), o.getReceiverName(), o.getPhone(), o.getStatus(),
				o.getTotalAmount(), o.getCreatedAt()));
	}

	@Transactional(readOnly = true)
	public OrderLedger orderLedger(Long orderId) {
		return ledgerOf(find(orderId));
	}

	// ------------------------------------------------------------ ghi
	/** Nhan vien xac nhan da thu tien cua don COD (khach tra tien mat tai cua hang / chuyen khoan truoc). */
	public OrderLedger collect(Long orderId, CollectRequest r) {
		Order o = find(orderId);
		if (!canCollect(o)) {
			throw ApiException.badRequest("Đơn " + o.getOrderCode() + " không ở trạng thái chờ thu tiền");
		}
		String actor = currentUser.actorName();
		String note = TextUtils.trimToNull(r.note()) != null ? r.note().trim()
				: ("CASH".equals(r.method()) ? "Khách trả tiền mặt tại cửa hàng" : "Khách chuyển khoản trước khi giao");
		ledger.recordManualPayment(o, r.method(), TextUtils.trimToNull(r.reference()), note, actor);
		o.setPaymentStatus(PaymentStatus.PAID);
		o.setPaidAt(LocalDateTime.now());
		audit.log("PAYMENT", "ORDER", o.getOrderCode(), "Ghi nhận đã thu " + NotificationService.vnd(o.getTotalAmount()) + " đơn "
				+ o.getOrderCode() + " (" + label(r.method()) + ")");
		notificationService.notifyUser(o.getUser(), "PAYMENT_RECEIVED", "Đã nhận thanh toán đơn " + o.getOrderCode(),
				"ToyLand đã nhận " + NotificationService.vnd(o.getTotalAmount()) + " (" + label(r.method()) + "). Cảm ơn bạn!",
				"/account/orders/" + o.getId());
		return ledgerOf(o);
	}

	/** Ghi nhan da hoan tien; hoan du so tien can hoan -> don chuyen "Da hoan tien". */
	public OrderLedger refund(Long orderId, RefundRequest r) {
		Order o = find(orderId);
		if (o.getPaymentStatus() != PaymentStatus.REFUND_PENDING) {
			throw ApiException.badRequest("Đơn " + o.getOrderCode() + " không ở trạng thái chờ hoàn tiền");
		}
		BigDecimal remaining = amounts(o)[2];
		if (r.amount().compareTo(remaining) > 0) {
			throw ApiException.badRequest("Số tiền hoàn vượt quá số còn phải hoàn (" + NotificationService.vnd(remaining) + ")");
		}
		String reference = TextUtils.trimToNull(r.reference());
		if ("MOMO".equals(r.method())) {
			if (o.getPaymentMethod() != PaymentMethod.MOMO) {
				throw ApiException.badRequest("Chỉ hoàn qua MoMo cho đơn đã thanh toán bằng MoMo");
			}
			// goi API hoan tien cua MoMo (tien ve vi/the cua khach); MoMo tu choi -> bao loi, khong ghi so cai
			PaymentTransaction paid = txnRepository.findByOrder(o.getId()).stream()
					.filter(t -> "MOMO".equals(t.getProvider()) && t.getKind() == PaymentKind.PAYMENT && "SUCCESS".equals(t.getStatus()))
					.findFirst()
					.orElseThrow(() -> ApiException.badRequest("Không tìm thấy giao dịch MoMo thành công của đơn " + o.getOrderCode()));
			reference = momoService.refund(paid, r.amount(), "Hoan tien don " + o.getOrderCode());
		}
		String actor = currentUser.actorName();
		String note = TextUtils.trimToNull(r.note()) != null ? r.note().trim() : "Hoàn tiền " + label(r.method()).toLowerCase();
		ledger.recordRefund(o, r.amount(), r.method(), reference, note, actor);
		boolean done = r.amount().compareTo(remaining) == 0;
		if (done) {
			o.setPaymentStatus(PaymentStatus.REFUNDED);
		}
		audit.log("REFUND", "ORDER", o.getOrderCode(), "Hoàn " + NotificationService.vnd(r.amount()) + " cho đơn " + o.getOrderCode()
				+ " (" + label(r.method()) + (reference != null ? ", mã " + reference : "") + ")"
				+ (done ? "" : " - còn phải hoàn " + NotificationService.vnd(remaining.subtract(r.amount()))));
		notificationService.notifyUser(o.getUser(), "REFUND_DONE", "Đã hoàn tiền đơn " + o.getOrderCode(),
				"ToyLand đã hoàn " + NotificationService.vnd(r.amount()) + " qua " + label(r.method()).toLowerCase()
						+ ". Tiền có thể mất 1-3 ngày làm việc để về tài khoản.",
				"/account/orders/" + o.getId());
		return ledgerOf(o);
	}

	// ------------------------------------------------------------ tien ich
	private Order find(Long id) {
		return orders.findById(id).orElseThrow(() -> ApiException.notFound("đơn hàng", id));
	}

	private boolean canCollect(Order o) {
		return o.getPaymentMethod() == PaymentMethod.COD
				&& (o.getPaymentStatus() == PaymentStatus.UNPAID || o.getPaymentStatus() == PaymentStatus.FAILED)
				&& COD_OPEN.contains(o.getStatus());
	}

	/** [da thu, da hoan, con phai hoan]. Don tra hang: chi hoan so tien da duyet trong yeu cau tra hang. */
	private BigDecimal[] amounts(Order o) {
		BigDecimal paid = ledger.paid(o.getId());
		if (paid.signum() == 0 && (o.getPaymentStatus() == PaymentStatus.REFUND_PENDING || o.getPaymentStatus() == PaymentStatus.REFUNDED)) {
			paid = o.getTotalAmount(); // du lieu cu chua co so cai
		}
		BigDecimal refunded = ledger.refunded(o.getId());
		BigDecimal target = paid;
		if (o.getStatus() == OrderStatus.RETURNED) {
			ReturnRequest rr = orders.findReturnOfOrder(o.getId()).orElse(null);
			if (rr != null && rr.getStatus() == ReturnStatus.APPROVED && rr.getRefundAmount() != null) {
				target = rr.getRefundAmount().min(paid);
			}
		}
		BigDecimal remaining = o.getPaymentStatus() == PaymentStatus.REFUND_PENDING ? target.subtract(refunded).max(BigDecimal.ZERO)
				: BigDecimal.ZERO;
		return new BigDecimal[] { paid, refunded, remaining };
	}

	private OrderLedger ledgerOf(Order o) {
		BigDecimal[] a = amounts(o);
		List<TransactionDto> txns = txnRepository.findByOrder(o.getId()).stream().map(AdminPaymentService::toDto).toList();
		return new OrderLedger(o.getId(), o.getOrderCode(), o.getPaymentMethod(), o.getPaymentStatus(), o.getTotalAmount(), a[0], a[1], a[2],
				canCollect(o), o.getPaymentStatus() == PaymentStatus.REFUND_PENDING, txns);
	}

	private static String like(String keyword) {
		String k = TextUtils.trimToNull(keyword);
		return k == null ? null : "%" + k.toLowerCase() + "%";
	}

	public static String label(String method) {
		return switch (method == null ? "" : method) {
		case "MOMO" -> "Ví MoMo";
		case "COD" -> "Thu hộ COD";
		case "CASH" -> "Tiền mặt";
		case "BANK_TRANSFER" -> "Chuyển khoản";
		default -> method;
		};
	}
}
