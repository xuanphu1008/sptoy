package com.toyland.report;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.toyland.catalog.ProductRepository;
import com.toyland.catalog.ProductVariant;
import com.toyland.catalog.ProductVariantRepository;
import com.toyland.inventory.InventoryService;
import com.toyland.order.OrderMapper;
import com.toyland.order.OrderRepository;
import com.toyland.order.OrderStatus;
import com.toyland.order.ReturnRequestRepository;
import com.toyland.order.ReturnStatus;
import com.toyland.order.dto.OrderDtos.OrderSummary;
import com.toyland.user.Role;
import com.toyland.user.UserRepository;

import jakarta.persistence.EntityManager;

/** So lieu bao cao cho trang tong quan quan tri (dung native SQL de gom nhom theo ngay / danh muc). */
@Service
@Transactional(readOnly = true)
public class DashboardService {

	private static final String DONE = "('DELIVERED','COMPLETED')";

	private final EntityManager em;
	private final OrderRepository orderRepository;
	private final UserRepository userRepository;
	private final ProductRepository productRepository;
	private final ProductVariantRepository variantRepository;
	private final ReturnRequestRepository returnRepository;

	public DashboardService(EntityManager em, OrderRepository orderRepository, UserRepository userRepository,
			ProductRepository productRepository, ProductVariantRepository variantRepository,
			ReturnRequestRepository returnRepository) {
		this.em = em;
		this.orderRepository = orderRepository;
		this.userRepository = userRepository;
		this.productRepository = productRepository;
		this.variantRepository = variantRepository;
		this.returnRepository = returnRepository;
	}

	public record Summary(BigDecimal revenue, long orders, long completedOrders, long cancelledOrders, double cancelRate,
			BigDecimal avgOrderValue, long newCustomers, long activeProducts, long lowStockVariants, long pendingOrders,
			long pendingReturns) {
	}

	public record SeriesPoint(LocalDate date, BigDecimal revenue, long orders) {
	}

	public record TopProduct(Long productId, String name, long quantity, BigDecimal revenue) {
	}

	public record NamedValue(String name, String label, long count, BigDecimal amount) {
	}

	public record LowStockItem(Long variantId, Long productId, String productName, String variantName, String sku, int stock) {
	}

	public record Dashboard(LocalDate from, LocalDate to, Summary summary, List<SeriesPoint> revenueSeries,
			List<TopProduct> topProducts, List<NamedValue> categoryRevenue, List<NamedValue> statusCounts,
			List<NamedValue> paymentShare, List<LowStockItem> lowStock, List<OrderSummary> recentOrders) {
	}

