package com.toyland.promotion;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.toyland.audit.AuditService;
import com.toyland.common.exception.ApiException;
import com.toyland.common.util.TextUtils;
import com.toyland.promotion.dto.PromotionDtos.BannerDto;
import com.toyland.promotion.dto.PromotionDtos.BannerRequest;

@Service
@Transactional
public class BannerService {

	private final BannerRepository repo;
	private final AuditService audit;

	public BannerService(BannerRepository repo, AuditService audit) {
		this.repo = repo;
		this.audit = audit;
	}

	public static BannerDto toDto(Banner b) {
		return new BannerDto(b.getId(), b.getTitle(), b.getSubtitle(), b.getImageUrl(), b.getLinkUrl(), b.getButtonText(),
				b.getBgColor(), b.getSortOrder(), b.isActive(), b.getStartAt(), b.getEndAt());
	}

	@Transactional(readOnly = true)
	public List<BannerDto> visible() {
		return repo.findVisible(LocalDateTime.now()).stream().map(BannerService::toDto).toList();
	}

	@Transactional(readOnly = true)
	public List<BannerDto> all() {
		return repo.findAllByOrderBySortOrderAscIdAsc().stream().map(BannerService::toDto).toList();
	}

	public BannerDto save(Long id, BannerRequest r) {
		Banner b = id == null ? new Banner() : repo.findById(id).orElseThrow(() -> ApiException.notFound("banner", id));
		b.setTitle(r.title().trim());
		b.setSubtitle(TextUtils.trimToNull(r.subtitle()));
		b.setImageUrl(TextUtils.trimToNull(r.imageUrl()));
		b.setLinkUrl(TextUtils.trimToNull(r.linkUrl()));
		b.setButtonText(TextUtils.trimToNull(r.buttonText()));
		b.setBgColor(TextUtils.trimToNull(r.bgColor()));
		b.setSortOrder(r.sortOrder());
		b.setActive(r.active());
		b.setStartAt(r.startAt());
		b.setEndAt(r.endAt());
		b = repo.save(b);
		audit.log(id == null ? "CREATE" : "UPDATE", "BANNER", b.getId(), (id == null ? "Thêm" : "Sửa") + " banner \"" + b.getTitle() + "\"");
		return toDto(b);
	}

	public void delete(Long id) {
		Banner b = repo.findById(id).orElseThrow(() -> ApiException.notFound("banner", id));
		repo.delete(b);
		audit.log("DELETE", "BANNER", id, "Xoá banner \"" + b.getTitle() + "\"");
	}
}
