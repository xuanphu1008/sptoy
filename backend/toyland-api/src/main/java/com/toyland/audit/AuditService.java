package com.toyland.audit;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.toyland.common.dto.PageResponse;
import com.toyland.common.util.ClientIp;
import com.toyland.common.util.TextUtils;

/**
 * Ghi nhat ky thao tac. Chay CHUNG transaction voi nghiep vu: nghiep vu loi (rollback) thi dong nhat ky cung khong duoc ghi.
 */
@Service
@Transactional
public class AuditService {

	private final AuditLogRepository repo;

	public AuditService(AuditLogRepository repo) {
		this.repo = repo;
	}

	public record AuditDto(Long id, String actor, String actorRole, String action, String entityType, String entityId,
			String summary, String ip, LocalDateTime createdAt) {
	}

	/** Ghi nhat ky cho nguoi dang dang nhap (lay ten + vai tro tu SecurityContext). */
	public void log(String action, String entityType, Object entityId, String summary) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String actor = "system";
		String role = null;
		if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
			actor = auth.getName();
			for (GrantedAuthority a : auth.getAuthorities()) {
				if (a.getAuthority() != null && a.getAuthority().startsWith("ROLE_")) {
					role = a.getAuthority().substring(5);
					break;
				}
			}
		}
		logAs(actor, role, action, entityType, entityId, summary);
	}

	/** Ghi nhat ky voi nguoi thao tac chi dinh (vd luc dang nhap chua co SecurityContext). */
	public void logAs(String actor, String role, String action, String entityType, Object entityId, String summary) {
		AuditLog a = new AuditLog();
		a.setActor(cut(actor == null ? "system" : actor, 50));
		a.setActorRole(role);
		a.setAction(cut(action, 30));
		a.setEntityType(cut(entityType, 40));
		a.setEntityId(entityId == null ? null : cut(String.valueOf(entityId), 40));
		a.setSummary(cut(summary == null ? "" : summary, 500));
		a.setIp(currentIp());
		repo.save(a);
	}

	@Transactional(readOnly = true)
	public PageResponse<AuditDto> search(String actor, String entityType, String action, LocalDate from, LocalDate to,
			String keyword, int page, int size) {
		String kw = TextUtils.isBlank(keyword) ? null : keyword.trim().toLowerCase();
		return PageResponse.of(repo.search(TextUtils.trimToNull(actor), TextUtils.trimToNull(entityType), TextUtils.trimToNull(action),
				from == null ? null : from.atStartOfDay(), to == null ? null : to.plusDays(1).atStartOfDay(), kw,
				PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 200))),
				a -> new AuditDto(a.getId(), a.getActor(), a.getActorRole(), a.getAction(), a.getEntityType(), a.getEntityId(),
						a.getSummary(), a.getIp(), a.getCreatedAt()));
	}

	private static String currentIp() {
		RequestAttributes ra = RequestContextHolder.getRequestAttributes();
		if (ra instanceof ServletRequestAttributes sra) {
			return cut(ClientIp.of(sra.getRequest()), 45);
		}
		return null;
	}

	private static String cut(String s, int max) {
		if (s == null) {
			return null;
		}
		return s.length() > max ? s.substring(0, max) : s;
	}
}
