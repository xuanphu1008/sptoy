package com.toyland.loyalty;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.toyland.audit.AuditService;
import com.toyland.auth.CurrentUserService;
import com.toyland.common.dto.PageResponse;
import com.toyland.common.exception.ApiException;
import com.toyland.loyalty.LoyaltyRules.Tier;
import com.toyland.notification.NotificationService;
import com.toyland.order.Order;
import com.toyland.user.User;
import com.toyland.user.UserApi;

/**
 * Khach hang than thiet: tich diem khi don HOAN TAT, dung diem khi dat hang, hoan diem khi huy/tra,
 * quan tri dieu chinh thu cong. So du diem cap nhat bang cau lenh nguyen tu (UserApi.deductPoints/addPoints).
 */
@Service
@Transactional
public class LoyaltyService {

	private final UserApi users;
	private final PointTransactionRepository txRepository;
	private final CurrentUserService currentUser;
	private final NotificationService notificationService;
	private final AuditService audit;
	private final LoyaltyRules rules;

	public LoyaltyService(UserApi users, PointTransactionRepository txRepository, CurrentUserService currentUser,
			NotificationService notificationService, AuditService audit,
			@Value("${app.loyalty.earn-per-amount:10000}") int earnPerAmount,
			@Value("${app.loyalty.point-value:100}") int pointValue,
			@Value("${app.loyalty.max-redeem-percent:50}") int maxRedeemPercent) {
		this.users = users;
		this.txRepository = txRepository;
		this.currentUser = currentUser;
		this.notificationService = notificationService;
		this.audit = audit;
		this.rules = new LoyaltyRules(earnPerAmount, pointValue, maxRedeemPercent);
	}

	public record TierDto(String code, String label, int minPoints, BigDecimal multiplier) {
	}

	public record Summary(int points, int lifetimePoints, TierDto tier, TierDto nextTier, int pointsToNextTier,
			BigDecimal pointsValue, int pointValue, int earnPerAmount, int maxRedeemPercent) {
	}

	public record TxDto(Long id, Long orderId, PointType type, int points, int balanceAfter, String note, String createdBy,
			LocalDateTime createdAt) {
	}

	public LoyaltyRules rules() {
		return rules;
	}

	// ------------------------------------------------ doc
	@Transactional(readOnly = true)
	public Summary mySummary() {
		return summary(currentUser.require().getId());
	}

	@Transactional(readOnly = true)
	public Summary summary(Long userId) {
		User u = users.findById(userId).orElseThrow(() -> ApiException.notFound("người dùng", userId));
		int points = users.pointsOf(userId);
		Tier t = LoyaltyRules.tierOf(u.getLifetimePoints());
		Tier next = LoyaltyRules.nextTier(u.getLifetimePoints());
		return new Summary(points, u.getLifetimePoints(), dto(t), next == null ? null : dto(next),
				next == null ? 0 : next.minPoints() - u.getLifetimePoints(), rules.valueOf(points), rules.pointValue(),
				rules.earnPerAmount(), rules.maxRedeemPercent());
	}

