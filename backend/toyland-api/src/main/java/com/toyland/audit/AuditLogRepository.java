package com.toyland.audit;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

	@Query("SELECT a FROM AuditLog a WHERE (:actor IS NULL OR a.actor = :actor) AND (:entityType IS NULL OR a.entityType = :entityType) "
			+ "AND (:action IS NULL OR a.action = :action) AND (:from IS NULL OR a.createdAt >= :from) AND (:to IS NULL OR a.createdAt < :to) "
			+ "AND (:kw IS NULL OR LOWER(a.summary) LIKE CONCAT('%', :kw, '%') OR a.entityId = :kw) ORDER BY a.id DESC")
	Page<AuditLog> search(@Param("actor") String actor, @Param("entityType") String entityType, @Param("action") String action,
			@Param("from") LocalDateTime from, @Param("to") LocalDateTime to, @Param("kw") String keyword, Pageable pageable);
}
