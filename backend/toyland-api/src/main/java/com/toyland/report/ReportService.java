package com.toyland.report;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.toyland.common.exception.ApiException;
import com.toyland.inventory.InventoryService;
import com.toyland.loyalty.LoyaltyRules;

import jakarta.persistence.EntityManager;

/**
 * Bao cao quan tri (chi ADMIN): doanh thu - loi nhuan gop theo ky, san pham, ton kho, khach hang.
 * Doanh thu tinh tren don DA GIAO / HOAN TAT, gom theo ngay dat hang (thong nhat voi trang Tong quan).
 * Gia von = gia von luu tren dong don luc ban (order_items.cost_price).
 */
@Service
@Transactional(readOnly = true)
public class ReportService {

	private static final String DONE = "('DELIVERED','COMPLETED')";

	private final EntityManager em;

	public ReportService(EntityManager em) {
		this.em = em;
	}

	public enum GroupBy { DAY, MONTH }

	/** Ky bao cao da chuan hoa [from, to] (mac dinh 30 ngay, toi da 2 nam). */
	public record Period(LocalDate from, LocalDate to) {
		public LocalDateTime start() { return from.atStartOfDay(); }
		public LocalDateTime end() { return to.plusDays(1).atStartOfDay(); }
	}

	public record SalesRow(String period, long orders, long itemsSold, BigDecimal goodsRevenue, BigDecimal discounts,
			BigDecimal netRevenue, BigDecimal cost, BigDecimal grossProfit, double margin) {
	}

	public record SalesReport(Period period, GroupBy groupBy, List<SalesRow> rows, SalesRow total) {
	}

	public record ProductRow(Long productId, String productName, String category, String brand, long quantity,
			BigDecimal revenue, BigDecimal cost, BigDecimal profit, double margin) {
	}

	public record InventoryRow(Long variantId, String sku, String productName, String variantName, String category, int stock,
			BigDecimal costPrice, BigDecimal price, BigDecimal stockValue, BigDecimal retailValue, long sold30d, boolean lowStock) {
	}

	public record InventoryReport(List<InventoryRow> rows, long totalUnits, BigDecimal totalStockValue, BigDecimal totalRetailValue,
			long lowStockCount, long outOfStockCount) {
	}

	public record CustomerRow(Long userId, String fullName, String email, String phone, long orders, BigDecimal spent,
			LocalDateTime lastOrderAt, int points, String tier) {
	}

	public Period period(LocalDate from, LocalDate to) {
		LocalDate t = to == null ? LocalDate.now() : to;
		LocalDate f = from == null ? t.minusDays(29) : from;
		if (f.isAfter(t)) {
			throw ApiException.badRequest("Ngày bắt đầu phải trước ngày kết thúc");
		}
		if (f.isBefore(t.minusYears(2))) {
			throw ApiException.badRequest("Khoảng thời gian tối đa 2 năm");
		}
		return new Period(f, t);
	}