	@Transactional(readOnly = true)
	public PageResponse<TxDto> history(Long userId, int page, int size) {
		return PageResponse.of(txRepository.findByUserIdOrderByIdDesc(userId, PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100))),
				t -> new TxDto(t.getId(), t.getOrderId(), t.getType(), t.getPoints(), t.getBalanceAfter(), t.getNote(), t.getCreatedBy(),
						t.getCreatedAt()));
	}

	@Transactional(readOnly = true)
	public PageResponse<TxDto> myHistory(int page, int size) {
		return history(currentUser.require().getId(), page, size);
	}

	public TierDto tierOf(User u) {
		return dto(LoyaltyRules.tierOf(u.getLifetimePoints()));
	}

	// ------------------------------------------------ nghiep vu don hang
	/** Tru diem khi dat hang (goi sau khi don da co id). */
	public void redeem(User u, Order o, int points) {
		if (points <= 0) {
			return;
		}
		if (!users.deductPoints(u.getId(), points)) {
			throw ApiException.conflict("Số điểm thưởng không đủ, vui lòng tải lại trang thanh toán");
		}
		record(u.getId(), o.getId(), PointType.REDEEM, -points, "Dùng điểm cho đơn " + o.getOrderCode(), u.getUsername());
	}

	/** Hoan lai diem da dung khi don bi huy / tra hang (moi don hoan 1 lan). */
	public void refundRedeemed(Order o, String reason) {
		if (o.getUser() == null || o.getPointsUsed() <= 0 || txRepository.existsByOrderIdAndType(o.getId(), PointType.REFUND)) {
			return;
		}
		Long uid = o.getUser().getId();
		users.addPoints(uid, o.getPointsUsed(), 0);
		record(uid, o.getId(), PointType.REFUND, o.getPointsUsed(), reason + " " + o.getOrderCode(), currentUser.actorName());
	}

	/** Cong diem khi don HOAN TAT (theo hang thanh vien tai thoi diem hoan tat). */
	public void earn(Order o) {
		if (o.getUser() == null || o.getPointsEarned() > 0 || txRepository.existsByOrderIdAndType(o.getId(), PointType.EARN)) {
			return;
		}
		User u = o.getUser();
		BigDecimal goods = o.getSubtotal().subtract(o.getDiscountAmount()).subtract(o.getPointsDiscount());
		int pts = rules.pointsToEarn(goods, u.getLifetimePoints());
		if (pts <= 0) {
			return;
		}
		users.addPoints(u.getId(), pts, pts);
		o.setPointsEarned(pts);
		record(u.getId(), o.getId(), PointType.EARN, pts, "Tích điểm đơn " + o.getOrderCode(), "system");
		notificationService.notifyUser(u, "POINTS_EARNED", "Bạn vừa nhận " + pts + " điểm thưởng",
				"Đơn " + o.getOrderCode() + " đã hoàn tất. 1 điểm = " + rules.pointValue() + "đ khi thanh toán.", "/account/points");
	}

	// ------------------------------------------------ quan tri
	public Summary adjust(Long userId, int delta, String note) {
		if (delta == 0) {
			throw ApiException.badRequest("Số điểm điều chỉnh phải khác 0");
		}
		User u = users.findById(userId).orElseThrow(() -> ApiException.notFound("người dùng", userId));
		if (delta < 0) {
			if (!users.deductPoints(userId, -delta)) {
				throw ApiException.badRequest("Khách chỉ còn " + users.pointsOf(userId) + " điểm");
			}
		} else {
			users.addPoints(userId, delta, 0);
		}
		String n = note == null || note.isBlank() ? "Điều chỉnh bởi quản trị" : note.trim();
		record(userId, null, PointType.ADJUST, delta, n, currentUser.actorName());
		audit.log("ADJUST", "POINTS", u.getUsername(), (delta > 0 ? "Cộng " : "Trừ ") + Math.abs(delta) + " điểm cho "
				+ u.getUsername() + ": " + n);
		notificationService.notifyUser(u, "POINTS_ADJUSTED", (delta > 0 ? "Bạn được cộng " : "Bạn bị trừ ") + Math.abs(delta) + " điểm",
				n, "/account/points");
		return summary(userId);
	}

	private void record(Long userId, Long orderId, PointType type, int points, String note, String by) {
		PointTransaction t = new PointTransaction();
		t.setUserId(userId);
		t.setOrderId(orderId);
		t.setType(type);
		t.setPoints(points);
		t.setBalanceAfter(users.pointsOf(userId));
		t.setNote(note != null && note.length() > 300 ? note.substring(0, 300) : note);
		t.setCreatedBy(by);
		txRepository.save(t);
	}

	private static TierDto dto(Tier t) {
		return new TierDto(t.code(), t.label(), t.minPoints(), t.multiplier());
	}
}
