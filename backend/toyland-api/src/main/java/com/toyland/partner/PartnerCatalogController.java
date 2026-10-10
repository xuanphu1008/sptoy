package com.toyland.partner;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.toyland.catalog.CatalogApi;
import com.toyland.catalog.CatalogApi.SellableItem;
import com.toyland.catalog.CatalogApi.SellablePage;
import com.toyland.common.exception.ApiException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Dich vu ToyLand cung cap cho ben thu ba (REST, phien ban v1). Chi doc: danh muc hang ban duoc va ton kho.
 * Du lieu tra ve la hinh chieu rieng (SellableItem), khong lo gia von hay thong tin noi bo.
 */
@Tag(name = "13. API đối tác (Partner v1) - ký HMAC")
@RestController
@RequestMapping("/api/partner/v1")
public class PartnerCatalogController {

	private final CatalogApi catalog;

	public PartnerCatalogController(CatalogApi catalog) {
		this.catalog = catalog;
	}

	@Operation(summary = "Danh sách hàng đang bán (phân trang, tối đa 100/trang)")
	@GetMapping("/products")
	public SellablePage products(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
		return catalog.sellableItems(page, size);
	}

	@Operation(summary = "Chi tiết một mã hàng (SKU)")
	@GetMapping("/products/{sku}")
	public SellableItem product(@PathVariable String sku) {
		return catalog.sellableBySku(sku).orElseThrow(() -> ApiException.notFound("mã hàng", sku));
	}

	@Operation(summary = "Tồn kho theo SKU")
	@GetMapping("/stock/{sku}")
	public StockView stock(@PathVariable String sku) {
		SellableItem i = catalog.sellableBySku(sku).orElseThrow(() -> ApiException.notFound("mã hàng", sku));
		return new StockView(i.sku(), i.stock(), i.stock() > 0);
	}

	public record StockView(String sku, int available, boolean inStock) {
	}
}
