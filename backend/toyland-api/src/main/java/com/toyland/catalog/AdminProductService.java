package com.toyland.catalog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.toyland.audit.AuditService;
import com.toyland.catalog.dto.AdminCatalogDtos.AdminProductDto;
import com.toyland.catalog.dto.AdminCatalogDtos.AdminProductRow;
import com.toyland.catalog.dto.AdminCatalogDtos.AdminVariantDto;
import com.toyland.catalog.dto.AdminCatalogDtos.ProductUpsertRequest;
import com.toyland.catalog.dto.AdminCatalogDtos.SpecRequest;
import com.toyland.catalog.dto.AdminCatalogDtos.VariantRequest;
import com.toyland.common.dto.PageResponse;
import com.toyland.common.exception.ApiException;
import com.toyland.common.util.TextUtils;
import com.toyland.inventory.InventoryService;
import com.toyland.inventory.MovementType;

/**
 * Quan tri san pham + bien the. Ton kho KHONG sua truc tiep o day (chi nhap ton ban dau khi tao bien the moi);
 * moi thay doi ton sau do qua module Kho (phieu nhap / dieu chinh) de co nhat ky.
 */
@Service
@Transactional
public class AdminProductService {

	private final ProductRepository productRepository;
	private final ProductVariantRepository variantRepository;
	private final CategoryRepository categoryRepository;
	private final BrandRepository brandRepository;
	private final FranchiseRepository franchiseRepository;
	private final InventoryService inventoryService;
	private final AuditService audit;

	public AdminProductService(ProductRepository productRepository, ProductVariantRepository variantRepository,
			CategoryRepository categoryRepository, BrandRepository brandRepository, FranchiseRepository franchiseRepository,
			InventoryService inventoryService, AuditService audit) {
		this.productRepository = productRepository;
		this.variantRepository = variantRepository;
		this.categoryRepository = categoryRepository;
		this.brandRepository = brandRepository;
		this.franchiseRepository = franchiseRepository;
		this.inventoryService = inventoryService;
		this.audit = audit;
	}

