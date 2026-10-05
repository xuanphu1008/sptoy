package com.toyland.cart;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

	@Query("SELECT c FROM CartItem c JOIN FETCH c.variant v JOIN FETCH v.product p WHERE c.user.id = :userId ORDER BY c.id ASC")
	List<CartItem> findAllOfUser(@Param("userId") Long userId);

	@Query("SELECT c FROM CartItem c WHERE c.user.id = :userId AND c.variant.id = :variantId")
	Optional<CartItem> findOne(@Param("userId") Long userId, @Param("variantId") Long variantId);

	@Query("SELECT c FROM CartItem c JOIN FETCH c.variant v JOIN FETCH v.product p WHERE c.id = :id AND c.user.id = :userId")
	Optional<CartItem> findOwn(@Param("id") Long id, @Param("userId") Long userId);

	@Modifying
	@Query("DELETE FROM CartItem c WHERE c.user.id = :userId")
	void deleteAllOfUser(@Param("userId") Long userId);
}
