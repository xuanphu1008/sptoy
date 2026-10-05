package com.toyland.shipping;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.toyland.shipping.ShippingService.Option;
import com.toyland.shipping.ShippingService.ShippingConfig;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "05. Vận chuyển (GHN)")
@RestController
@RequestMapping("/api/v1/shipping")
public class ShippingController {

	private final ShippingService service;

	public ShippingController(ShippingService service) {
		this.service = service;
	}

	@GetMapping("/config")
	public ShippingConfig config() {
		return service.config();
	}

	@GetMapping("/provinces")
	public List<Option> provinces() {
		return service.provinces();
	}

	@GetMapping("/districts")
	public List<Option> districts(@RequestParam int provinceId) {
		return service.districts(provinceId);
	}

	@GetMapping("/wards")
	public List<Option> wards(@RequestParam int districtId) {
		return service.wards(districtId);
	}
}
