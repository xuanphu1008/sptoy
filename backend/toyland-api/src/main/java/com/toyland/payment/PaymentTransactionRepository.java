package com.toyland.payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {

	@Query("SELECT t FROM PaymentTransaction t JOIN FETCH t.order WHERE t.txnRef = :ref")
	Optional<PaymentTransaction> findByTxnRef(@Param("ref") String txnRef);

	@Query("SELECT t FROM PaymentTransaction t WHERE t.order.id = :orderId ORDER BY t.id DESC")
	List<PaymentTransaction> findByOrder(@Param("orderId") Long orderId);

	/** Tong tien theo chieu (thu / hoan) cua 1 don, chi tinh giao dich thanh cong. */
	@Query("SELECT COALESCE(SUM(t.amount), 0) FROM PaymentTransaction t WHERE t.order.id = :orderId AND t.kind = :kind AND t.status = 'SUCCESS'")
	BigDecimal sumSuccess(@Param("orderId") Long orderId, @Param("kind") PaymentKind kind);

	/** Giao dich MoMo van o PENDING trong khoang [from, to): ung vien de doi soat chu dong khi IPN bi thieu. */
	@Query("SELECT t.txnRef FROM PaymentTransaction t WHERE t.provider = 'MOMO' AND t.kind = com.toyland.payment.PaymentKind.PAYMENT"
			+ " AND t.status = 'PENDING' AND t.createdAt >= :from AND t.createdAt < :to ORDER BY t.id")
	List<String> pendingMomoRefs(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to, Pageable pageable);

	long countByOrderIdAndKind(Long orderId, PaymentKind kind);

	boolean existsByTxnRef(String txnRef);

	String SEARCH_WHERE = " WHERE (:provider IS NULL OR t.provider = :provider) AND (:status IS NULL OR t.status = :status)"
			+ " AND (:kind IS NULL OR t.kind = :kind) AND t.createdAt >= :from AND t.createdAt < :to"
			+ " AND (:kw IS NULL OR LOWER(o.orderCode) LIKE :kw OR LOWER(t.txnRef) LIKE :kw"
			+ " OR LOWER(COALESCE(t.transactionNo, '')) LIKE :kw OR LOWER(o.receiverName) LIKE :kw)";

	@Query(value = "SELECT t FROM PaymentTransaction t JOIN FETCH t.order o" + SEARCH_WHERE + " ORDER BY t.createdAt DESC, t.id DESC",
			countQuery = "SELECT COUNT(t) FROM PaymentTransaction t JOIN t.order o" + SEARCH_WHERE)
	Page<PaymentTransaction> search(@Param("provider") String provider, @Param("status") String status, @Param("kind") PaymentKind kind,
			@Param("from") LocalDateTime from, @Param("to") LocalDateTime to, @Param("kw") String keyword, Pageable pageable);

	/** Giao dich trong ky (tong hop so lieu doi soat). */
	@Query("SELECT t FROM PaymentTransaction t WHERE t.createdAt >= :from AND t.createdAt < :to")
	List<PaymentTransaction> inPeriod(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
