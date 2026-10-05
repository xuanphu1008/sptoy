package com.toyland.shipping;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.toyland.common.util.TextUtils;

/**
 * Goi REST API cua Giao Hang Nhanh (GHN) - vi du ve tich hop dich vu ben thu 3 (SOA).
 * Tai lieu: https://api.ghn.vn/home/docs
 */
@Component
public class GhnClient {

	private static final Logger log = LoggerFactory.getLogger(GhnClient.class);

	private final RestClient client;
	private final boolean enabled;
	private final int serviceTypeId;

	public GhnClient(@Value("${app.ghn.base-url}") String baseUrl, @Value("${app.ghn.token:}") String token,
			@Value("${app.ghn.shop-id:}") String shopId, @Value("${app.ghn.service-type-id:2}") int serviceTypeId) {
		this.enabled = !TextUtils.isBlank(token) && !TextUtils.isBlank(shopId);
		this.serviceTypeId = serviceTypeId;
		// timeout de checkout khong bi treo khi GHN cham (se dung phi co dinh)
		SimpleClientHttpRequestFactory rf = new SimpleClientHttpRequestFactory();
		rf.setConnectTimeout(3000);
		rf.setReadTimeout(5000);
		RestClient.Builder b = RestClient.builder().requestFactory(rf).baseUrl(baseUrl)
				.defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
		if (enabled) {
			b.defaultHeader("Token", token.trim()).defaultHeader("ShopId", shopId.trim());
		}
		this.client = b.build();
		log.info("GHN: {}", enabled ? "da cau hinh (" + baseUrl + ")" : "CHUA cau hinh -> dung phi ship co dinh");
	}

	public boolean isEnabled() {
		return enabled;
	}

	public JsonNode provinces() {
		return client.get().uri("/master-data/province").retrieve().body(JsonNode.class);
	}

	public JsonNode districts(int provinceId) {
		return client.post().uri("/master-data/district").body(Map.of("province_id", provinceId)).retrieve().body(JsonNode.class);
	}

	public JsonNode wards(int districtId) {
		return client.post().uri("/master-data/ward?district_id=" + districtId).body(Map.of("district_id", districtId))
				.retrieve().body(JsonNode.class);
	}

	/** Tinh phi van chuyen: POST /v2/shipping-order/fee */
	public JsonNode fee(int toDistrictId, String toWardCode, int weightGram, int length, int width, int height, long insuranceValue) {
		Map<String, Object> body = Map.of(
				"service_type_id", serviceTypeId,
				"to_district_id", toDistrictId,
				"to_ward_code", toWardCode,
				"weight", Math.max(weightGram, 1),
				"length", Math.min(Math.max(length, 1), 200),
				"width", Math.min(Math.max(width, 1), 200),
				"height", Math.min(Math.max(height, 1), 200),
				"insurance_value", Math.min(insuranceValue, 5_000_000L));
		return client.post().uri("/v2/shipping-order/fee").body(body).retrieve().body(JsonNode.class);
	}
}
