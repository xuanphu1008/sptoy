package com.toyland.payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.toyland.order.Order;

/**
 * Ghi so cai thu - chi (payment_transactions). Khong phu thuoc OrderWorkflow de tranh vong phu thuoc:
 * OrderWorkflow / MomoService / AdminPaymentService deu goi vao day.
 */
@Service
@Transactional
public class PaymentLedgerService {

	private final PaymentTransactionRepository repo;

	public PaymentLedgerService(PaymentTransactionRepository repo) {
		this.repo = repo;
	}

	/** Don COD giao thanh cong -> shipper da thu tien mat cua khach. */
	public void recordCodCollected(Order o, String actor) {
		String ref = "COD-" + o.getOrderCode();
		if (repo.existsByTxnRef(ref)) {
			return;
		}
		record(o, "COD", ref, o.getTotalAmount(), PaymentKind.PAYMENT, null, "Thu tiền mặt khi giao hàng (COD)", actor);
	}

	/** Nhan vien ghi nhan khach da tra (tien mat tai quay / chuyen khoan truoc) cho don COD. */
	public PaymentTransaction recordManualPayment(Order o, String method, String reference, String note, String actor) {
		String ref = "PAY-" + o.getOrderCode() + "-" + (repo.countByOrderIdAndKind(o.getId(), PaymentKind.PAYMENT) + 1);
		return record(o, method, ref, o.getTotalAmount(), PaymentKind.PAYMENT, reference, note, actor);
	}

	/** Ghi nhan da hoan tien cho khach. */
	public PaymentTransaction recordRefund(Order o, BigDecimal amount, String method, String reference, String note, String actor) {
		String ref = "RF-" + o.getOrderCode() + "-" + (repo.countByOrderIdAndKind(o.getId(), PaymentKind.REFUND) + 1);
		return record(o, method, ref, amount, PaymentKind.REFUND, reference, note, actor);
	}

	public BigDecimal paid(Long orderId) {
		return repo.sumSuccess(orderId, PaymentKind.PAYMENT);
	}

	public BigDecimal refunded(Long orderId) {
		return repo.sumSuccess(orderId, PaymentKind.REFUND);
	}

	private PaymentTransaction record(Order o, String provider, String ref, BigDecimal amount, PaymentKind kind, String reference,
			String note, String actor) {
		PaymentTransaction t = new PaymentTransaction();
		t.setOrder(o);
		t.setProvider(provider);
		t.setTxnRef(ref);
		t.setAmount(amount);
		t.setKind(kind);
		t.setStatus("SUCCESS");
		t.setTransactionNo(reference);
		t.setNote(note);
		t.setCreatedBy(actor);
		t.setCreatedAt(LocalDateTime.now());
		return repo.save(t);
	}
}
