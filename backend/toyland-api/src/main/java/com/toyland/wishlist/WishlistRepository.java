package com.toyland.wishlist;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WishlistRepository extends JpaRepository<WishlistItem, Long> {

	@Query("SELECT w FROM WishlistItem w JOIN FETCH w.product p WHERE w.user.id = :userId AND p.deleted = false ORDER BY w.id DESC")
	List<WishlistItem> findAllOfUser(@Param("userId") Long userId);

	@Query("SELECT w.product.id FROM WishlistItem w WHERE w.user.id = :userId")
	List<Long> findProductIdsOfUser(@Param("userId") Long userId);

	@Query("SELECT w FROM WishlistItem w WHERE w.user.id = :userId AND w.product.id = :productId")
	Optional<WishlistItem> findOne(@Param("userId") Long userId, @Param("productId") Long productId);
}
