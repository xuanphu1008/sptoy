package com.toyland.order;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Component;

import com.toyland.common.util.AfterCommit;
import com.toyland.catalog.Product;
import com.toyland.catalog.ProductVariant;
import com.toyland.inventory.InventoryService;
import com.toyland.inventory.MovementType;
import com.toyland.notification.MailService;
import com.toyland.notification.MailService.OrderMail;
import com.toyland.promotion.Voucher;
import com.toyland.promotion.VoucherUsageRepository;

/**
 * Trung tam xu ly chuyen trang thai don hang va cac tac dong kem theo
 * (hoan kho, hoan luot voucher, cap nhat da ban, trang thai thanh toan, gui email).
 * Phai goi trong transaction dang mo.
 */
@Component
public class OrderWorkflow {

	private final InventoryService inventoryService;
	private final VoucherUsageRepository voucherUsageRepository;
	private final MailService mailService;
	private final com.toyland.loyalty.LoyaltyService loyaltyService;
	private final com.toyland.notification.NotificationService notificationService;
	private final com.toyland.payment.PaymentLedgerService ledger;

	public OrderWorkflow(InventoryService inventoryService, VoucherUsageRepository voucherUsageRepository,
			MailService mailService, com.toyland.loyalty.LoyaltyService loyaltyService,
			com.toyland.notification.NotificationService notificationService, com.toyland.payment.PaymentLedgerService ledger) {
		this.inventoryService = inventoryService;
		this.voucherUsageRepository = voucherUsageRepository;
		this.mailService = mailService;
		this.loyaltyService = loyaltyService;
		this.notificationService = notificationService;
		this.ledger = ledger;
	}

	public void transition(Order o, OrderStatus to, String note, String actor) {
		OrderStatus from = o.getStatus();
		if (from == to) {
			return;
		}
		switch (to) {
		case CANCELLED -> onCancel(o, from);
		case DELIVERED -> {
			// tu choi tra hang (RETURN_REQUESTED -> DELIVERED) thi khong tinh da ban lan nua
			if (from != OrderStatus.RETURN_REQUESTED) {
				onDelivered(o, actor);
			}
		}
		case RETURNED -> onReturned(o);
		case COMPLETED -> loyaltyService.earn(o);
		default -> {
		}
		}
		o.setStatus(to);
		o.addHistory(from, to, note, actor);
		// thong bao trong ung dung cho khach (tru khi chinh khach thao tac)
		if (actor == null || !actor.equals(o.getUser().getUsername())) {
			notificationService.notifyUser(o.getUser(), "ORDER_STATUS", "Đơn " + o.getOrderCode() + ": " + to.getLabel(),
					note != null ? note : "Trạng thái đơn hàng vừa được cập nhật", "/account/orders/" + o.getId());
		}
		if (to != OrderStatus.PENDING || from != OrderStatus.PENDING_PAYMENT) {
			OrderMail m = mail(o, note);
			AfterCommit.run(() -> mailService.sendOrderStatusChanged(m)); // gui sau khi commit
		}
	}

	private void onCancel(Order o, OrderStatus from) {
		if (from.isFinal()) {
			return;
		}
		for (OrderItem i : o.getItems()) {
			ProductVariant v = i.getVariant();
			if (v != null) {
				inventoryService.change(v, i.getQuantity(), MovementType.CANCEL, o.getOrderCode(), "Hoàn kho do huỷ đơn");
			}
		}
		voucherUsageRepository.findByOrder(o.getId()).ifPresent(u -> {
			Voucher v = u.getVoucher();
			v.setUsedCount(Math.max(0, v.getUsedCount() - 1));
			voucherUsageRepository.delete(u);
		});
		markRefundPending(o, "đơn bị huỷ");
		loyaltyService.refundRedeemed(o, "Hoàn điểm do huỷ đơn");
	}

	private void onDelivered(Order o, String actor) {
		o.setDeliveredAt(LocalDateTime.now());
		if (o.getPaymentMethod() == PaymentMethod.COD && o.getPaymentStatus() != PaymentStatus.PAID) {
			o.setPaymentStatus(PaymentStatus.PAID);
			o.setPaidAt(LocalDateTime.now());
			// ghi so thu: tien mat shipper thu ho (khach tu bam "da nhan hang" -> ghi la he thong)
			ledger.recordCodCollected(o, actor == null || actor.equals(o.getUser().getUsername()) ? "system" : actor);
		}
		for (OrderItem i : o.getItems()) {
			Product p = i.getProduct();
			if (p != null) {
				p.setSoldCount(p.getSoldCount() + i.getQuantity());
			}
		}
	}

	private void onReturned(Order o) {
		for (OrderItem i : o.getItems()) {
			if (i.getVariant() != null) {
				inventoryService.change(i.getVariant(), i.getQuantity(), MovementType.RETURN, o.getOrderCode(), "Nhập lại hàng trả");
			}
			Product p = i.getProduct();
			if (p != null) {
				p.setSoldCount(Math.max(0, p.getSoldCount() - i.getQuantity()));
			}
		}
		markRefundPending(o, "khách trả hàng");
		loyaltyService.refundRedeemed(o, "Hoàn điểm do trả hàng");
	}

	/**
	 * Da thu tien ma don bi huy / tra hang -> chuyen "Cho hoan tien", bao nhan vien vao man hinh Thanh toan
	 * de chuyen tien va ghi nhan (khong tu dong danh dau Da hoan tien khi tien chua thuc su ve tay khach).
	 */
	public void markRefundPending(Order o, String why) {
		if (o.getPaymentStatus() != PaymentStatus.PAID) {
			return;
		}
		o.setPaymentStatus(PaymentStatus.REFUND_PENDING);
		notificationService.notifyStaff("REFUND_PENDING", "Cần hoàn tiền đơn " + o.getOrderCode(),
				o.getReceiverName() + " · " + com.toyland.notification.NotificationService.vnd(o.getTotalAmount()) + " · " + why,
				"/admin/payments?tab=refunds");
	}

	/** Bao nhan vien co don moi can xac nhan (COD: luc dat; MoMo: khi da thanh toan). */
	public void notifyNewOrder(Order o) {
		int qty = o.getItems().stream().mapToInt(OrderItem::getQuantity).sum();
		notificationService.notifyStaff("ORDER_NEW", "Đơn hàng mới " + o.getOrderCode(),
				o.getReceiverName() + " đặt " + qty + " sản phẩm · "
						+ com.toyland.notification.NotificationService.vnd(o.getTotalAmount())
						+ (o.getPaymentStatus() == PaymentStatus.PAID ? " · đã thanh toán MoMo" : " · COD"),
				"/admin/orders/" + o.getId());
	}

	public void notifyStaff(String type, String title, String message, String link) {
		notificationService.notifyStaff(type, title, message, link);
	}

	/** Dung du lieu email trong transaction (khong truy cap entity o luong gui mail). */
	public OrderMail mail(Order o, String note) {
		List<String> lines = o.getItems().stream().map(i -> i.getProductName()
				+ (i.getVariantName() != null ? " (" + i.getVariantName() + ")" : "") + " × " + i.getQuantity()).toList();
		return new OrderMail(o.getUser().getEmail(), o.getUser().getFullName(), o.getId(), o.getOrderCode(),
				o.getStatus().getLabel(), o.getTotalAmount(), o.getPaymentMethod() == PaymentMethod.COD ? "COD" : "Ví MoMo",
				o.getFullAddress(), lines, note);
	}
}
