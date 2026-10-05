package com.toyland.promotion;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.toyland.audit.AuditService;
import com.toyland.common.exception.ApiException;
import com.toyland.common.util.Money;
import com.toyland.common.util.TextUtils;
import com.toyland.promotion.dto.PromotionDtos.VoucherDto;
import com.toyland.promotion.dto.PromotionDtos.VoucherRequest;

@Service
@Transactional
public class VoucherService {

	private final VoucherRepository repo;
	private final VoucherUsageRepository usageRepository;
	private final AuditService audit;

	public VoucherService(VoucherRepository repo, VoucherUsageRepository usageRepository, AuditService audit) {
		this.repo = repo;
		this.usageRepository = usageRepository;
		this.audit = audit;
	}

	/** Ket qua ap ma: so tien giam tren hang + so tien giam phi ship. */
	public record VoucherResult(Voucher voucher, BigDecimal discount, BigDecimal shippingDiscount) {
	}

	/**
	 * Kiem tra va tinh gia tri ma giam gia.
	 * @throws ApiException neu ma khong hop le (thong bao ro ly do)
	 */
	// noRollbackFor: loi voucher (het han, chua du dieu kien) duoc checkout bat lai -> khong danh dau rollback-only
	@Transactional(readOnly = true, noRollbackFor = ApiException.class)
	public VoucherResult evaluate(String code, Long userId, BigDecimal subtotal, BigDecimal shippingFee) {
		Voucher v = repo.findByCodeIgnoreCase(code.trim())
				.orElseThrow(() -> ApiException.badRequest("VOUCHER_INVALID", "Mã giảm giá không tồn tại"));
		LocalDateTime now = LocalDateTime.now();
		if (!v.isActive()) {
			throw ApiException.badRequest("VOUCHER_INVALID", "Mã giảm giá đã ngừng áp dụng");
		}
		if (now.isBefore(v.getStartAt())) {
			throw ApiException.badRequest("VOUCHER_INVALID", "Mã giảm giá chưa đến thời gian sử dụng");
		}
		if (now.isAfter(v.getEndAt())) {
			throw ApiException.badRequest("VOUCHER_EXPIRED", "Mã giảm giá đã hết hạn");
		}
		if (v.getUsageLimit() != null && v.getUsedCount() >= v.getUsageLimit()) {
			throw ApiException.badRequest("VOUCHER_OUT", "Mã giảm giá đã hết lượt sử dụng");
		}
		if (userId != null && usageRepository.countByVoucherAndUser(v.getId(), userId) >= v.getPerUserLimit()) {
			throw ApiException.badRequest("VOUCHER_USED", "Bạn đã dùng hết lượt của mã này");
		}
		if (subtotal.compareTo(v.getMinOrderValue()) < 0) {
			throw ApiException.badRequest("VOUCHER_MIN_ORDER", "Đơn hàng cần tối thiểu " + vnd(v.getMinOrderValue())
					+ " để dùng mã này (còn thiếu " + vnd(v.getMinOrderValue().subtract(subtotal)) + ")");
		}
		BigDecimal discount = BigDecimal.ZERO;
		BigDecimal shipDiscount = BigDecimal.ZERO;
		switch (v.getType()) {
		case PERCENT -> {
			discount = Money.percentOf(subtotal, v.getValue().intValue());
			if (v.getMaxDiscount() != null) {
				discount = Money.min(discount, v.getMaxDiscount());
			}
		}
		case FIXED -> discount = Money.min(v.getValue(), subtotal);
		case FREESHIP -> {
			shipDiscount = shippingFee;
			if (v.getMaxDiscount() != null) {
				shipDiscount = Money.min(shipDiscount, v.getMaxDiscount());
			}
		}
		}
		return new VoucherResult(v, discount, shipDiscount);
	}

	/** Ghi nhan 1 luot dung voucher (cap nhat co dieu kien, chong vuot usage_limit khi dat dong thoi). */
	public void consume(Voucher v) {
		if (repo.incrementUsage(v.getId()) == 0) {
			throw ApiException.conflict("Mã " + v.getCode() + " vừa hết lượt sử dụng, vui lòng chọn mã khác");
		}
	}

