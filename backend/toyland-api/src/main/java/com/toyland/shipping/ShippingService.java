package com.toyland.shipping;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.toyland.common.exception.ApiException;

/** Phi van chuyen + danh muc dia gioi hanh chinh (lay tu GHN, co cache). */
@Service
public class ShippingService {

	private static final Logger log = LoggerFactory.getLogger(ShippingService.class);

	private final GhnClient ghn;
	private final Map<String, List<Option>> cache = new ConcurrentHashMap<>();

	@Value("${app.shipping.fixed-fee}")
	private BigDecimal fixedFee;

	@Value("${app.shipping.free-threshold}")
	private BigDecimal freeThreshold;

	@Value("${app.order.gift-wrap-fee}")
	private BigDecimal giftWrapFee;

	public ShippingService(GhnClient ghn) {
		this.ghn = ghn;
	}

	public record Option(String id, String name) {
	}

	public record ShippingConfig(boolean ghnEnabled, BigDecimal freeShipThreshold, BigDecimal fixedFee, BigDecimal giftWrapFee) {
	}

	/** Kien hang tinh phi. */
	public record Parcel(int weightGram, int lengthCm, int widthCm, int heightCm, BigDecimal value) {
	}

	public record FeeResult(BigDecimal fee, String provider, String message) {
	}

	public ShippingConfig config() {
		return new ShippingConfig(ghn.isEnabled(), freeThreshold, fixedFee, giftWrapFee);
	}

	public BigDecimal freeThreshold() {
		return freeThreshold;
	}

	public BigDecimal giftWrapFee() {
		return giftWrapFee;
	}

	public List<Option> provinces() {
		requireGhn();
		return cached("p", () -> parse(call(ghn::provinces), "ProvinceID", "ProvinceName"));
	}

	public List<Option> districts(int provinceId) {
		requireGhn();
		return cached("d" + provinceId, () -> parse(call(() -> ghn.districts(provinceId)), "DistrictID", "DistrictName"));
	}

	public List<Option> wards(int districtId) {
		requireGhn();
		return cached("w" + districtId, () -> parse(call(() -> ghn.wards(districtId)), "WardCode", "WardName"));
	}

	/** Chi cache danh sach khong rong (GHN loi tam thoi tra ve rong thi lan sau goi lai). */
	private List<Option> cached(String key, java.util.function.Supplier<List<Option>> loader) {
		List<Option> hit = cache.get(key);
		if (hit != null) {
			return hit;
		}
		List<Option> list = loader.get();
		if (!list.isEmpty()) {
			cache.put(key, list);
		}
		return list;
	}

	/**
	 * Tinh phi ship. Neu GHN chua cau hinh / dia chi thieu ma GHN / GHN loi -> dung phi co dinh (van dat hang duoc).
	 */
	public FeeResult fee(Integer districtId, String wardCode, Parcel parcel) {
		if (!ghn.isEnabled()) {
			return new FeeResult(fixedFee, "FIXED", "Phí vận chuyển cố định (chưa kết nối GHN)");
		}
		if (districtId == null || wardCode == null || wardCode.isBlank()) {
			return new FeeResult(fixedFee, "FIXED", "Địa chỉ chưa có mã GHN, áp dụng phí cố định");
		}
		try {
			JsonNode res = ghn.fee(districtId, wardCode, parcel.weightGram(), parcel.lengthCm(), parcel.widthCm(),
					parcel.heightCm(), parcel.value().longValue());
			JsonNode total = res == null ? null : res.path("data").path("total");
			if (total == null || total.isMissingNode() || total.isNull()) {
				throw new IllegalStateException(res == null ? "empty response" : res.path("message").asText());
			}
			return new FeeResult(BigDecimal.valueOf(total.asLong()), "GHN", "Giao Hàng Nhanh - giao hàng tiêu chuẩn");
		} catch (Exception e) {
			log.warn("GHN tinh phi loi: {}", e.getMessage());
			return new FeeResult(fixedFee, "FIXED", "Không lấy được phí GHN, áp dụng phí cố định");
		}
	}

	private void requireGhn() {
		if (!ghn.isEnabled()) {
			throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "GHN_DISABLED", "Chưa cấu hình GHN - vui lòng nhập địa chỉ thủ công");
		}
	}

	private JsonNode call(java.util.function.Supplier<JsonNode> s) {
		try {
			return s.get();
		} catch (Exception e) {
			log.warn("GHN loi: {}", e.getMessage());
			throw new ApiException(HttpStatus.BAD_GATEWAY, "GHN_ERROR", "Không kết nối được Giao Hàng Nhanh, vui lòng thử lại");
		}
	}

	private static List<Option> parse(JsonNode res, String idField, String nameField) {
		List<Option> out = new ArrayList<>();
		for (JsonNode n : res.path("data")) {
			String name = n.path(nameField).asText("");
			// GHN moi truong dev co mot so ban ghi test -> bo qua
			if (name.isBlank() || name.toLowerCase().contains("test")) {
				continue;
			}
			out.add(new Option(n.path(idField).asText(), name));
		}
		out.sort(Comparator.comparing(Option::name));
		return out;
	}
}
