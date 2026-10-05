package com.toyland.report;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.toyland.audit.AuditService;
import com.toyland.report.ExcelExporter.Col;
import com.toyland.report.ExcelExporter.Kind;
import com.toyland.report.ReportService.CustomerRow;
import com.toyland.report.ReportService.GroupBy;
import com.toyland.report.ReportService.InventoryReport;
import com.toyland.report.ReportService.InventoryRow;
import com.toyland.report.ReportService.Period;
import com.toyland.report.ReportService.ProductRow;
import com.toyland.report.ReportService.SalesReport;
import com.toyland.report.ReportService.SalesRow;

import io.swagger.v3.oas.annotations.tags.Tag;

/** Bao cao + xuat Excel (duong dan /admin/reports/** -> chi ADMIN). */
@Tag(name = "15. Quản trị - Thống kê")
@RestController
@RequestMapping("/api/v1/admin/reports")
public class AdminReportController {

	private static final MediaType XLSX = MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
	private static final DateTimeFormatter D = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	private static final DateTimeFormatter F = DateTimeFormatter.ofPattern("yyyyMMdd");

	private final ReportService service;
	private final AuditService audit;

	public AdminReportController(ReportService service, AuditService audit) {
		this.service = service;
		this.audit = audit;
	}

	// ------------------------------------------------ JSON
	@GetMapping("/sales")
	public SalesReport sales(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
			@RequestParam(defaultValue = "DAY") GroupBy groupBy) {
		return service.sales(from, to, groupBy);
	}

	@GetMapping("/products")
	public List<ProductRow> products(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
			@RequestParam(defaultValue = "revenue") String sort, @RequestParam(defaultValue = "50") int limit) {
		return service.products(from, to, sort, limit);
	}

	@GetMapping("/inventory")
	public InventoryReport inventory() {
		return service.inventory();
	}

	@GetMapping("/customers")
	public List<CustomerRow> customers(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
			@RequestParam(defaultValue = "50") int limit) {
		return service.customers(from, to, limit);
	}

	// ------------------------------------------------ Excel
	@GetMapping("/sales/export")
	public ResponseEntity<byte[]> exportSales(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
			@RequestParam(defaultValue = "DAY") GroupBy groupBy) {
		SalesReport r = service.sales(from, to, groupBy);
		List<Col<SalesRow>> cols = List.of(
				new Col<SalesRow>(groupBy == GroupBy.MONTH ? "Tháng" : "Ngày", Kind.TEXT, SalesRow::period),
				new Col<SalesRow>("Số đơn", Kind.INT, SalesRow::orders),
				new Col<SalesRow>("SL bán", Kind.INT, SalesRow::itemsSold),
				new Col<SalesRow>("Tiền hàng (đ)", Kind.MONEY, SalesRow::goodsRevenue),
				new Col<SalesRow>("Giảm giá (đ)", Kind.MONEY, SalesRow::discounts),
				new Col<SalesRow>("Doanh thu thuần (đ)", Kind.MONEY, SalesRow::netRevenue),
				new Col<SalesRow>("Giá vốn (đ)", Kind.MONEY, SalesRow::cost),
				new Col<SalesRow>("Lợi nhuận gộp (đ)", Kind.MONEY, SalesRow::grossProfit),
				new Col<SalesRow>("Biên LN", Kind.PERCENT, SalesRow::margin));
		byte[] file = ExcelExporter.export("BÁO CÁO DOANH THU - LỢI NHUẬN", label(r.period()), cols, r.rows(), r.total());
		return download(file, "bao-cao-doanh-thu", r.period(), "doanh thu");
	}

	@GetMapping("/products/export")
	public ResponseEntity<byte[]> exportProducts(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
			@RequestParam(defaultValue = "revenue") String sort, @RequestParam(defaultValue = "200") int limit) {
		Period p = service.period(from, to);
		List<Col<ProductRow>> cols = List.of(
				new Col<ProductRow>("Sản phẩm", Kind.TEXT, ProductRow::productName),
				new Col<ProductRow>("Danh mục", Kind.TEXT, ProductRow::category),
				new Col<ProductRow>("Thương hiệu", Kind.TEXT, ProductRow::brand),
				new Col<ProductRow>("SL bán", Kind.INT, ProductRow::quantity),
				new Col<ProductRow>("Doanh thu (đ)", Kind.MONEY, ProductRow::revenue),
				new Col<ProductRow>("Giá vốn (đ)", Kind.MONEY, ProductRow::cost),
				new Col<ProductRow>("Lợi nhuận (đ)", Kind.MONEY, ProductRow::profit),
				new Col<ProductRow>("Biên LN", Kind.PERCENT, ProductRow::margin));
		byte[] file = ExcelExporter.export("BÁO CÁO BÁN HÀNG THEO SẢN PHẨM", label(p), cols, service.products(from, to, sort, limit), null);
		return download(file, "bao-cao-san-pham", p, "sản phẩm");
	}