	@Transactional(readOnly = true)
	public List<VoucherDto> available() {
		return repo.findAvailable(LocalDateTime.now()).stream().map(VoucherService::toDto).toList();
	}

	@Transactional(readOnly = true)
	public List<VoucherDto> all() {
		return repo.findAllByOrderByIdDesc().stream().map(VoucherService::toDto).toList();
	}

	public VoucherDto save(Long id, VoucherRequest r) {
		if (!r.endAt().isAfter(r.startAt())) {
			throw ApiException.badRequest("Thời gian kết thúc phải sau thời gian bắt đầu");
		}
		if (r.type() == VoucherType.PERCENT && (r.value().compareTo(BigDecimal.ONE) < 0 || r.value().compareTo(BigDecimal.valueOf(100)) > 0)) {
			throw ApiException.badRequest("Giảm theo % phải từ 1 đến 100");
		}
		String code = r.code().trim().toUpperCase();
		if (id == null ? repo.existsByCodeIgnoreCase(code) : repo.existsByCodeIgnoreCaseAndIdNot(code, id)) {
			throw ApiException.conflict("Mã voucher đã tồn tại");
		}
		Voucher v = id == null ? new Voucher() : repo.findById(id).orElseThrow(() -> ApiException.notFound("voucher", id));
		v.setCode(code);
		v.setName(r.name().trim());
		v.setDescription(TextUtils.trimToNull(r.description()));
		v.setType(r.type());
		v.setValue(r.value());
		v.setMaxDiscount(r.maxDiscount());
		v.setMinOrderValue(r.minOrderValue());
		v.setStartAt(r.startAt());
		v.setEndAt(r.endAt());
		v.setUsageLimit(r.usageLimit());
		v.setPerUserLimit(Math.max(1, r.perUserLimit()));
		v.setActive(r.active());
		v = repo.save(v);
		audit.log(id == null ? "CREATE" : "UPDATE", "VOUCHER", v.getCode(), (id == null ? "Tạo" : "Sửa") + " mã giảm giá " + v.getCode()
				+ " (" + v.getName() + ")");
		return toDto(v);
	}

	public void delete(Long id) {
		Voucher v = repo.findById(id).orElseThrow(() -> ApiException.notFound("voucher", id));
		if (v.getUsedCount() > 0) {
			v.setActive(false); // da duoc dung -> chi tat, giu lich su
			audit.log("DISABLE", "VOUCHER", v.getCode(), "Tắt mã giảm giá " + v.getCode() + " (đã có lượt dùng)");
		} else {
			repo.delete(v);
			audit.log("DELETE", "VOUCHER", v.getCode(), "Xoá mã giảm giá " + v.getCode());
		}
	}

	public static VoucherDto toDto(Voucher v) {
		return new VoucherDto(v.getId(), v.getCode(), v.getName(), v.getDescription(), v.getType(), v.getValue(),
				v.getMaxDiscount(), v.getMinOrderValue(), v.getStartAt(), v.getEndAt(), v.getUsageLimit(), v.getUsedCount(),
				v.getPerUserLimit(), v.isActive(), summary(v));
	}

	private static String summary(Voucher v) {
		String min = v.getMinOrderValue().signum() > 0 ? " cho đơn từ " + vnd(v.getMinOrderValue()) : "";
		return switch (v.getType()) {
		case PERCENT -> "Giảm " + v.getValue().intValue() + "%" + (v.getMaxDiscount() != null ? " tối đa " + vnd(v.getMaxDiscount()) : "") + min;
		case FIXED -> "Giảm " + vnd(v.getValue()) + min;
		case FREESHIP -> "Miễn phí vận chuyển" + (v.getMaxDiscount() != null ? " tối đa " + vnd(v.getMaxDiscount()) : "") + min;
		};
	}

	private static String vnd(BigDecimal v) {
		return NumberFormat.getInstance(Locale.forLanguageTag("vi-VN")).format(v) + "đ";
	}
}
