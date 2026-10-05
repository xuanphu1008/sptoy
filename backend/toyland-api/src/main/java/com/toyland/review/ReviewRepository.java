package com.toyland.review;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, Long> {

	@Query(value = "SELECT r FROM Review r JOIN FETCH r.user u WHERE r.product.id = :productId AND r.hidden = false "
			+ "AND (:rating IS NULL OR r.rating = :rating) AND (:withImages = false OR r.imageUrls IS NOT NULL) ORDER BY r.createdAt DESC",
			countQuery = "SELECT COUNT(r) FROM Review r WHERE r.product.id = :productId AND r.hidden = false "
			+ "AND (:rating IS NULL OR r.rating = :rating) AND (:withImages = false OR r.imageUrls IS NOT NULL)")
	Page<Review> findVisible(@Param("productId") Long productId, @Param("rating") Integer rating,
			@Param("withImages") boolean withImages, Pageable pageable);

	/** [rating, so luong] cua cac danh gia dang hien. */
	@Query("SELECT r.rating, COUNT(r) FROM Review r WHERE r.product.id = :productId AND r.hidden = false GROUP BY r.rating")
	List<Object[]> countByRating(@Param("productId") Long productId);

	@Query("SELECT COUNT(r) FROM Review r WHERE r.product.id = :productId AND r.hidden = false AND r.imageUrls IS NOT NULL")
	long countWithImages(@Param("productId") Long productId);

	@Query(value = "SELECT r FROM Review r JOIN FETCH r.user u JOIN FETCH r.product p WHERE (:rating IS NULL OR r.rating = :rating) "
			+ "AND (:hidden IS NULL OR r.hidden = :hidden) AND (:flagged IS NULL OR r.flagged = :flagged) "
			+ "AND (:kw IS NULL OR p.searchText LIKE CONCAT('%', :kw, '%') OR LOWER(u.fullName) LIKE CONCAT('%', :kw, '%')) ORDER BY r.id DESC",
			countQuery = "SELECT COUNT(r) FROM Review r JOIN r.user u JOIN r.product p WHERE (:rating IS NULL OR r.rating = :rating) "
			+ "AND (:hidden IS NULL OR r.hidden = :hidden) AND (:flagged IS NULL OR r.flagged = :flagged) "
			+ "AND (:kw IS NULL OR p.searchText LIKE CONCAT('%', :kw, '%') OR LOWER(u.fullName) LIKE CONCAT('%', :kw, '%'))")
	Page<Review> adminSearch(@Param("rating") Integer rating, @Param("hidden") Boolean hidden, @Param("flagged") Boolean flagged,
			@Param("kw") String keyword, Pageable pageable);

	/** Danh gia dang hien, co noi dung (quet lai khi them tu khoa cam). */
	@Query("SELECT r FROM Review r JOIN FETCH r.product WHERE r.hidden = false AND r.comment IS NOT NULL")
	List<Review> findVisibleWithComment();

	long countByFlaggedTrue();
}
