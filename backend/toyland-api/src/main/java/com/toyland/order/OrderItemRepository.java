package com.toyland.order;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

	@Query("SELECT oi FROM OrderItem oi JOIN FETCH oi.order o WHERE oi.id = :id AND o.user.id = :userId")
	Optional<OrderItem> findOwn(@Param("id") Long id, @Param("userId") Long userId);
}
