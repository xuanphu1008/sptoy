package com.toyland.outbox;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.toyland.notification.MailService.Email;

/**
 * Ghi su kien vao bang outbox. BAT BUOC goi trong transaction nghiep vu dang mo (MANDATORY):
 * neu transaction nghiep vu rollback thi su kien cung khong ton tai, khong co thong bao ma.
 */
@Service
public class OutboxService {

	private final OutboxEventRepository repository;
	private final ObjectMapper mapper;

	public OutboxService(OutboxEventRepository repository, ObjectMapper mapper) {
		this.repository = repository;
		this.mapper = mapper;
	}

	public record Row(Long id, String eventId, String type, String aggregateId, Long partnerId, String status, int attempts,
			java.time.LocalDateTime createdAt, java.time.LocalDateTime sentAt, String lastError) {
	}

	public record Summary(long newCount, long sentCount, long deadCount, java.util.List<Row> recent) {
	}

	@Transactional(readOnly = true)
	public Summary summary() {
		var recent = repository.findTop20ByOrderByIdDesc().stream().map(e -> new Row(e.getId(), e.getEventId(), e.getType(),
				e.getAggregateId(), e.getPartnerId(), e.getStatus().name(), e.getAttempts(), e.getCreatedAt(), e.getSentAt(),
				e.getLastError())).toList();
		return new Summary(repository.countByStatus(OutboxEvent.Status.NEW), repository.countByStatus(OutboxEvent.Status.SENT),
				repository.countByStatus(OutboxEvent.Status.DEAD), recent);
	}

	/** Dat su kien (thuong la DEAD) ve NEW de bo phat gui lai ngay. */
	@Transactional
	public void retry(Long id) {
		OutboxEvent e = repository.findById(id).orElseThrow(() -> com.toyland.common.exception.ApiException.notFound("sự kiện", id));
		e.setStatus(OutboxEvent.Status.NEW);
		e.setAttempts(0);
		e.setNextAttemptAt(java.time.LocalDateTime.now());
	}

	/**
	 * @param email       thu da dung xong gui cho khach (null neu khong co)
	 * @param partnerData du lieu gui cho doi tac qua webhook (null neu don khong thuoc doi tac)
	 */
	@Transactional(propagation = Propagation.MANDATORY)
	public OutboxEvent publish(String type, String aggregateId, Long partnerId, Email email, Map<String, Object> partnerData) {
		Map<String, Object> payload = new LinkedHashMap<>();
		payload.put("email", email == null ? null : Map.of("to", email.to(), "subject", email.subject(), "html", email.html()));
		payload.put("partner", partnerData);
		OutboxEvent e = new OutboxEvent();
		e.setEventId(UUID.randomUUID().toString());
		e.setType(type);
		e.setAggregateId(aggregateId);
		e.setPartnerId(partnerId);
		try {
			e.setPayload(mapper.writeValueAsString(payload));
		} catch (JsonProcessingException ex) {
			throw new IllegalStateException("Khong ghi duoc payload su kien", ex);
		}
		return repository.save(e);
	}
}