	public Dashboard build(LocalDate fromDate, LocalDate toDate) {
		LocalDate to = toDate == null ? LocalDate.now() : toDate;
		LocalDate from = fromDate == null ? to.minusDays(29) : fromDate;
		LocalDateTime start = from.atStartOfDay();
		LocalDateTime end = to.plusDays(1).atStartOfDay();

		// ---- chuoi doanh thu theo ngay
		Map<LocalDate, SeriesPoint> byDay = new LinkedHashMap<>();
		for (LocalDate d = from; !d.isAfter(to); d = d.plusDays(1)) {
			byDay.put(d, new SeriesPoint(d, BigDecimal.ZERO, 0));
		}
		for (Object[] r : rows("SELECT DATE(created_at), "
				+ "COALESCE(SUM(CASE WHEN status IN " + DONE + " THEN total_amount ELSE 0 END), 0), "
				+ "SUM(CASE WHEN status <> 'CANCELLED' THEN 1 ELSE 0 END) "
				+ "FROM orders WHERE created_at >= ?1 AND created_at < ?2 GROUP BY DATE(created_at)", start, end)) {
			LocalDate d = LocalDate.parse(String.valueOf(r[0]).substring(0, 10));
			byDay.put(d, new SeriesPoint(d, dec(r[1]), num(r[2])));
		}
		List<SeriesPoint> series = new ArrayList<>(byDay.values());

		// ---- dem theo trang thai
		Map<String, Long> statusMap = new LinkedHashMap<>();
		for (Object[] r : rows("SELECT status, COUNT(*) FROM orders WHERE created_at >= ?1 AND created_at < ?2 GROUP BY status", start, end)) {
			statusMap.put(String.valueOf(r[0]), num(r[1]));
		}
		List<NamedValue> statusCounts = new ArrayList<>();
		for (OrderStatus s : OrderStatus.values()) {
			statusCounts.add(new NamedValue(s.name(), s.getLabel(), statusMap.getOrDefault(s.name(), 0L), null));
		}

		// ---- san pham ban chay
		List<TopProduct> top = new ArrayList<>();
		for (Object[] r : rows("SELECT oi.product_id, oi.product_name, SUM(oi.quantity), SUM(oi.quantity * oi.unit_price) "
				+ "FROM order_items oi JOIN orders o ON o.id = oi.order_id "
				+ "WHERE o.status IN " + DONE + " AND o.created_at >= ?1 AND o.created_at < ?2 "
				+ "GROUP BY oi.product_id, oi.product_name ORDER BY SUM(oi.quantity) DESC LIMIT 10", start, end)) {
			top.add(new TopProduct(r[0] == null ? null : num(r[0]), String.valueOf(r[1]), num(r[2]), dec(r[3])));
		}

		// ---- doanh thu theo danh muc goc
		List<NamedValue> byCategory = new ArrayList<>();
		for (Object[] r : rows("SELECT COALESCE(pc.name, c.name), SUM(oi.quantity * oi.unit_price) "
				+ "FROM order_items oi JOIN orders o ON o.id = oi.order_id JOIN products p ON p.id = oi.product_id "
				+ "JOIN categories c ON c.id = p.category_id LEFT JOIN categories pc ON pc.id = c.parent_id "
				+ "WHERE o.status IN " + DONE + " AND o.created_at >= ?1 AND o.created_at < ?2 "
				+ "GROUP BY COALESCE(pc.name, c.name) ORDER BY SUM(oi.quantity * oi.unit_price) DESC", start, end)) {
			byCategory.add(new NamedValue(String.valueOf(r[0]), String.valueOf(r[0]), 0, dec(r[1])));
		}

		// ---- phuong thuc thanh toan
		List<NamedValue> payment = new ArrayList<>();
		for (Object[] r : rows("SELECT payment_method, COUNT(*), COALESCE(SUM(total_amount), 0) FROM orders "
				+ "WHERE status <> 'CANCELLED' AND created_at >= ?1 AND created_at < ?2 GROUP BY payment_method", start, end)) {
			String m = String.valueOf(r[0]);
			payment.add(new NamedValue(m, "COD".equals(m) ? "Thanh toán khi nhận hàng" : "Ví MoMo", num(r[1]), dec(r[2])));
		}

		// ---- tong hop
		BigDecimal revenue = series.stream().map(SeriesPoint::revenue).reduce(BigDecimal.ZERO, BigDecimal::add);
		long totalOrders = statusMap.values().stream().mapToLong(Long::longValue).sum();
		long done = statusMap.getOrDefault("DELIVERED", 0L) + statusMap.getOrDefault("COMPLETED", 0L);
		long cancelled = statusMap.getOrDefault("CANCELLED", 0L);
		BigDecimal aov = done == 0 ? BigDecimal.ZERO : revenue.divide(BigDecimal.valueOf(done), 0, RoundingMode.HALF_UP);
		double cancelRate = totalOrders == 0 ? 0 : Math.round(cancelled * 1000.0 / totalOrders) / 10.0;
		Summary summary = new Summary(revenue, totalOrders, done, cancelled, cancelRate, aov,
				userRepository.countByRoleAndCreatedAtBetween(Role.CUSTOMER, start, end), productRepository.countActive(),
				variantRepository.countLowStock(InventoryService.LOW_STOCK),
				orderRepository.countByStatusIn(List.of(OrderStatus.PENDING, OrderStatus.CONFIRMED, OrderStatus.PACKING)),
				returnRepository.countByStatus(ReturnStatus.REQUESTED));

		List<LowStockItem> low = new ArrayList<>();
		for (ProductVariant v : variantRepository.findLowStock(InventoryService.LOW_STOCK, PageRequest.of(0, 8))) {
			low.add(new LowStockItem(v.getId(), v.getProduct().getId(), v.getProduct().getName(), v.getDisplayName(), v.getSku(),
					v.getStockQuantity()));
		}
		List<OrderSummary> recent = orderRepository.findAll(PageRequest.of(0, 6, Sort.by(Sort.Direction.DESC, "createdAt")))
				.getContent().stream().map(OrderMapper::summary).toList();
		return new Dashboard(from, to, summary, series, top, byCategory, statusCounts, payment, low, recent);
	}

	@SuppressWarnings("unchecked")
	private List<Object[]> rows(String sql, Object p1, Object p2) {
		return em.createNativeQuery(sql).setParameter(1, p1).setParameter(2, p2).getResultList();
	}

	private static long num(Object o) {
		return o == null ? 0 : ((Number) o).longValue();
	}

	private static BigDecimal dec(Object o) {
		if (o == null) {
			return BigDecimal.ZERO;
		}
		return o instanceof BigDecimal b ? b : new BigDecimal(o.toString());
	}
}