	// ------------------------------------------------------------------ doanh thu - loi nhuan theo ky
	public SalesReport sales(LocalDate from, LocalDate to, GroupBy groupBy) {
		Period p = period(from, to);
		GroupBy g = groupBy == null ? GroupBy.DAY : groupBy;
		String key = g == GroupBy.DAY ? "DATE_FORMAT(o.created_at, '%Y-%m-%d')" : "DATE_FORMAT(o.created_at, '%Y-%m')";
		// khung ky rong (ngay/thang khong co don van hien 0)
		Map<String, SalesRow> map = new LinkedHashMap<>();
		if (g == GroupBy.DAY) {
			for (LocalDate d = p.from(); !d.isAfter(p.to()); d = d.plusDays(1)) {
				map.put(d.toString(), empty(d.toString()));
			}
		} else {
			for (LocalDate d = p.from().withDayOfMonth(1); !d.isAfter(p.to()); d = d.plusMonths(1)) {
				String k = d.toString().substring(0, 7);
				map.put(k, empty(k));
			}
		}
		// tien hang + gia von theo dong don
		Map<String, Object[]> items = new LinkedHashMap<>();
		for (Object[] r : rows("SELECT " + key + ", SUM(oi.quantity), SUM(oi.quantity * oi.unit_price), "
				+ "SUM(oi.quantity * COALESCE(oi.cost_price, 0)) FROM order_items oi JOIN orders o ON o.id = oi.order_id "
				+ "WHERE o.status IN " + DONE + " AND o.created_at >= ?1 AND o.created_at < ?2 GROUP BY " + key, p.start(), p.end())) {
			items.put(String.valueOf(r[0]), r);
		}
		for (Object[] r : rows("SELECT " + key + ", COUNT(*), SUM(o.discount_amount + o.points_discount) FROM orders o "
				+ "WHERE o.status IN " + DONE + " AND o.created_at >= ?1 AND o.created_at < ?2 GROUP BY " + key, p.start(), p.end())) {
			String k = String.valueOf(r[0]);
			Object[] it = items.get(k);
			long orders = num(r[1]);
			BigDecimal disc = dec(r[2]);
			long qty = it == null ? 0 : num(it[1]);
			BigDecimal goods = it == null ? BigDecimal.ZERO : dec(it[2]);
			BigDecimal cost = it == null ? BigDecimal.ZERO : dec(it[3]);
			map.put(k, row(k, orders, qty, goods, disc, cost));
		}
		List<SalesRow> list = new ArrayList<>(map.values());
		long o = 0;
		long q = 0;
		BigDecimal goods = BigDecimal.ZERO;
		BigDecimal disc = BigDecimal.ZERO;
		BigDecimal cost = BigDecimal.ZERO;
		for (SalesRow r : list) {
			o += r.orders();
			q += r.itemsSold();
			goods = goods.add(r.goodsRevenue());
			disc = disc.add(r.discounts());
			cost = cost.add(r.cost());
		}
		return new SalesReport(p, g, list, row("Tổng cộng", o, q, goods, disc, cost));
	}

	// ------------------------------------------------------------------ theo san pham
	public List<ProductRow> products(LocalDate from, LocalDate to, String sort, int limit) {
		Period p = period(from, to);
		String order = switch (sort == null ? "revenue" : sort) {
		case "quantity" -> "SUM(oi.quantity) DESC";
		case "profit" -> "SUM(oi.quantity * (oi.unit_price - COALESCE(oi.cost_price, 0))) DESC";
		default -> "SUM(oi.quantity * oi.unit_price) DESC";
		};
		List<ProductRow> list = new ArrayList<>();
		for (Object[] r : rows("SELECT oi.product_id, MAX(oi.product_name), MAX(c.name), MAX(b.name), SUM(oi.quantity), "
				+ "SUM(oi.quantity * oi.unit_price), SUM(oi.quantity * COALESCE(oi.cost_price, 0)) "
				+ "FROM order_items oi JOIN orders o ON o.id = oi.order_id LEFT JOIN products pr ON pr.id = oi.product_id "
				+ "LEFT JOIN categories c ON c.id = pr.category_id LEFT JOIN brands b ON b.id = pr.brand_id "
				+ "WHERE o.status IN " + DONE + " AND o.created_at >= ?1 AND o.created_at < ?2 "
				+ "GROUP BY oi.product_id ORDER BY " + order + " LIMIT " + Math.min(Math.max(limit, 1), 500), p.start(), p.end())) {
			BigDecimal rev = dec(r[5]);
			BigDecimal cost = dec(r[6]);
			BigDecimal profit = rev.subtract(cost);
			list.add(new ProductRow(r[0] == null ? null : num(r[0]), str(r[1]), str(r[2]), str(r[3]), num(r[4]), rev, cost, profit,
					margin(profit, rev)));
		}
		return list;
	}

