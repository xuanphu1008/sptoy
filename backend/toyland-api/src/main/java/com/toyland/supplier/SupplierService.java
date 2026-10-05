package com.toyland.supplier;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.toyland.audit.AuditService;
import com.toyland.common.dto.PageResponse;
import com.toyland.common.exception.ApiException;
import com.toyland.common.util.TextUtils;
import com.toyland.inventory.StockReceiptRepository;
import com.toyland.supplier.SupplierDtos.SupplierDto;
import com.toyland.supplier.SupplierDtos.SupplierOption;
import com.toyland.supplier.SupplierDtos.SupplierRequest;

/** Quan ly nha cung cap + thong ke nhap hang theo tung nha cung cap. */
@Service
@Transactional
public class SupplierService {

	private final SupplierRepository repo;
	private final StockReceiptRepository receiptRepository;
	private final AuditService audit;

	public SupplierService(SupplierRepository repo, StockReceiptRepository receiptRepository, AuditService audit) {
		this.repo = repo;
		this.receiptRepository = receiptRepository;
		this.audit = audit;
	}

	private record Stat(long count, BigDecimal total, LocalDateTime last) {
	}

	@Transactional(readOnly = true)
	public PageResponse<SupplierDto> list(String keyword, Boolean active, int page, int size) {
		String kw = TextUtils.isBlank(keyword) ? null : keyword.trim().toLowerCase();
		Page<Supplier> p = repo.search(kw, active, PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 200)));
		Map<Long, Stat> stats = stats(p.getContent().stream().map(Supplier::getId).toList());
		return PageResponse.of(p, s -> toDto(s, stats.get(s.getId())));
	}

	@Transactional(readOnly = true)
	public List<SupplierOption> options() {
		return repo.findByActiveTrueOrderByNameAsc().stream().map(s -> new SupplierOption(s.getId(), s.getCode(), s.getName())).toList();
	}

	@Transactional(readOnly = true)
	public SupplierDto get(Long id) {
		Supplier s = find(id);
		return toDto(s, stats(List.of(id)).get(id));
	}

	public SupplierDto save(Long id, SupplierRequest r) {
		Supplier s = id == null ? new Supplier() : find(id);
		String code = TextUtils.trimToNull(r.code());
		if (code == null) {
			code = id == null ? nextCode() : s.getCode();
		}
		code = code.toUpperCase();
		boolean dup = id == null ? repo.existsByCodeIgnoreCase(code) : repo.existsByCodeIgnoreCaseAndIdNot(code, id);
		if (dup) {
			throw ApiException.conflict("Mã nhà cung cấp " + code + " đã tồn tại");
		}
		s.setCode(code);
		s.setName(r.name().trim());
		s.setContactName(TextUtils.trimToNull(r.contactName()));
		s.setPhone(TextUtils.trimToNull(r.phone()));
		s.setEmail(TextUtils.trimToNull(r.email()));
		s.setAddress(TextUtils.trimToNull(r.address()));
		s.setTaxCode(TextUtils.trimToNull(r.taxCode()));
		s.setNote(TextUtils.trimToNull(r.note()));
		if (r.active() != null) {
			s.setActive(r.active());
		}
		s = repo.save(s);
		audit.log(id == null ? "CREATE" : "UPDATE", "SUPPLIER", s.getCode(),
				(id == null ? "Thêm nhà cung cấp " : "Cập nhật nhà cung cấp ") + s.getName());
		return toDto(s, stats(List.of(s.getId())).get(s.getId()));
	}

	/** Nha cung cap da co phieu nhap thi khong xoa (giu lich su) - chuyen sang ngung giao dich. */
	public void delete(Long id) {
		Supplier s = find(id);
		if (receiptRepository.countBySupplierRefId(id) > 0) {
			throw ApiException.conflict("Nhà cung cấp đã có phiếu nhập. Hãy chuyển sang \"Ngừng giao dịch\" thay vì xoá.");
		}
		repo.delete(s);
		audit.log("DELETE", "SUPPLIER", s.getCode(), "Xoá nhà cung cấp " + s.getName());
	}

	public Supplier requireActive(Long id) {
		Supplier s = find(id);
		if (!s.isActive()) {
			throw ApiException.badRequest("Nhà cung cấp " + s.getName() + " đang ngừng giao dịch");
		}
		return s;
	}

	private Supplier find(Long id) {
		return repo.findById(id).orElseThrow(() -> ApiException.notFound("nhà cung cấp", id));
	}

	private String nextCode() {
		long n = repo.count() + 1;
		String code;
		do {
			code = String.format("NCC-%04d", n++);
		} while (repo.existsByCodeIgnoreCase(code));
		return code;
	}

	private Map<Long, Stat> stats(List<Long> ids) {
		Map<Long, Stat> m = new HashMap<>();
		if (ids.isEmpty()) {
			return m;
		}
		for (Object[] r : receiptRepository.statsBySupplier(ids)) {
			m.put(((Number) r[0]).longValue(), new Stat(((Number) r[1]).longValue(),
					r[2] == null ? BigDecimal.ZERO : new BigDecimal(r[2].toString()), (LocalDateTime) r[3]));
		}
		return m;
	}

	private static SupplierDto toDto(Supplier s, Stat st) {
		return new SupplierDto(s.getId(), s.getCode(), s.getName(), s.getContactName(), s.getPhone(), s.getEmail(), s.getAddress(),
				s.getTaxCode(), s.getNote(), s.isActive(), st == null ? 0 : st.count(), st == null ? BigDecimal.ZERO : st.total(),
				st == null ? null : st.last(), s.getCreatedAt());
	}
}
