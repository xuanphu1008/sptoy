package com.toyland.review;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.toyland.auth.EmailVerificationService;
import com.toyland.audit.AuditService;
import com.toyland.auth.CurrentUserService;
import com.toyland.catalog.Product;
import com.toyland.common.dto.PageResponse;
import com.toyland.common.exception.ApiException;
import com.toyland.common.util.TextUtils;
import com.toyland.notification.NotificationService;
import com.toyland.order.OrderItem;
import com.toyland.order.OrderItemRepository;
import com.toyland.order.OrderStatus;
import com.toyland.review.dto.ReviewDtos.AdminReviewDto;
import com.toyland.review.dto.ReviewDtos.ReviewCreateRequest;
import com.toyland.review.dto.ReviewDtos.ReviewDto;
import com.toyland.user.User;

/** Danh gia san pham - chi khach da nhan hang moi duoc danh gia (moi san pham trong don 1 lan). */
@Service
@Transactional
public class ReviewService {

	private final ReviewRepository repo;
	private final OrderItemRepository orderItemRepository;
	private final CurrentUserService currentUser;
	private final KeywordService keywordService;
	private final NotificationService notificationService;
	private final AuditService audit;

	public ReviewService(ReviewRepository repo, OrderItemRepository orderItemRepository, CurrentUserService currentUser,
			KeywordService keywordService, NotificationService notificationService, AuditService audit) {
		this.repo = repo;
		this.orderItemRepository = orderItemRepository;
		this.currentUser = currentUser;
		this.keywordService = keywordService;
		this.notificationService = notificationService;
		this.audit = audit;
	}