	@GetMapping("/inventory/export")
	public ResponseEntity<byte[]> exportInventory() {
		InventoryReport r = service.inventory();
		InventoryRow total = new InventoryRow(null, "Tổng cộng", null, null, null, (int) r.totalUnits(), null, null,
				r.totalStockValue(), r.totalRetailValue(), r.rows().stream().mapToLong(InventoryRow::sold30d).sum(), false);
		List<Col<InventoryRow>> cols = List.of(
				new Col<InventoryRow>("SKU", Kind.TEXT, InventoryRow::sku),
				new Col<InventoryRow>("Sản phẩm", Kind.TEXT, InventoryRow::productName),
				new Col<InventoryRow>("Biến thể", Kind.TEXT, InventoryRow::variantName),
				new Col<InventoryRow>("Danh mục", Kind.TEXT, InventoryRow::category),
				new Col<InventoryRow>("Tồn kho", Kind.INT, InventoryRow::stock),
				new Col<InventoryRow>("Giá vốn (đ)", Kind.MONEY, InventoryRow::costPrice),
				new Col<InventoryRow>("Giá bán (đ)", Kind.MONEY, InventoryRow::price),
				new Col<InventoryRow>("Giá trị tồn theo vốn (đ)", Kind.MONEY, InventoryRow::stockValue),
				new Col<InventoryRow>("Giá trị tồn theo giá bán (đ)", Kind.MONEY, InventoryRow::retailValue),
				new Col<InventoryRow>("Bán 30 ngày", Kind.INT, InventoryRow::sold30d));
		LocalDate today = LocalDate.now();
		byte[] file = ExcelExporter.export("BÁO CÁO TỒN KHO", "Tại ngày " + today.format(D) + " · " + r.lowStockCount() + " biến thể sắp hết, "
				+ r.outOfStockCount() + " hết hàng", cols, r.rows(), total);
		return download(file, "bao-cao-ton-kho", new Period(today, today), "tồn kho");
	}

	@GetMapping("/customers/export")
	public ResponseEntity<byte[]> exportCustomers(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
			@RequestParam(defaultValue = "200") int limit) {
		Period p = service.period(from, to);
		List<Col<CustomerRow>> cols = List.of(
				new Col<CustomerRow>("Khách hàng", Kind.TEXT, CustomerRow::fullName),
				new Col<CustomerRow>("Email", Kind.TEXT, CustomerRow::email),
				new Col<CustomerRow>("Điện thoại", Kind.TEXT, CustomerRow::phone),
				new Col<CustomerRow>("Số đơn", Kind.INT, CustomerRow::orders),
				new Col<CustomerRow>("Tổng chi tiêu (đ)", Kind.MONEY, CustomerRow::spent),
				new Col<CustomerRow>("Đơn gần nhất", Kind.DATETIME, CustomerRow::lastOrderAt),
				new Col<CustomerRow>("Điểm hiện có", Kind.INT, CustomerRow::points),
				new Col<CustomerRow>("Hạng", Kind.TEXT, CustomerRow::tier));
		byte[] file = ExcelExporter.export("KHÁCH HÀNG MUA NHIỀU NHẤT", label(p), cols, service.customers(from, to, limit), null);
		return download(file, "bao-cao-khach-hang", p, "khách hàng");
	}

	private static String label(Period p) {
		return "Từ " + p.from().format(D) + " đến " + p.to().format(D) + " · Đơn đã giao / hoàn tất";
	}

	private ResponseEntity<byte[]> download(byte[] file, String name, Period p, String what) {
		String filename = name + "-" + p.from().format(F) + (p.from().equals(p.to()) ? "" : "-" + p.to().format(F)) + ".xlsx";
		audit.log("EXPORT", "REPORT", name, "Xuất Excel báo cáo " + what + " (" + p.from().format(D) + " - " + p.to().format(D) + ")");
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(filename).build().toString())
				.contentType(XLSX)
				.body(file);
	}
}
