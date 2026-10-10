package com.toyland.review;

import java.util.List;

/** Giao dien cong bo cua mien Danh gia: so lieu danh gia cua mot san pham (dung o trang chi tiet). */
public interface ReviewStatsApi {

	/** Moi phan tu: [so sao, so luong]. */
	List<Object[]> countByRating(Long productId);

	long countWithImages(Long productId);
}
