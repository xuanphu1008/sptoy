package com.toyland.notification;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.toyland.common.dto.MessageResponse;
import com.toyland.common.dto.PageResponse;
import com.toyland.notification.NotificationService.NotificationDto;

import io.swagger.v3.oas.annotations.tags.Tag;

/** Thong bao cua nguoi dang dang nhap (khach hang lan nhan vien). Realtime: STOMP /ws -> /user/queue/notifications. */
@Tag(name = "20. Thông báo")
@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

	private final NotificationService service;

	public NotificationController(NotificationService service) {
		this.service = service;
	}

	@GetMapping
	public PageResponse<NotificationDto> mine(@RequestParam(defaultValue = "false") boolean unreadOnly,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
		return service.mine(unreadOnly, page, size);
	}

	@GetMapping("/unread-count")
	public Map<String, Long> unreadCount() {
		return service.unreadCount();
	}

	@PutMapping("/{id}/read")
	public NotificationDto read(@PathVariable Long id) {
		return service.markRead(id);
	}

	@PutMapping("/read-all")
	public MessageResponse readAll() {
		int n = service.markAllRead();
		return new MessageResponse("Đã đánh dấu " + n + " thông báo là đã đọc");
	}
}
