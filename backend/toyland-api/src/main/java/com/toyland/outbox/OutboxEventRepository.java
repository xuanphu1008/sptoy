package com.toyland.outbox;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

	@Query("SELECT e FROM OutboxEvent e WHERE e.status = com.toyland.outbox.OutboxEvent.Status.NEW AND e.nextAttemptAt <= :now ORDER BY e.id")
	List<OutboxEvent> findDue(@Param("now") LocalDateTime now, Pageable pageable);

	long countByStatus(OutboxEvent.Status status);

	List<OutboxEvent> findTop20ByOrderByIdDesc();
}
