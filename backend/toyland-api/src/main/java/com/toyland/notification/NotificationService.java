package com.toyland.notification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.toyland.auth.CurrentUserService;
import com.toyland.common.dto.PageResponse;
import com.toyland.common.exception.ApiException;
import com.toyland.common.util.AfterCommit;
import com.toyland.user.User;
import com.toyland.user.UserApi;

/**
 * Thong bao trong ung dung.
 * Ghi CSDL trong transaction nghiep vu, SAU KHI commit moi day realtime qua WebSocket (STOMP)
 * toi kenh rieng tung nguoi: /user/queue/notifications.
 */
@Service
@Transactional
public class NotificationService {

	private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
	public static final String USER_QUEUE = "/queue/notifications";

	private final NotificationRepository repo;
	private final UserApi users;
	private final CurrentUserService currentUser;
	private final SimpMessagingTemplate broker;

	public NotificationService(NotificationRepository repo, UserApi users, CurrentUserService currentUser,
			SimpMessagingTemplate broker) {
		this.repo = repo;
		this.users = users;
		this.currentUser = currentUser;
		this.broker = broker;
	}

	public record NotificationDto(Long id, String type, String title, String message, String link, boolean read,
			LocalDateTime createdAt) {
	}

	/** Goi toi toan bo nhan vien + quan tri (don moi, tra hang, danh gia vi pham, sap het hang...). */
	public void notifyStaff(String type, String title, String message, String link) {
		List<User> staff = users.activeStaff();
		for (User u : staff) {
			notifyUser(u, type, title, message, link);
		}
	}

	/** Goi toi 1 nguoi (vd khach hang khi don doi trang thai). */
	public void notifyUser(User u, String type, String title, String message, String link) {
		Notification n = new Notification();
		n.setUserId(u.getId());
		n.setType(type);
		n.setTitle(cut(title, 200));
		n.setMessage(cut(message, 500));
		n.setLink(cut(link, 300));
		repo.save(n);
		String username = u.getUsername();
		NotificationDto dto = toDto(n);
		AfterCommit.run(() -> push(username, dto));
	}

	private void push(String username, NotificationDto dto) {
		try {
			broker.convertAndSendToUser(username, USER_QUEUE, dto);
		} catch (RuntimeException e) {
			// khong co ai dang ket noi / loi broker: bo qua, thong bao van nam trong CSDL
			log.debug("Khong day duoc thong bao realtime cho {}: {}", username, e.getMessage());
		}
	}

	// ------------------------------------------------ API cua nguoi dang dang nhap
	@Transactional(readOnly = true)
	public PageResponse<NotificationDto> mine(boolean unreadOnly, int page, int size) {
		Long uid = currentUser.requireId();
		PageRequest pr = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50));
		return PageResponse.of(unreadOnly ? repo.findByUserIdAndReadFalseOrderByIdDesc(uid, pr)
				: repo.findByUserIdOrderByIdDesc(uid, pr), NotificationService::toDto);
	}

	@Transactional(readOnly = true)
	public Map<String, Long> unreadCount() {
		return Map.of("unread", repo.countByUserIdAndReadFalse(currentUser.requireId()));
	}

	public NotificationDto markRead(Long id) {
		Notification n = repo.findByIdAndUserId(id, currentUser.requireId())
				.orElseThrow(() -> ApiException.notFound("thông báo", id));
		n.setRead(true);
		return toDto(n);
	}

	public int markAllRead() {
		return repo.markAllRead(currentUser.requireId());
	}

	static NotificationDto toDto(Notification n) {
		return new NotificationDto(n.getId(), n.getType(), n.getTitle(), n.getMessage(), n.getLink(), n.isRead(), n.getCreatedAt());
	}

	/** 1250000 -> "1.250.000đ" */
	public static String vnd(java.math.BigDecimal v) {
		List<String> parts = new ArrayList<>();
		String s = v == null ? "0" : v.setScale(0, java.math.RoundingMode.HALF_UP).toPlainString();
		boolean neg = s.startsWith("-");
		if (neg) {
			s = s.substring(1);
		}
		while (s.length() > 3) {
			parts.add(0, s.substring(s.length() - 3));
			s = s.substring(0, s.length() - 3);
		}
		parts.add(0, s);
		return (neg ? "-" : "") + String.join(".", parts) + "đ";
	}

	private static String cut(String s, int max) {
		return s == null || s.length() <= max ? s : s.substring(0, max);
	}
}
