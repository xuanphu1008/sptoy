package com.toyland.notification;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

/**
 * Gui email HTML (bat dong bo).
 * - Phat trien: Mailtrap (app.mail.enabled=true + tai khoan Mailtrap) -> thu nam trong hop thu test.
 * - app.mail.enabled=false: chi ghi log noi dung, khong gui.
 */
@Service
public class MailService {

	private static final Logger log = LoggerFactory.getLogger(MailService.class);

	private final ObjectProvider<JavaMailSender> mailSenderProvider;

	@Value("${app.mail.enabled}")
	private boolean enabled;

	@Value("${app.mail.from}")
	private String from;

	@Value("${app.frontend-url}")
	private String frontendUrl;

	public MailService(ObjectProvider<JavaMailSender> mailSenderProvider) {
		this.mailSenderProvider = mailSenderProvider;
	}

	/** Du lieu toi thieu cua don hang de dung email (tranh truy cap entity ngoai transaction). */
	public record OrderMail(String email, String customerName, Long orderId, String orderCode, String statusLabel,
			BigDecimal total, String paymentMethod, String address, List<String> lines, String note) {
	}

	/** Thu da dung xong (nguoi nhan, tieu de, HTML day du): di qua outbox sang dich vu thong bao hoac gui noi bo. */
	public record Email(String to, String subject, String html) {
	}

	public Email renderOrderPlaced(OrderMail m) {
		StringBuilder rows = new StringBuilder();
		m.lines().forEach(l -> rows.append("<li>").append(escape(l)).append("</li>"));
		String body = "<p>Xin chào <b>" + escape(m.customerName()) + "</b>,</p>"
				+ "<p>Cảm ơn bạn đã đặt hàng tại ToyLand. Đơn hàng <b>" + m.orderCode() + "</b> đã được ghi nhận.</p>"
				+ "<ul>" + rows + "</ul>"
				+ "<p>Tổng thanh toán: <b>" + vnd(m.total()) + "</b> (" + m.paymentMethod() + ")<br/>Giao đến: " + escape(m.address()) + "</p>"
				+ button("Xem đơn hàng", frontendUrl + "/account/orders/" + m.orderId());
		return new Email(m.email(), "[ToyLand] Xác nhận đơn hàng " + m.orderCode(), wrap(body));
	}

	public Email renderOrderStatusChanged(OrderMail m) {
		String body = "<p>Xin chào <b>" + escape(m.customerName()) + "</b>,</p>"
				+ "<p>Đơn hàng <b>" + m.orderCode() + "</b> vừa chuyển sang trạng thái: <b style='color:#e53935'>"
				+ escape(m.statusLabel()) + "</b>.</p>"
				+ (m.note() != null ? "<p>Ghi chú: " + escape(m.note()) + "</p>" : "")
				+ button("Theo dõi đơn hàng", frontendUrl + "/account/orders/" + m.orderId());
		return new Email(m.email(), "[ToyLand] Đơn " + m.orderCode() + ": " + m.statusLabel(), wrap(body));
	}

	@Async
	public void sendPasswordReset(String email, String name, String link) {
		if (!enabled) {
			log.info("[MAIL tắt] Link đặt lại mật khẩu cho {}: {}", email, link);
		}
		String body = "<p>Xin chào <b>" + escape(name) + "</b>,</p>"
				+ "<p>Bạn vừa yêu cầu đặt lại mật khẩu. Liên kết có hiệu lực trong <b>15 phút</b>.</p>"
				+ button("Đặt lại mật khẩu", link)
				+ "<p style='color:#888'>Nếu không phải bạn, hãy bỏ qua email này.</p>";
		send(email, "[ToyLand] Đặt lại mật khẩu", body);
	}

	@Async
	public void sendEmailVerification(String email, String name, String link, int hours) {
		if (!enabled) {
			// Chua cau hinh SMTP: in link ra log de van demo duoc (copy link dan vao trinh duyet)
			log.info("[MAIL tắt] Link xác nhận email cho {}: {}", email, link);
		}
		String body = "<p>Xin chào <b>" + escape(name) + "</b>,</p>"
				+ "<p>Cảm ơn bạn đã đăng ký tài khoản ToyLand. Vui lòng bấm nút bên dưới để xác nhận địa chỉ email <b>"
				+ escape(email) + "</b>. Liên kết có hiệu lực trong <b>" + hours + " giờ</b>.</p>"
				+ button("Xác nhận email", link)
				+ "<p style='color:#888'>Nếu nút không hoạt động, sao chép liên kết sau vào trình duyệt:<br/>" + escape(link) + "</p>"
				+ "<p style='color:#888'>Nếu bạn không tạo tài khoản này, hãy bỏ qua email.</p>";
		send(email, "[ToyLand] Xác nhận địa chỉ email", body);
	}

	private void send(String to, String subject, String htmlBody) {
		deliver(new Email(to, subject, wrap(htmlBody)));
	}

	private String wrap(String htmlBody) {
		return "<div style='font-family:Arial,sans-serif;max-width:560px;margin:auto;border:1px solid #eee;border-radius:12px;overflow:hidden'>"
				+ "<div style='background:#e53935;color:#fff;padding:16px 24px;font-size:20px;font-weight:bold'>🧸 ToyLand</div>"
				+ "<div style='padding:16px 24px;color:#333;line-height:1.6'>" + htmlBody + "</div>"
				+ "<div style='background:#fafafa;padding:12px 24px;color:#999;font-size:12px'>Email tự động từ hệ thống ToyLand – vui lòng không trả lời.</div></div>";
	}

	/** Gui ngay mot thu da dung xong bang SMTP cua chinh he thong (dong bo; goi tu luong nen). */
	public void deliver(Email mail) {
		String to = mail.to();
		String subject = mail.subject();
		String html = mail.html();
		if (!enabled) {
			log.info("[MAIL tắt] To: {} | Subject: {}", to, subject);
			return;
		}
		JavaMailSender sender = mailSenderProvider.getIfAvailable();
		if (sender == null) {
			log.warn("Chua cau hinh JavaMailSender, bo qua email toi {}", to);
			return;
		}
		try {
			MimeMessage msg = sender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
			helper.setFrom(from);
			helper.setTo(to);
			helper.setSubject(subject);
			helper.setText(html, true);
			sender.send(msg);
			log.info("Da gui email '{}' toi {}", subject, to);
		} catch (Exception e) {
			log.error("Gui email that bai toi {}: {}", to, e.getMessage());
		}
	}

	private String button(String text, String url) {
		return "<p><a href='" + url + "' style='display:inline-block;background:#e53935;color:#fff;padding:10px 20px;"
				+ "border-radius:24px;text-decoration:none;font-weight:bold'>" + text + "</a></p>";
	}

	private static String vnd(BigDecimal v) {
		return NumberFormat.getInstance(Locale.forLanguageTag("vi-VN")).format(v) + " ₫";
	}

	private static String escape(String s) {
		return s == null ? "" : s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
	}
}
