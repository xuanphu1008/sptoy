package com.toyland.payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import com.fasterxml.jackson.databind.JsonNode;
import com.toyland.common.exception.ApiException;
import com.toyland.common.util.TextUtils;
import com.toyland.order.Order;
import com.toyland.order.OrderStatus;
import com.toyland.order.OrderWorkflow;
import com.toyland.order.PaymentStatus;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;

/**
 * Tich hop cong thanh toan vi MoMo (moi truong thu nghiem test-payment.momo.vn) - vi du tich hop dich vu ben thu 3 (SOA).
 * <ol>
 * <li>Backend goi API tao giao dich cua MoMo (ky HMAC-SHA256) -> nhan payUrl.</li>
 * <li>Khach thanh toan tren trang MoMo (quet QR bang app MoMo thu nghiem hoac the ATM thu nghiem).</li>
 * <li>MoMo chuyen trinh duyet ve redirectUrl (frontend) -> frontend gui lai toan bo tham so cho
 * {@code GET /payments/momo/return} de backend kiem tra chu ky va cap nhat don.</li>
 * <li>Dong thoi MoMo goi IPN (server-to-server) {@code POST /payments/momo/ipn} - chi toi duoc khi backend co dia chi cong khai
 * (deploy that hoac ngrok); chay localhost thi buoc 3 dam nhiem.</li>
 * </ol>
 * Hai duong (redirect + IPN) cung di qua {@link #handleResult(Map)} va idempotent: goi nhieu lan khong sai lech.
 * Khoa mac dinh la bo khoa thu nghiem cong khai trong ma mau cua MoMo; khi co tai khoan MoMo Business thi
 * ghi de bang bien moi truong (APP_MOMO_PARTNER_CODE, APP_MOMO_ACCESS_KEY, APP_MOMO_SECRET_KEY).
 */
@Service
public class MomoService {

	private static final Logger log = LoggerFactory.getLogger(MomoService.class);
	private static final ZoneId VN = ZoneId.of("Asia/Ho_Chi_Minh");
	/** Gioi han so tien cua MoMo cho 1 giao dich thanh toan vi. */
	static final long MIN_AMOUNT = 1_000, MAX_AMOUNT = 50_000_000;

	private final PaymentTransactionRepository txnRepository;
	private final OrderWorkflow workflow;
	private final RestClient client;

	@PersistenceContext
	private EntityManager em;

	private final boolean enabled;
	private final String partnerCode;
	private final String accessKey;
	private final String secretKey;
	private final String requestType;
	private final String redirectUrl;
	private final String ipnUrl;