	@Transactional(readOnly = true)
	public PageResponse<AdminProductRow> list(String keyword, Long categoryId, List<ProductStatus> statuses, int page,
			int size, String sort) {
		List<Long> catIds = null;
		if (categoryId != null) {
			catIds = new CategoryTree(categoryRepository.findAllOrdered()).withDescendants(categoryId);
		}
		ProductFilter f = new ProductFilter(keyword, catIds, null, null, null, null, null, null, null, null, null,
				statuses, false, null);
		Sort s;
		if (sort == null || sort.isBlank() || "id".equals(sort)) {
			s = Sort.by(Sort.Direction.DESC, "id");
		} else if ("updated".equals(sort)) {
			s = Sort.by(Sort.Direction.DESC, "updatedAt").and(Sort.by(Sort.Direction.DESC, "id"));
		} else if ("stock_asc".equals(sort)) {
			s = Sort.by(Sort.Direction.ASC, "totalStock").and(Sort.by(Sort.Direction.DESC, "id"));
		} else {
			s = ProductQueryService.sortOf(sort);
		}
		return PageResponse.of(productRepository.findAll(ProductSpecs.of(f), PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 500), s)), p -> new AdminProductRow(
				p.getId(), p.getName(), p.getSlug(), p.getImages().isEmpty() ? null : p.getImages().get(0).getUrl(),
				p.getCategory().getName(), p.getBrand() == null ? null : p.getBrand().getName(), p.getMinPrice(), p.getMaxPrice(),
				p.getTotalStock(), (int) p.getVariants().stream().filter(ProductVariant::isActive).count(), p.getSoldCount(),
				p.getAvgRating(), p.getStatus(), p.isFeatured(), p.getUpdatedAt()));
	}

	@Transactional(readOnly = true)
	public AdminProductDto get(Long id) {
		return toDto(find(id));
	}

	public AdminProductDto create(ProductUpsertRequest r) {
		Product p = new Product();
		apply(p, r);
		p = productRepository.save(p);
		syncVariants(p, r.variants(), true);
		ProductMapper.refreshAggregates(p);
		p.setSearchText(buildSearchText(p));
		audit.log("CREATE", "PRODUCT", p.getId(), "Thêm sản phẩm " + p.getName() + " (" + p.getVariants().size() + " biến thể)");
		return toDto(p);
	}

	public AdminProductDto update(Long id, ProductUpsertRequest r) {
		Product p = find(id);
		apply(p, r);
		syncVariants(p, r.variants(), false);
		ProductMapper.refreshAggregates(p);
		p.setSearchText(buildSearchText(p));
		audit.log("UPDATE", "PRODUCT", p.getId(), "Cập nhật sản phẩm " + p.getName() + " (giá " + p.getMinPrice().toBigInteger()
				+ (p.getMaxPrice().compareTo(p.getMinPrice()) > 0 ? " - " + p.getMaxPrice().toBigInteger() : "") + "đ)");
		return toDto(p);
	}

	/** Xoa mem: an khoi cua hang, giu lich su don hang. */
	public void delete(Long id) {
		Product p = find(id);
		p.setDeleted(true);
		p.setStatus(ProductStatus.INACTIVE);
		p.setSlug(p.getSlug() + "-deleted-" + p.getId());
		audit.log("DELETE", "PRODUCT", p.getId(), "Xoá sản phẩm " + p.getName());
	}

	public void changeStatus(Long id, ProductStatus status) {
		Product p = find(id);
		p.setStatus(status);
		audit.log("STATUS", "PRODUCT", id, "Đổi trạng thái " + p.getName() + " → " + status);
	}

	private Product find(Long id) {
		return productRepository.findByIdAndDeletedFalse(id).orElseThrow(() -> ApiException.notFound("sản phẩm", id));
	}

	private void apply(Product p, ProductUpsertRequest r) {
		String slug = TextUtils.isBlank(r.slug()) ? TextUtils.slugify(r.name()) : TextUtils.slugify(r.slug());
		if (p.getId() == null ? productRepository.existsBySlug(slug) : productRepository.existsBySlugAndIdNot(slug, p.getId())) {
			throw ApiException.conflict("Slug sản phẩm đã tồn tại: " + slug);
		}
		if (r.ageMaxMonths() != null && r.ageMaxMonths() < r.ageMinMonths()) {
			throw ApiException.badRequest("Độ tuổi tối đa phải lớn hơn độ tuổi tối thiểu");
		}
		if (TextUtils.isBlank(r.option1Name()) && !TextUtils.isBlank(r.option2Name())) {
			throw ApiException.badRequest("Phải nhập thuộc tính 1 trước khi nhập thuộc tính 2");
		}
		p.setName(r.name().trim());
		p.setSlug(slug);
		p.setShortDescription(TextUtils.trimToNull(r.shortDescription()));
		p.setDescription(TextUtils.trimToNull(r.description()));
		p.setCategory(categoryRepository.findById(r.categoryId()).orElseThrow(() -> ApiException.notFound("danh mục", r.categoryId())));
		p.setBrand(r.brandId() == null ? null
				: brandRepository.findById(r.brandId()).orElseThrow(() -> ApiException.notFound("thương hiệu", r.brandId())));
		Set<Franchise> frs = new HashSet<>();
		if (r.franchiseIds() != null) {
			frs.addAll(franchiseRepository.findAllById(r.franchiseIds()));
		}
		p.getFranchises().clear();
		p.getFranchises().addAll(frs);
		p.setAgeMinMonths(r.ageMinMonths());
		p.setAgeMaxMonths(r.ageMaxMonths());
		p.setGender(r.gender());
		p.setMaterial(TextUtils.trimToNull(r.material()));
		p.setBrandOrigin(TextUtils.trimToNull(r.brandOrigin()));
		p.setMadeIn(TextUtils.trimToNull(r.madeIn()));
		p.setManufactureYear(r.manufactureYear());
		p.setBattery(TextUtils.trimToNull(r.battery()));
		p.setSmallPartsWarning(r.smallPartsWarning());
		p.setSafetyWarning(TextUtils.trimToNull(r.safetyWarning()));
		p.setCrCertified(r.crCertified());
		p.setImporter(TextUtils.trimToNull(r.importer()));
		p.setLengthCm(r.lengthCm());
		p.setWidthCm(r.widthCm());
		p.setHeightCm(r.heightCm());
		p.setVideoUrl(TextUtils.trimToNull(r.videoUrl()));
		p.setStatus(r.status());
		p.setFeatured(r.featured());
		p.setOnlineExclusive(r.onlineExclusive());
		p.setMetaTitle(TextUtils.trimToNull(r.metaTitle()));
		p.setMetaDescription(TextUtils.trimToNull(r.metaDescription()));
		p.setOption1Name(TextUtils.trimToNull(r.option1Name()));
		p.setOption2Name(TextUtils.trimToNull(r.option2Name()));
		// thong so
		p.getSpecs().clear();
		int i = 0;
		if (r.specs() != null) {
			for (SpecRequest s : r.specs()) {
				ProductSpec spec = new ProductSpec();
				spec.setProduct(p);
				spec.setSpecKey(s.key().trim());
				spec.setSpecValue(s.value().trim());
				spec.setSortOrder(i++);
				p.getSpecs().add(spec);
			}
		}
		// anh
		p.getImages().clear();
		i = 0;
		if (r.images() != null) {
			for (String url : r.images()) {
				if (TextUtils.isBlank(url)) {
					continue;
				}
				ProductImage img = new ProductImage();
				img.setProduct(p);
				img.setUrl(url.trim());
				img.setAlt(p.getName());
				img.setSortOrder(i++);
				p.getImages().add(img);
			}
		}
	}

	/** Dong bo danh sach bien the theo payload: sua bien the co id, tao moi bien the chua co id, tat bien the bi bo. */
	private void syncVariants(Product p, List<VariantRequest> reqs, boolean creating) {
		boolean hasOpt1 = p.getOption1Name() != null;
		boolean hasOpt2 = p.getOption2Name() != null;
		if (!hasOpt1 && reqs.stream().filter(VariantRequest::active).count() > 1) {
			throw ApiException.badRequest("Sản phẩm không có thuộc tính biến thể chỉ được có 1 biến thể");
		}
		if (reqs.stream().noneMatch(VariantRequest::active)) {
			throw ApiException.badRequest("Sản phẩm cần ít nhất 1 biến thể đang bán");
		}
		Set<String> combos = new HashSet<>();
		Set<String> skus = new HashSet<>();
		for (VariantRequest v : reqs) {
			if (!skus.add(v.sku().trim().toUpperCase())) {
				throw ApiException.badRequest("Trùng SKU trong danh sách: " + v.sku());
			}
			if (!v.active()) {
				continue; // bien the da tat (vd bien the cu sau khi doi thuoc tinh) -> khong bat buoc du thuoc tinh
			}
			if (hasOpt1 && TextUtils.isBlank(v.option1())) {
				throw ApiException.badRequest("Biến thể " + v.sku() + " thiếu giá trị " + p.getOption1Name());
			}
			if (hasOpt2 && TextUtils.isBlank(v.option2())) {
				throw ApiException.badRequest("Biến thể " + v.sku() + " thiếu giá trị " + p.getOption2Name());
			}
			String combo = (hasOpt1 ? v.option1().trim().toLowerCase() : "") + "|" + (hasOpt2 ? v.option2().trim().toLowerCase() : "");
			if (!combos.add(combo)) {
				throw ApiException.badRequest("Trùng tổ hợp thuộc tính: " + v.option1() + (hasOpt2 ? " / " + v.option2() : ""));
			}
			if (v.compareAtPrice() != null && v.compareAtPrice().compareTo(v.price()) < 0) {
				throw ApiException.badRequest("Giá gốc của " + v.sku() + " phải >= giá bán");
			}
		}
		Map<Long, ProductVariant> existing = new HashMap<>();
		for (ProductVariant v : p.getVariants()) {
			existing.put(v.getId(), v);
		}
		Set<Long> kept = new HashSet<>();
		List<ProductVariant> created = new ArrayList<>();
		int order = 0;
		boolean defaultSet = false;
		for (VariantRequest r : reqs) {
			String sku = r.sku().trim().toUpperCase();
			ProductVariant v;
			if (r.id() != null && existing.containsKey(r.id())) {
				v = existing.get(r.id());
				if (variantRepository.existsBySkuAndIdNot(sku, v.getId())) {
					throw ApiException.conflict("SKU đã tồn tại ở sản phẩm khác: " + sku);
				}
				kept.add(v.getId());
			} else {
				if (variantRepository.existsBySku(sku)) {
					throw ApiException.conflict("SKU đã tồn tại: " + sku);
				}
				v = new ProductVariant();
				v.setProduct(p);
				v.setStockQuantity(0);
				created.add(v);
			}
			v.setSku(sku);
			v.setBarcode(TextUtils.trimToNull(r.barcode()));
			// bien the da tat co the thieu gia tri thuoc tinh -> giu gia tri cu
			if (!hasOpt1 || !TextUtils.isBlank(r.option1())) {
				v.setOption1(hasOpt1 ? r.option1().trim() : null);
			}
			if (!hasOpt2 || !TextUtils.isBlank(r.option2())) {
				v.setOption2(hasOpt2 ? r.option2().trim() : null);
			}
			v.setPrice(r.price());
			v.setCompareAtPrice(r.compareAtPrice());
			v.setCostPrice(r.costPrice());
			v.setWeightGram(r.weightGram());
			v.setImageUrl(TextUtils.trimToNull(r.imageUrl()));
			v.setActive(r.active());
			v.setSortOrder(order++);
			// bien the mac dinh = bien the DANG BAN dau tien
			v.setDefaultVariant(r.active() && !defaultSet);
			if (r.active()) {
				defaultSet = true;
			}
		}
		// bien the khong con trong payload -> ngung ban (giu lai vi co the nam trong don hang cu)
		for (ProductVariant v : p.getVariants()) {
			if (v.getId() != null && !kept.contains(v.getId())) {
				v.setActive(false);
				v.setDefaultVariant(false);
			}
		}
		for (ProductVariant v : created) {
			variantRepository.save(v);
			p.getVariants().add(v);
		}
		// ton kho ban dau cho bien the moi (ghi nhat ky kho)
		for (int i = 0; i < reqs.size(); i++) {
			VariantRequest r = reqs.get(i);
			if ((r.id() == null || !existing.containsKey(r.id())) && r.initialStock() != null && r.initialStock() > 0) {
				ProductVariant v = created.stream().filter(c -> c.getSku().equals(r.sku().trim().toUpperCase())).findFirst().orElse(null);
				if (v != null) {
					inventoryService.change(v, r.initialStock(), MovementType.INITIAL, creating ? "TAO-SP" : "THEM-BIEN-THE", "Tồn kho ban đầu");
				}
			}
		}
	}

	private String buildSearchText(Product p) {
		StringBuilder sb = new StringBuilder(p.getName());
		if (p.getBrand() != null) {
			sb.append(' ').append(p.getBrand().getName());
		}
		p.getFranchises().forEach(f -> sb.append(' ').append(f.getName()));
		p.getVariants().forEach(v -> sb.append(' ').append(v.getSku()));
		String s = TextUtils.normalize(sb.toString());
		return s.length() > 1000 ? s.substring(0, 1000) : s;
	}

	private AdminProductDto toDto(Product p) {
		return new AdminProductDto(p.getId(), p.getName(), p.getSlug(), p.getShortDescription(), p.getDescription(),
				p.getCategory().getId(), p.getBrand() == null ? null : p.getBrand().getId(),
				p.getFranchises().stream().map(Franchise::getId).toList(), p.getAgeMinMonths(), p.getAgeMaxMonths(),
				p.getGender(), p.getMaterial(), p.getBrandOrigin(), p.getMadeIn(), p.getManufactureYear(), p.getBattery(),
				p.isSmallPartsWarning(), p.getSafetyWarning(), p.isCrCertified(), p.getImporter(), p.getLengthCm(),
				p.getWidthCm(), p.getHeightCm(), p.getVideoUrl(), p.getStatus(), p.isFeatured(), p.isOnlineExclusive(),
				p.getMetaTitle(), p.getMetaDescription(), p.getOption1Name(), p.getOption2Name(),
				p.getSpecs().stream().map(s -> new SpecRequest(s.getSpecKey(), s.getSpecValue())).toList(),
				p.getImages().stream().map(ProductImage::getUrl).toList(),
				p.getVariants().stream().map(v -> new AdminVariantDto(v.getId(), v.getSku(), v.getBarcode(), v.getOption1(),
						v.getOption2(), v.getPrice(), v.getCompareAtPrice(), v.getCostPrice(), v.getStockQuantity(),
						v.getWeightGram(), v.getImageUrl(), v.isActive())).toList(),
				p.getCreatedAt(), p.getUpdatedAt());
	}
}
