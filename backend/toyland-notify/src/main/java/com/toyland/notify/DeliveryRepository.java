package com.toyland.notify;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {

	@Query("SELECT d FROM Delivery d WHERE d.status = com.toyland.notify.Delivery.Status.PENDING AND d.nextAttemptAt <= :now ORDER BY d.id")
	List<Delivery> findDue(@Param("now") LocalDateTime now, Pageable pageable);

	List<Delivery> findTop50ByStatusOrderByIdDesc(Delivery.Status status);

	List<Delivery> findByEventId(String eventId);

	long countByStatus(Delivery.Status status);
}