	// ------------------------------------------------------------------ ton kho & gia tri ton
	public InventoryReport inventory() {
		LocalDateTime since = LocalDate.now().minusDays(29).atStartOfDay();
		Map<Long, Long> sold = new LinkedHashMap<>();
		for (Object[] r : rows("SELECT oi.variant_id, SUM(oi.quantity) FROM order_items oi JOIN orders o ON o.id = oi.order_id "
				+ "WHERE o.status NOT IN ('CANCELLED','RETURNED') AND o.created_at >= ?1 AND oi.variant_id IS NOT NULL AND 1 = ?2 "
				+ "GROUP BY oi.variant_id", since, 1)) {
			sold.put(num(r[0]), num(r[1]));
		}
		List<InventoryRow> list = new ArrayList<>();
		long units = 0;
		long low = 0;
		long out = 0;
		BigDecimal sv = BigDecimal.ZERO;
		BigDecimal rv = BigDecimal.ZERO;
		@SuppressWarnings("unchecked")
		List<Object[]> data = em.createNativeQuery("SELECT v.id, v.sku, p.name, CONCAT_WS(' / ', v.option1, v.option2), c.name, "
				+ "v.stock_quantity, COALESCE(v.cost_price, 0), v.price FROM product_variants v JOIN products p ON p.id = v.product_id "
				+ "JOIN categories c ON c.id = p.category_id WHERE p.deleted = FALSE AND v.active = TRUE "
				+ "ORDER BY v.stock_quantity * COALESCE(v.cost_price, 0) DESC, v.sku").getResultList();
		for (Object[] r : data) {
			int stock = (int) num(r[5]);
			BigDecimal cost = dec(r[6]);
			BigDecimal price = dec(r[7]);
			BigDecimal stockValue = cost.multiply(BigDecimal.valueOf(stock));
			BigDecimal retail = price.multiply(BigDecimal.valueOf(stock));
			boolean isLow = stock <= InventoryService.LOW_STOCK;
			String vn = str(r[3]);
			list.add(new InventoryRow(num(r[0]), str(r[1]), str(r[2]), vn == null || vn.isBlank() ? null : vn, str(r[4]), stock, cost,
					price, stockValue, retail, sold.getOrDefault(num(r[0]), 0L), isLow));
			units += stock;
			sv = sv.add(stockValue);
			rv = rv.add(retail);
			if (stock == 0) {
				out++;
			} else if (isLow) {
				low++;
			}
		}
		return new InventoryReport(list, units, sv, rv, low, out);
	}

	// ------------------------------------------------------------------ khach hang mua nhieu
	public List<CustomerRow> customers(LocalDate from, LocalDate to, int limit) {
		Period p = period(from, to);
		List<CustomerRow> list = new ArrayList<>();
		for (Object[] r : rows("SELECT u.id, u.full_name, u.email, u.phone, COUNT(o.id), SUM(o.total_amount), MAX(o.created_at), "
				+ "u.points, u.lifetime_points FROM orders o JOIN users u ON u.id = o.user_id "
				+ "WHERE o.status IN " + DONE + " AND o.created_at >= ?1 AND o.created_at < ?2 "
				+ "GROUP BY u.id, u.full_name, u.email, u.phone, u.points, u.lifetime_points ORDER BY SUM(o.total_amount) DESC LIMIT "
				+ Math.min(Math.max(limit, 1), 500), p.start(), p.end())) {
			list.add(new CustomerRow(num(r[0]), str(r[1]), str(r[2]), str(r[3]), num(r[4]), dec(r[5]), toDateTime(r[6]),
					(int) num(r[7]), LoyaltyRules.tierOf((int) num(r[8])).label()));
		}
		return list;
	}

	// ------------------------------------------------------------------ tien ich
	private static SalesRow empty(String k) {
		return new SalesRow(k, 0, 0, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 0);
	}

	private static SalesRow row(String k, long orders, long qty, BigDecimal goods, BigDecimal disc, BigDecimal cost) {
		BigDecimal net = goods.subtract(disc);
		BigDecimal profit = net.subtract(cost);
		return new SalesRow(k, orders, qty, goods, disc, net, cost, profit, margin(profit, net));
	}

	static double margin(BigDecimal profit, BigDecimal revenue) {
		if (revenue == null || revenue.signum() == 0) {
			return 0;
		}
		return profit.multiply(BigDecimal.valueOf(1000)).divide(revenue, 0, RoundingMode.HALF_UP).doubleValue() / 10.0;
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

	private static String str(Object o) {
		return o == null ? null : o.toString();
	}

	private static LocalDateTime toDateTime(Object o) {
		if (o == null) {
			return null;
		}
		if (o instanceof LocalDateTime l) {
			return l;
		}
		if (o instanceof java.sql.Timestamp t) {
			return t.toLocalDateTime();
		}
		return LocalDateTime.parse(o.toString().replace(' ', 'T').substring(0, 19));
	}
}
