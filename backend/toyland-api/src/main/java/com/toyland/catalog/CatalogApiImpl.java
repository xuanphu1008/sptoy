package com.toyland.catalog;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
class CatalogApiImpl implements CatalogApi {

	private final ProductRepository productRepository;
	private final ProductVariantRepository variantRepository;
	private final CategoryRepository categoryRepository;
	private final BrandRepository brandRepository;
	private final FranchiseRepository franchiseRepository;

	CatalogApiImpl(ProductRepository productRepository, ProductVariantRepository variantRepository,
			CategoryRepository categoryRepository, BrandRepository brandRepository, FranchiseRepository franchiseRepository) {
		this.productRepository = productRepository;
		this.variantRepository = variantRepository;
		this.categoryRepository = categoryRepository;
		this.brandRepository = brandRepository;
		this.franchiseRepository = franchiseRepository;
	}

	@Override
	public Optional<ProductVariant> findVariantWithProduct(Long variantId) {
		return variantRepository.findWithProduct(variantId);
	}

	@Override
	public List<ProductVariant> findVariantsWithProduct(Collection<Long> variantIds) {
		return variantRepository.findAllWithProduct(variantIds);
	}

	@Override
	public Optional<Product> findProduct(Long productId) {
		return productRepository.findByIdAndDeletedFalse(productId);
	}

	@Override
	public Optional<String> categoryName(Long id) {
		return categoryRepository.findById(id).map(Category::getName);
	}

	@Override
	public Optional<String> brandName(Long id) {
		return brandRepository.findById(id).map(Brand::getName);
	}

	@Override
	public Optional<String> franchiseName(Long id) {
		return franchiseRepository.findById(id).map(Franchise::getName);
	}

	@Override
	public Optional<String> productName(Long id) {
		return productRepository.findById(id).map(Product::getName);
	}

	@Override
	public long countActiveProducts() {
		return productRepository.countActive();
	}

	@Override
	public long countLowStock(int threshold) {
		return variantRepository.countLowStock(threshold);
	}

	@Override
	public List<ProductVariant> findLowStock(int threshold, int limit) {
		return variantRepository.findLowStock(threshold, PageRequest.of(0, limit));
	}

	@Override
	public SellablePage sellableItems(int page, int size) {
		var res = variantRepository.findSellable(PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100),
				org.springframework.data.domain.Sort.by("id")));
		return new SellablePage(res.getContent().stream().map(CatalogApiImpl::toItem).toList(), res.getNumber(), res.getSize(),
				res.getTotalElements(), res.getTotalPages());
	}

	@Override
	public Optional<SellableItem> sellableBySku(String sku) {
		return variantRepository.findSellableBySku(sku).map(CatalogApiImpl::toItem);
	}

	@Override
	public Optional<ProductVariant> findSellableVariant(String sku) {
		return variantRepository.findSellableBySku(sku);
	}

	private static SellableItem toItem(ProductVariant v) {
		Product p = v.getProduct();
		String variantName = java.util.stream.Stream.of(v.getOption1(), v.getOption2())
				.filter(x -> x != null && !x.isBlank()).collect(java.util.stream.Collectors.joining(" / "));
		return new SellableItem(v.getSku(), p.getId(), p.getName(), variantName.isEmpty() ? null : variantName,
				p.getCategory() != null ? p.getCategory().getName() : null, p.getBrand() != null ? p.getBrand().getName() : null,
				v.getPrice(), v.getCompareAtPrice(), Math.max(v.getStockQuantity(), 0), v.getImageUrl());
	}
}
