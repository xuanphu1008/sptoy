package com.toyland.inventory;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StockReceiptRepository extends JpaRepository<StockReceipt, Long> {

	Page<StockReceipt> findAllByOrderByIdDesc(Pageable pageable);

	boolean existsByCode(String code);

	Page<StockReceipt> findBySupplierRefIdOrderByIdDesc(Long supplierId, Pageable pageable);

	long countBySupplierRefId(Long supplierId);

	/** [supplierId, so phieu, tong tien nhap, lan nhap gan nhat] */
	@Query("SELECT r.supplierRef.id, COUNT(r), SUM(r.totalCost), MAX(r.createdAt) FROM StockReceipt r "
			+ "WHERE r.supplierRef.id IN :ids GROUP BY r.supplierRef.id")
	List<Object[]> statsBySupplier(@Param("ids") List<Long> ids);
}
