package com.toyland.promotion;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.toyland.audit.AuditService;
import com.toyland.catalog.BrandRepository;
import com.toyland.catalog.CategoryRepository;
import com.toyland.catalog.FranchiseRepository;
import com.toyland.catalog.ProductRepository;
import com.toyland.common.exception.ApiException;
import com.toyland.common.util.TextUtils;
import com.toyland.promotion.dto.PromotionDtos.CampaignDto;
import com.toyland.promotion.dto.PromotionDtos.CampaignRequest;

@Service
@Transactional
public class CampaignService {

	private final CampaignRepository repo;
	private final CategoryRepository categoryRepository;
	private final BrandRepository brandRepository;
	private final FranchiseRepository franchiseRepository;
	private final ProductRepository productRepository;
	private final AuditService audit;

	public CampaignService(CampaignRepository repo, CategoryRepository categoryRepository, BrandRepository brandRepository,
			FranchiseRepository franchiseRepository, ProductRepository productRepository, AuditService audit) {
		this.repo = repo;
		this.categoryRepository = categoryRepository;
		this.brandRepository = brandRepository;
		this.franchiseRepository = franchiseRepository;
		this.productRepository = productRepository;
		this.audit = audit;
	}

	@Transactional(readOnly = true)
	public List<CampaignDto> all() {
		return repo.findAllByOrderByStartAtDesc().stream().map(this::toDto).toList();
	}

	public CampaignDto save(Long id, CampaignRequest r) {
		if (!r.endAt().isAfter(r.startAt())) {
			throw ApiException.badRequest("Thời gian kết thúc phải sau thời gian bắt đầu");
		}
		if (r.scopeType() != CampaignScope.ALL && r.scopeId() == null) {
			throw ApiException.badRequest("Vui lòng chọn đối tượng áp dụng");
		}
		Campaign c = id == null ? new Campaign() : repo.findById(id).orElseThrow(() -> ApiException.notFound("chương trình", id));
		c.setName(r.name().trim());
		c.setDescription(TextUtils.trimToNull(r.description()));
		c.setDiscountPercent(r.discountPercent());
		c.setScopeType(r.scopeType());
		c.setScopeId(r.scopeType() == CampaignScope.ALL ? null : r.scopeId());
		c.setBadgeLabel(TextUtils.isBlank(r.badgeLabel()) ? "-" + r.discountPercent() + "%" : r.badgeLabel().trim());
		c.setStartAt(r.startAt());
		c.setEndAt(r.endAt());
		c.setActive(r.active());
		scopeName(c, true); // kiem tra doi tuong ton tai
		c = repo.save(c);
		audit.log(id == null ? "CREATE" : "UPDATE", "CAMPAIGN", c.getId(), (id == null ? "Tạo" : "Sửa") + " chương trình \"" + c.getName()
				+ "\" giảm " + c.getDiscountPercent() + "%");
		return toDto(c);
	}

	public void delete(Long id) {
		Campaign c = repo.findById(id).orElseThrow(() -> ApiException.notFound("chương trình", id));
		repo.delete(c);
		audit.log("DELETE", "CAMPAIGN", id, "Xoá chương trình \"" + c.getName() + "\"");
	}

	private CampaignDto toDto(Campaign c) {
		LocalDateTime now = LocalDateTime.now();
		boolean running = c.isActive() && !now.isBefore(c.getStartAt()) && !now.isAfter(c.getEndAt());
		return new CampaignDto(c.getId(), c.getName(), c.getDescription(), c.getDiscountPercent(), c.getScopeType(),
				c.getScopeId(), scopeName(c, false), c.getBadgeLabel(), c.getStartAt(), c.getEndAt(), c.isActive(), running);
	}

	/** Ten doi tuong ap dung. strict = true khi luu (bao loi neu khong ton tai); khi hien thi thi tra nhan thay the. */
	private String scopeName(Campaign c, boolean strict) {
		Long id = c.getScopeId();
		Optional<String> name = switch (c.getScopeType()) {
		case ALL -> Optional.of("Toàn bộ sản phẩm");
		case CATEGORY -> categoryRepository.findById(id).map(x -> x.getName());
		case BRAND -> brandRepository.findById(id).map(x -> x.getName());
		case FRANCHISE -> franchiseRepository.findById(id).map(x -> x.getName());
		case PRODUCT -> productRepository.findById(id).map(x -> x.getName());
		};
		if (name.isEmpty() && strict) {
			throw ApiException.notFound("đối tượng áp dụng", id);
		}
		return name.orElse("(đã xoá #" + id + ")");
	}
}
