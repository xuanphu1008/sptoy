package com.toyland.payment;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.toyland.payment.MomoService.CallbackResult;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "09. Thanh toán MoMo")
@RestController
@RequestMapping("/api/v1/payments/momo")
public class PaymentController {

	private final MomoService momoService;

	public PaymentController(MomoService momoService) {
		this.momoService = momoService;
	}

	@Operation(summary = "Frontend chuyển tiếp toàn bộ tham số MoMo trả về (redirectUrl) để backend kiểm tra chữ ký & cập nhật đơn")
	@GetMapping("/return")
	public CallbackResult momoReturn(@RequestParam Map<String, String> params) {
		return momoService.handleResult(params);
	}

	@Operation(summary = "IPN - MoMo gọi server-to-server (cần địa chỉ công khai: deploy hoặc ngrok). Luôn trả 204 theo yêu cầu MoMo")
	@PostMapping("/ipn")
	public ResponseEntity<Void> ipn(@RequestBody JsonNode body) {
		momoService.handleIpn(body);
		return ResponseEntity.noContent().build();
	}
}
