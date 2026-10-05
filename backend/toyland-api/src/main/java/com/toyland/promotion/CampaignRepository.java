package com.toyland.promotion;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CampaignRepository extends JpaRepository<Campaign, Long> {

	List<Campaign> findAllByOrderByStartAtDesc();

	@Query("SELECT c FROM Campaign c WHERE c.active = true AND c.startAt <= :now AND c.endAt >= :now ORDER BY c.discountPercent DESC")
	List<Campaign> findRunning(@Param("now") LocalDateTime now);
}