	@Transactional(readOnly = true)
	public PageResponse<ReviewDto> list(Long productId, Integer rating, boolean withImages, int page, int size) {
		return PageResponse.of(repo.findVisible(productId, rating, withImages, PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 500))), r -> new ReviewDto(
				r.getId(), TextUtils.maskName(r.getUser().getFullName()), r.getRating(), r.getComment(), r.getImageUrls(),
				r.getVariantName(), r.getOrderItem() != null, r.getAdminReply(), r.getRepliedAt(), r.getCreatedAt(), false));
	}

	public ReviewDto create(ReviewCreateRequest req) {
		User u = currentUser.require();
		EmailVerificationService.requireVerified(u, "viết đánh giá");
		OrderItem item = orderItemRepository.findOwn(req.orderItemId(), u.getId())
				.orElseThrow(() -> ApiException.notFound("sản phẩm đã mua", req.orderItemId()));
		OrderStatus st = item.getOrder().getStatus();
		if (st != OrderStatus.DELIVERED && st != OrderStatus.COMPLETED) {
			throw ApiException.badRequest("CANNOT_REVIEW", "Chỉ đánh giá được sau khi đã nhận hàng");
		}
		if (item.isReviewed()) {
			throw ApiException.conflict("Bạn đã đánh giá sản phẩm này");
		}
		if (item.getProduct() == null) {
			throw ApiException.badRequest("Sản phẩm không còn tồn tại");
		}
		Review r = new Review();
		r.setProduct(item.getProduct());
		r.setUser(u);
		r.setOrderItem(item);
		r.setVariantName(item.getVariantName());
		r.setRating(req.rating());
		r.setComment(TextUtils.trimToNull(req.comment()));
		if (req.imageUrls() != null) {
			r.getImageUrls().addAll(req.imageUrls().stream().filter(s -> !TextUtils.isBlank(s)).limit(5).toList());
		}
		// kiem duyet tu dong: chua tu khoa cam -> an + cho duyet
		List<String> hits = keywordService.check(r.getComment());
		if (!hits.isEmpty()) {
			flag(r, hits);
		}
		repo.save(r);
		item.setReviewed(true);
		recalc(item.getProduct());
		if (r.isFlagged()) {
			notificationService.notifyStaff("REVIEW_FLAGGED", "Đánh giá chờ duyệt",
					u.getFullName() + " đánh giá \"" + item.getProductName() + "\" có từ khoá cấm: " + String.join(", ", hits),
					"/admin/reviews?flagged=true");
		}
		return new ReviewDto(r.getId(), TextUtils.maskName(u.getFullName()), r.getRating(), r.getComment(), r.getImageUrls(),
				r.getVariantName(), true, null, null, r.getCreatedAt(), r.isFlagged());
	}

	// ------------------------------------------------ quan tri
	@Transactional(readOnly = true)
	public PageResponse<AdminReviewDto> adminList(Integer rating, Boolean hidden, Boolean flagged, String keyword, int page, int size) {
		String kw = TextUtils.isBlank(keyword) ? null : TextUtils.normalize(keyword);
		return PageResponse.of(repo.adminSearch(rating, hidden, flagged, kw, PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 500))), this::toAdmin);
	}

	/** Hien lai 1 danh gia bi gan co = duyet (bo co vi pham). */
	public AdminReviewDto setHidden(Long id, boolean hidden) {
		Review r = find(id);
		r.setHidden(hidden);
		if (!hidden) {
			r.setFlagged(false);
			r.setFlagReason(null);
		}
		repo.flush();
		recalc(r.getProduct());
		audit.log(hidden ? "HIDE" : "SHOW", "REVIEW", r.getId(), (hidden ? "Ẩn" : "Hiện") + " đánh giá " + r.getRating()
				+ " sao của " + r.getUser().getFullName() + " cho \"" + r.getProduct().getName() + "\"");
		return toAdmin(r);
	}

	/** Duyet danh gia bi gan co vi pham: bo co + hien thi. */
	public AdminReviewDto approve(Long id) {
		Review r = find(id);
		if (!r.isFlagged()) {
			throw ApiException.badRequest("Đánh giá này không nằm trong hàng chờ duyệt");
		}
		return setHidden(id, false);
	}

	/**
	 * Quet lai cac danh gia dang hien theo danh sach tu khoa hien tai (sau khi them tu khoa moi).
	 * @return so danh gia vua bi an
	 */
	public int rescan() {
		int n = 0;
		Set<Product> touched = new HashSet<>();
		for (Review r : repo.findVisibleWithComment()) {
			List<String> hits = keywordService.check(r.getComment());
			if (!hits.isEmpty()) {
				flag(r, hits);
				touched.add(r.getProduct());
				n++;
			}
		}
		repo.flush();
		for (Product p : touched) {
			recalc(p);
		}
		if (n > 0) {
			audit.log("MODERATE", "REVIEW", null, "Quét lại từ khoá cấm: ẩn " + n + " đánh giá");
		}
		return n;
	}

	private static void flag(Review r, List<String> hits) {
		r.setHidden(true);
		r.setFlagged(true);
		String reason = "Chứa từ khoá cấm: " + String.join(", ", hits);
		r.setFlagReason(reason.length() > 300 ? reason.substring(0, 300) : reason);
	}

	public AdminReviewDto reply(Long id, String reply) {
		Review r = find(id);
		r.setAdminReply(TextUtils.trimToNull(reply));
		r.setRepliedAt(r.getAdminReply() == null ? null : LocalDateTime.now());
		audit.log("REPLY", "REVIEW", r.getId(), "Phản hồi đánh giá của " + r.getUser().getFullName());
		return toAdmin(r);
	}

	/** Tinh lai diem trung binh + so danh gia (chi danh gia dang hien). */
	private void recalc(Product p) {
		repo.flush();
		long total = 0;
		long sum = 0;
		for (Object[] row : repo.countByRating(p.getId())) {
			int rating = ((Number) row[0]).intValue();
			long c = ((Number) row[1]).longValue();
			total += c;
			sum += rating * c;
		}
		p.setReviewCount((int) total);
		p.setAvgRating(total == 0 ? BigDecimal.ZERO : BigDecimal.valueOf(sum).divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP));
	}

	private Review find(Long id) {
		return repo.findById(id).orElseThrow(() -> ApiException.notFound("đánh giá", id));
	}

	private AdminReviewDto toAdmin(Review r) {
		return new AdminReviewDto(r.getId(), r.getProduct().getId(), r.getProduct().getName(), r.getProduct().getSlug(),
				r.getUser().getFullName(), r.getUser().getEmail(), r.getRating(), r.getComment(), r.getImageUrls(), r.isHidden(),
				r.getAdminReply(), r.getCreatedAt(), r.isFlagged(), r.getFlagReason());
	}
}