	public MomoService(PaymentTransactionRepository txnRepository, OrderWorkflow workflow,
			@Value("${app.momo.enabled:true}") boolean enabled,
			@Value("${app.momo.endpoint:https://test-payment.momo.vn}") String endpoint,
			@Value("${app.momo.partner-code:MOMO}") String partnerCode,
			@Value("${app.momo.access-key:F8BBA842ECF85}") String accessKey,
			@Value("${app.momo.secret-key:K951B6PE1waDMi640xX08PD3vg6EkVlz}") String secretKey,
			@Value("${app.momo.request-type:payWithMethod}") String requestType,
			@Value("${app.momo.redirect-url:${app.frontend-url:http://localhost:4200}/payment/momo-return}") String redirectUrl,
			@Value("${app.momo.ipn-url:${app.public-base-url:http://localhost:8080}/api/v1/payments/momo/ipn}") String ipnUrl) {
		this.txnRepository = txnRepository;
		this.workflow = workflow;
		this.partnerCode = partnerCode.trim();
		this.accessKey = accessKey.trim();
		this.secretKey = secretKey.trim();
		this.requestType = requestType.trim();
		this.redirectUrl = redirectUrl.trim();
		this.ipnUrl = ipnUrl.trim();
		this.enabled = enabled && !TextUtils.isBlank(this.partnerCode) && !TextUtils.isBlank(this.accessKey)
				&& !TextUtils.isBlank(this.secretKey);
		// MoMo yeu cau cho phan hoi toi thieu 30 giay
		SimpleClientHttpRequestFactory rf = new SimpleClientHttpRequestFactory();
		rf.setConnectTimeout(10_000);
		rf.setReadTimeout(30_000);
		this.client = RestClient.builder().requestFactory(rf).baseUrl(endpoint.trim())
				.defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE).build();
		log.info("MoMo: {}", this.enabled ? "bat (" + endpoint.trim() + ", partner " + this.partnerCode + ", " + this.requestType + ")"
				: "TAT -> chi nhan COD");
	}

	public boolean isEnabled() {
		return enabled;
	}

	public record CallbackResult(boolean validSignature, boolean success, Long orderId, String orderCode, String message,
			String resultCode) {
	}

	/** Tao giao dich MoMo cho don dang cho thanh toan, tra ve payUrl de chuyen trinh duyet. Goi trong transaction dang mo. */
	public String createPayment(Order order) {
		if (!enabled) {
			throw ApiException.badRequest("MOMO_DISABLED", "Thanh toán MoMo đang tắt, vui lòng chọn COD");
		}
		long amount = order.getTotalAmount().longValue();
		if (amount < MIN_AMOUNT || amount > MAX_AMOUNT) {
			throw ApiException.badRequest("MOMO_AMOUNT", "MoMo chỉ nhận giao dịch từ 1.000đ đến 50.000.000đ, vui lòng chọn COD");
		}
		// them mili-giay: bam "Thanh toan" 2 lan trong cung 1 giay khong bi trung ma giao dich
		String momoOrderId = order.getOrderCode() + "-" + LocalDateTime.now(VN).format(DateTimeFormatter.ofPattern("HHmmssSSS"));
		PaymentTransaction t = new PaymentTransaction();
		t.setOrder(order);
		t.setProvider("MOMO");
		t.setTxnRef(momoOrderId);
		t.setAmount(order.getTotalAmount());
		t.setStatus("PENDING");
		txnRepository.save(t);

		Map<String, String> sign = new HashMap<>();
		sign.put("accessKey", accessKey);
		sign.put("amount", String.valueOf(amount));
		sign.put("extraData", "");
		sign.put("ipnUrl", ipnUrl);
		sign.put("orderId", momoOrderId);
		sign.put("orderInfo", "Thanh toan don hang " + order.getOrderCode());
		sign.put("partnerCode", partnerCode);
		sign.put("redirectUrl", redirectUrl);
		sign.put("requestId", momoOrderId);
		sign.put("requestType", requestType);
		String signature = MomoUtil.hmacSHA256(secretKey, MomoUtil.rawSignature(MomoUtil.CREATE_FIELDS, sign));

		Map<String, Object> body = new LinkedHashMap<>();
		body.put("partnerCode", partnerCode);
		body.put("partnerName", "ToyLand");
		body.put("storeId", "ToyLand");
		body.put("requestId", momoOrderId);
		body.put("amount", amount);
		body.put("orderId", momoOrderId);
		body.put("orderInfo", sign.get("orderInfo"));
		body.put("redirectUrl", redirectUrl);
		body.put("ipnUrl", ipnUrl);
		body.put("lang", "vi");
		body.put("requestType", requestType);
		body.put("autoCapture", true);
		body.put("extraData", "");
		body.put("signature", signature);

		JsonNode res = post("/v2/gateway/api/create", body);
		int resultCode = res.path("resultCode").asInt(-1);
		String payUrl = res.path("payUrl").asText("");
		if (resultCode != 0 || payUrl.isBlank()) {
			log.warn("MoMo tu choi tao giao dich {}: {} {}", momoOrderId, resultCode, res.path("message").asText());
			throw new ApiException(HttpStatus.BAD_GATEWAY, "MOMO_ERROR",
					"MoMo từ chối tạo giao dịch: " + res.path("message").asText("lỗi không xác định") + " (mã " + resultCode + ")");
		}
		return payUrl;
	}

	/** Xu ly ket qua MoMo tra ve (redirect hoac IPN). Idempotent. */
	@Transactional
	public CallbackResult handleResult(Map<String, String> params) {
		Map<String, String> v = new HashMap<>(params);
		v.put("accessKey", accessKey);
		String received = params.get("signature");
		if (!enabled || received == null
				|| !MomoUtil.sameSignature(received, MomoUtil.hmacSHA256(secretKey, MomoUtil.rawSignature(MomoUtil.RESULT_FIELDS, v)))) {
			return new CallbackResult(false, false, null, null, "Chữ ký không hợp lệ", "SIGNATURE");
		}
		if (!partnerCode.equals(params.get("partnerCode"))) {
			return new CallbackResult(true, false, null, null, "Sai mã đối tác", "PARTNER");
		}
		PaymentTransaction t = txnRepository.findByTxnRef(params.get("orderId")).orElse(null);
		if (t == null) {
			return new CallbackResult(true, false, null, null, "Không tìm thấy giao dịch", "NOT_FOUND");
		}
		Order order = t.getOrder();
		// khoa dong don hang (SELECT ... FOR UPDATE) + nap lai trang thai moi nhat: tranh xung dot voi job tu huy don qua han
		em.refresh(order, LockModeType.PESSIMISTIC_WRITE);
		long amount;
		try {
			amount = Long.parseLong(params.getOrDefault("amount", "0"));
		} catch (NumberFormatException e) {
			amount = -1;
		}
		if (t.getAmount().longValue() != amount) {
			return new CallbackResult(true, false, order.getId(), order.getOrderCode(), "Số tiền không khớp", "AMOUNT");
		}
		String resultCode = params.getOrDefault("resultCode", "");
		if ("SUCCESS".equals(t.getStatus())) {
			return new CallbackResult(true, true, order.getId(), order.getOrderCode(), "Giao dịch đã được xác nhận trước đó", resultCode);
		}
		t.setResponseCode(TextUtils.isBlank(resultCode) ? null : resultCode);
		t.setBankCode(TextUtils.trimToNull(params.get("payType")));
		t.setTransactionNo(TextUtils.trimToNull(params.get("transId")));
		t.setRawData(params.toString());
		if ("0".equals(resultCode)) {
			t.setStatus("SUCCESS");
			t.setNote("Thanh toán qua ví MoMo");
			order.setPaymentStatus(PaymentStatus.PAID);
			order.setPaidAt(LocalDateTime.now());
			if (order.getStatus() == OrderStatus.PENDING_PAYMENT) {
				workflow.transition(order, OrderStatus.PENDING, "Thanh toán MoMo thành công (mã GD " + t.getTransactionNo() + ")", "momo");
				workflow.notifyNewOrder(order);
			} else if (order.getStatus() == OrderStatus.CANCELLED) {
				String note = "Khách đã thanh toán MoMo sau khi đơn bị huỷ - cần hoàn tiền";
				order.setInternalNote(order.getInternalNote() == null || order.getInternalNote().isBlank() ? note
						: order.getInternalNote() + "\n" + note);
				workflow.markRefundPending(order, "khách thanh toán MoMo sau khi đơn đã bị huỷ");
			}
			log.info("MoMo thanh cong: don {} giao dich {}", order.getOrderCode(), t.getTxnRef());
			return new CallbackResult(true, true, order.getId(), order.getOrderCode(), "Thanh toán thành công", resultCode);
		}
		if ("9000".equals(resultCode) || "7000".equals(resultCode) || "7002".equals(resultCode)) {
			// giao dich dang xu ly: cho IPN / lan kiem tra sau, khong danh dau that bai
			return new CallbackResult(true, false, order.getId(), order.getOrderCode(),
					"Giao dịch đang được MoMo xử lý, vui lòng kiểm tra lại sau ít phút", resultCode);
		}
		t.setStatus("FAILED");
		t.setNote("MoMo báo lỗi mã " + resultCode + (TextUtils.isBlank(params.get("message")) ? "" : ": " + params.get("message")));
		if (order.getPaymentStatus() != PaymentStatus.PAID) {
			order.setPaymentStatus(PaymentStatus.FAILED);
		}
		String why = "1006".equals(resultCode) ? "bạn đã huỷ giao dịch trên MoMo" : "mã " + resultCode;
		return new CallbackResult(true, false, order.getId(), order.getOrderCode(), "Thanh toán không thành công (" + why + ")",
				resultCode);
	}

	/** IPN cua MoMo gui JSON (so la kieu number) -> doi ve Map chuoi de dung chung voi luong redirect. */
	@Transactional // goi noi bo handleResult khong qua proxy -> transaction phai mo tu day
	public CallbackResult handleIpn(JsonNode body) {
		Map<String, String> m = new HashMap<>();
		if (body != null) {
			body.properties().forEach(e -> m.put(e.getKey(), e.getValue().isNull() ? "" : e.getValue().asText()));
		}
		return handleResult(m);
	}

	/**
	 * Hoan tien qua API hoan tien cua MoMo cho 1 giao dich da thanh toan thanh cong.
	 * @return ma giao dich hoan tien do MoMo cap
	 */
	public String refund(PaymentTransaction paid, BigDecimal amount, String description) {
		if (!enabled) {
			throw ApiException.badRequest("MOMO_DISABLED", "Thanh toán MoMo đang tắt, không hoàn tiền tự động được");
		}
		if (TextUtils.isBlank(paid.getTransactionNo())) {
			throw ApiException.badRequest("MOMO_NO_TRANS", "Không có mã giao dịch MoMo của lần thanh toán để hoàn tiền");
		}
		long transId;
		try {
			transId = Long.parseLong(paid.getTransactionNo().trim());
		} catch (NumberFormatException e) {
			throw ApiException.badRequest("MOMO_NO_TRANS", "Mã giao dịch MoMo không hợp lệ: " + paid.getTransactionNo());
		}
		String refundId = "RF" + paid.getOrder().getOrderCode() + "-" + LocalDateTime.now(VN).format(DateTimeFormatter.ofPattern("HHmmssSSS"));
		String desc = TextUtils.isBlank(description) ? "Hoan tien don " + paid.getOrder().getOrderCode() : description.trim();
		Map<String, String> sign = new HashMap<>();
		sign.put("accessKey", accessKey);
		sign.put("amount", String.valueOf(amount.longValue()));
		sign.put("description", desc);
		sign.put("orderId", refundId);
		sign.put("partnerCode", partnerCode);
		sign.put("requestId", refundId);
		sign.put("transId", String.valueOf(transId));

		Map<String, Object> body = new LinkedHashMap<>();
		body.put("partnerCode", partnerCode);
		body.put("orderId", refundId);
		body.put("requestId", refundId);
		body.put("amount", amount.longValue());
		body.put("transId", transId);
		body.put("lang", "vi");
		body.put("description", desc);
		body.put("signature", MomoUtil.hmacSHA256(secretKey, MomoUtil.rawSignature(MomoUtil.REFUND_FIELDS, sign)));

		JsonNode res = post("/v2/gateway/api/refund", body);
		int resultCode = res.path("resultCode").asInt(-1);
		if (resultCode != 0) {
			throw new ApiException(HttpStatus.BAD_GATEWAY, "MOMO_REFUND_ERROR",
					"MoMo từ chối hoàn tiền: " + res.path("message").asText("lỗi không xác định") + " (mã " + resultCode
							+ "). Có thể chọn hoàn bằng chuyển khoản.");
		}
		log.info("MoMo hoan tien thanh cong: don {} so tien {} ma {}", paid.getOrder().getOrderCode(), amount, res.path("transId").asText());
		return res.path("transId").asText(refundId);
	}

	private JsonNode post(String path, Map<String, Object> body) {
		try {
			JsonNode res = client.post().uri(path).body(body).retrieve().body(JsonNode.class);
			if (res == null) {
				throw new ApiException(HttpStatus.BAD_GATEWAY, "MOMO_ERROR", "MoMo không phản hồi");
			}
			return res;
		} catch (RestClientResponseException e) {
			// MoMo tra loi nghiep vu kem ma HTTP 4xx/5xx nhung van co body JSON (resultCode, message)
			JsonNode res = null;
			try {
				res = e.getResponseBodyAs(JsonNode.class);
			} catch (RuntimeException ignore) {
				// body khong phai JSON
			}
			if (res != null && res.has("resultCode")) {
				return res;
			}
			log.warn("Goi MoMo {} loi HTTP {}: {}", path, e.getStatusCode(), e.getResponseBodyAsString());
			throw new ApiException(HttpStatus.BAD_GATEWAY, "MOMO_ERROR", "Cổng MoMo báo lỗi HTTP " + e.getStatusCode().value());
		} catch (RestClientException e) {
			log.warn("Goi MoMo {} loi: {}", path, e.getMessage());
			throw new ApiException(HttpStatus.BAD_GATEWAY, "MOMO_UNREACHABLE",
					"Không kết nối được cổng MoMo, vui lòng thử lại sau hoặc chọn COD");
		}
	}
}
