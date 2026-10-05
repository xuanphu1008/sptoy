package com.toyland.catalog;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.toyland.catalog.dto.AdminCatalogDtos.AdminProductDto;
import com.toyland.catalog.dto.AdminCatalogDtos.AdminProductRow;
import com.toyland.catalog.dto.AdminCatalogDtos.BrandRequest;
import com.toyland.catalog.dto.AdminCatalogDtos.CategoryRequest;
import com.toyland.catalog.dto.AdminCatalogDtos.FranchiseRequest;
import com.toyland.catalog.dto.AdminCatalogDtos.ProductUpsertRequest;
import com.toyland.catalog.dto.CatalogDtos.BrandDto;
import com.toyland.catalog.dto.CatalogDtos.CategoryNode;
import com.toyland.catalog.dto.CatalogDtos.FranchiseDto;
import com.toyland.common.dto.MessageResponse;
import com.toyland.common.dto.PageResponse;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "10. Quản trị - Sản phẩm & danh mục")
@RestController
public class AdminCatalogController {

	private final AdminCatalogService catalogService;
	private final AdminProductService productService;

	public AdminCatalogController(AdminCatalogService catalogService, AdminProductService productService) {
		this.catalogService = catalogService;
		this.productService = productService;
	}

	// ---------------- danh muc (ADMIN)
	@GetMapping("/api/v1/admin/categories")
	public List<CategoryNode> categories() {
		return catalogService.categoryTree();
	}

	@PostMapping("/api/v1/admin/categories")
	@ResponseStatus(HttpStatus.CREATED)
	public CategoryNode createCategory(@Valid @RequestBody CategoryRequest r) {
		return catalogService.saveCategory(null, r);
	}

	@PutMapping("/api/v1/admin/categories/{id}")
	public CategoryNode updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryRequest r) {
		return catalogService.saveCategory(id, r);
	}

	@DeleteMapping("/api/v1/admin/categories/{id}")
	public MessageResponse deleteCategory(@PathVariable Long id) {
		catalogService.deleteCategory(id);
		return new MessageResponse("Đã xoá danh mục");
	}

	// ---------------- thuong hieu (ADMIN)
	@PostMapping("/api/v1/admin/brands")
	@ResponseStatus(HttpStatus.CREATED)
	public BrandDto createBrand(@Valid @RequestBody BrandRequest r) {
		return catalogService.saveBrand(null, r);
	}

	@PutMapping("/api/v1/admin/brands/{id}")
	public BrandDto updateBrand(@PathVariable Long id, @Valid @RequestBody BrandRequest r) {
		return catalogService.saveBrand(id, r);
	}

	@DeleteMapping("/api/v1/admin/brands/{id}")
	public MessageResponse deleteBrand(@PathVariable Long id) {
		catalogService.deleteBrand(id);
		return new MessageResponse("Đã xoá thương hiệu");
	}

	// ---------------- nhan vat (ADMIN)
	@PostMapping("/api/v1/admin/franchises")
	@ResponseStatus(HttpStatus.CREATED)
	public FranchiseDto createFranchise(@Valid @RequestBody FranchiseRequest r) {
		return catalogService.saveFranchise(null, r);
	}

	@PutMapping("/api/v1/admin/franchises/{id}")
	public FranchiseDto updateFranchise(@PathVariable Long id, @Valid @RequestBody FranchiseRequest r) {
		return catalogService.saveFranchise(id, r);
	}

	@DeleteMapping("/api/v1/admin/franchises/{id}")
	public MessageResponse deleteFranchise(@PathVariable Long id) {
		catalogService.deleteFranchise(id);
		return new MessageResponse("Đã xoá nhân vật/chủ đề");
	}

	// ---------------- san pham (ADMIN + STAFF)
	@GetMapping("/api/v1/admin/products")
	public PageResponse<AdminProductRow> products(@RequestParam(required = false) String keyword,
			@RequestParam(required = false) Long categoryId, @RequestParam(required = false) List<ProductStatus> status,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size,
			@RequestParam(required = false) String sort) {
		return productService.list(keyword, categoryId, status, page, size, sort);
	}

	@GetMapping("/api/v1/admin/products/{id}")
	public AdminProductDto product(@PathVariable Long id) {
		return productService.get(id);
	}

	@PostMapping("/api/v1/admin/products")
	@ResponseStatus(HttpStatus.CREATED)
	public AdminProductDto createProduct(@Valid @RequestBody ProductUpsertRequest r) {
		return productService.create(r);
	}

	@PutMapping("/api/v1/admin/products/{id}")
	public AdminProductDto updateProduct(@PathVariable Long id, @Valid @RequestBody ProductUpsertRequest r) {
		return productService.update(id, r);
	}

	@PutMapping("/api/v1/admin/products/{id}/status")
	public MessageResponse changeStatus(@PathVariable Long id, @RequestParam ProductStatus status) {
		productService.changeStatus(id, status);
		return new MessageResponse("Đã cập nhật trạng thái sản phẩm");
	}

	@DeleteMapping("/api/v1/admin/products/{id}")
	public MessageResponse deleteProduct(@PathVariable Long id) {
		productService.delete(id);
		return new MessageResponse("Đã xoá sản phẩm (ẩn khỏi cửa hàng, giữ lịch sử đơn hàng)");
	}
}
