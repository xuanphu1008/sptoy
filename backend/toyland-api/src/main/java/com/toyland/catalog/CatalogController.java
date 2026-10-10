package com.toyland.catalog;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.toyland.catalog.dto.CatalogDtos.BrandDto;
import com.toyland.catalog.dto.CatalogDtos.CategoryNode;
import com.toyland.catalog.dto.CatalogDtos.Facets;
import com.toyland.catalog.dto.CatalogDtos.FranchiseDto;
import com.toyland.catalog.dto.CatalogDtos.ProductCard;
import com.toyland.catalog.dto.CatalogDtos.ProductDetail;
import com.toyland.catalog.dto.CatalogDtos.RatingSummary;
import com.toyland.catalog.dto.CatalogDtos.Suggestion;
import com.toyland.catalog.ProductQueryService.SearchParams;
import com.toyland.common.dto.PageResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "02. Cửa hàng - Sản phẩm")
@RestController
@RequestMapping("/api/v1")
public class CatalogController {

	private final ProductQueryService queryService;
	private final HomeService homeService;

	public CatalogController(ProductQueryService queryService, HomeService homeService) {
		this.queryService = queryService;
		this.homeService = homeService;
	}

	@Operation(summary = "Dữ liệu trang chủ (banner, danh mục, sản phẩm nổi bật/bán chạy/mới/giảm giá...)")
	@GetMapping("/home")
	public HomeService.HomeData home() {
		return homeService.home();
	}

	@Operation(summary = "Cây danh mục")
	@GetMapping("/categories")
	@Transactional(readOnly = true)
	public List<CategoryNode> categories() {
		return queryService.tree().toNodes(true);
	}

	@GetMapping("/brands")
	public List<BrandDto> brands() {
		return homeService.brands();
	}

	@GetMapping("/franchises")
	public List<FranchiseDto> franchises() {
		return homeService.franchises();
	}

	@Operation(summary = "Tìm kiếm / lọc / sắp xếp sản phẩm",
			description = "sort: newest | price_asc | price_desc | bestseller | rating | name. "
					+ "age: BABY, TODDLER, PRESCHOOL, SCHOOL, TEEN. gender: BOY, GIRL, UNISEX. brand/franchise: slug, nhiều giá trị cách nhau dấu phẩy")
	@GetMapping("/products")
	public PageResponse<ProductCard> search(@RequestParam(required = false) String q,
			@RequestParam(required = false) String category, @RequestParam(required = false) List<String> brand,
			@RequestParam(required = false) List<String> franchise, @RequestParam(required = false) List<String> age,
			@RequestParam(required = false) List<String> gender, @RequestParam(required = false) BigDecimal minPrice,
			@RequestParam(required = false) BigDecimal maxPrice, @RequestParam(required = false) Boolean inStock,
			@RequestParam(required = false) Boolean onSale, @RequestParam(required = false) Boolean featured,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "24") int size,
			@RequestParam(defaultValue = "newest") String sort) {
		return queryService.search(new SearchParams(q, category, brand, franchise, age, gender, minPrice, maxPrice, inStock,
				onSale, featured), page, size, sort);
	}

	@Operation(summary = "Đếm số sản phẩm theo từng lựa chọn bộ lọc (faceted search)")
	@GetMapping("/products/facets")
	public Facets facets(@RequestParam(required = false) String q, @RequestParam(required = false) String category,
			@RequestParam(required = false) List<String> brand, @RequestParam(required = false) List<String> franchise,
			@RequestParam(required = false) List<String> age, @RequestParam(required = false) List<String> gender,
			@RequestParam(required = false) BigDecimal minPrice, @RequestParam(required = false) BigDecimal maxPrice,
			@RequestParam(required = false) Boolean inStock, @RequestParam(required = false) Boolean onSale,
			@RequestParam(required = false) Boolean featured) {
		return queryService.facets(new SearchParams(q, category, brand, franchise, age, gender, minPrice, maxPrice, inStock, onSale, featured));
	}

	@Operation(summary = "Gợi ý tìm kiếm (autocomplete, không dấu)")
	@GetMapping("/products/suggest")
	public Suggestion suggest(@RequestParam String q) {
		return queryService.suggest(q);
	}

	@Operation(summary = "Lấy thẻ sản phẩm theo danh sách id (đã xem gần đây)")
	@GetMapping("/products/by-ids")
	public List<ProductCard> byIds(@RequestParam List<Long> ids) {
		return queryService.cardsByIds(ids.stream().limit(30).toList());
	}

	@Operation(summary = "Chi tiết sản phẩm theo slug")
	@GetMapping("/products/slug/{slug}")
	public ProductDetail detailBySlug(@PathVariable String slug) {
		return queryService.detailBySlug(slug);
	}

	@GetMapping("/products/{id}")
	public ProductDetail detail(@PathVariable Long id) {
		return queryService.detailById(id);
	}

	@Operation(summary = "Tổng hợp đánh giá (điểm trung bình, số lượng theo sao)")
	@GetMapping("/products/{id}/rating-summary")
	public RatingSummary ratingSummary(@PathVariable Long id) {
		return queryService.ratingSummary(id);
	}
}
