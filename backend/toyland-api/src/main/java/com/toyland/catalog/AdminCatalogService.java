package com.toyland.catalog;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.toyland.audit.AuditService;
import com.toyland.catalog.dto.AdminCatalogDtos.BrandRequest;
import com.toyland.catalog.dto.AdminCatalogDtos.CategoryRequest;
import com.toyland.catalog.dto.AdminCatalogDtos.FranchiseRequest;
import com.toyland.catalog.dto.CatalogDtos.BrandDto;
import com.toyland.catalog.dto.CatalogDtos.CategoryNode;
import com.toyland.catalog.dto.CatalogDtos.FranchiseDto;
import com.toyland.common.exception.ApiException;
import com.toyland.common.util.TextUtils;

/** CRUD danh muc, thuong hieu, nhan vat. */
@Service
@Transactional
public class AdminCatalogService {

	private final CategoryRepository categoryRepository;
	private final BrandRepository brandRepository;
	private final FranchiseRepository franchiseRepository;
	private final ProductRepository productRepository;
	private final AuditService audit;

	public AdminCatalogService(CategoryRepository categoryRepository, BrandRepository brandRepository,
			FranchiseRepository franchiseRepository, ProductRepository productRepository, AuditService audit) {
		this.categoryRepository = categoryRepository;
		this.brandRepository = brandRepository;
		this.franchiseRepository = franchiseRepository;
		this.productRepository = productRepository;
		this.audit = audit;
	}

	// ------------------------------------------------ danh muc
	@Transactional(readOnly = true)
	public List<CategoryNode> categoryTree() {
		return new CategoryTree(categoryRepository.findAllOrdered()).toNodes(false);
	}

	public CategoryNode saveCategory(Long id, CategoryRequest r) {
		Category c = id == null ? new Category() : categoryRepository.findById(id).orElseThrow(() -> ApiException.notFound("danh mục", id));
		String slug = TextUtils.isBlank(r.slug()) ? TextUtils.slugify(r.name()) : TextUtils.slugify(r.slug());
		if (id == null ? categoryRepository.existsBySlug(slug) : categoryRepository.existsBySlugAndIdNot(slug, id)) {
			throw ApiException.conflict("Slug danh mục đã tồn tại: " + slug);
		}
		Category parent = null;
		if (r.parentId() != null) {
			if (r.parentId().equals(id)) {
				throw ApiException.badRequest("Danh mục cha không hợp lệ");
			}
			parent = categoryRepository.findById(r.parentId()).orElseThrow(() -> ApiException.notFound("danh mục cha", r.parentId()));
			// chan vong lap: cha khong duoc la con chau cua chinh no
			for (Category p = parent; p != null; p = p.getParent()) {
				if (id != null && p.getId().equals(id)) {
					throw ApiException.badRequest("Không thể chọn danh mục con làm danh mục cha");
				}
			}
		}
		c.setName(r.name().trim());
		c.setSlug(slug);
		c.setParent(parent);
		c.setDescription(TextUtils.trimToNull(r.description()));
		c.setImageUrl(TextUtils.trimToNull(r.imageUrl()));
		c.setIcon(TextUtils.trimToNull(r.icon()));
		c.setSortOrder(r.sortOrder());
		c.setActive(r.active());
		c = categoryRepository.save(c);
		audit.log(id == null ? "CREATE" : "UPDATE", "CATEGORY", c.getId(), (id == null ? "Thêm" : "Sửa") + " danh mục " + c.getName());
		return new CategoryNode(c.getId(), c.getName(), c.getSlug(), c.getIcon(), c.getImageUrl(), c.getDescription(),
				parent == null ? null : parent.getId(), c.getSortOrder(), c.isActive(), List.of());
	}

	public void deleteCategory(Long id) {
		Category c = categoryRepository.findById(id).orElseThrow(() -> ApiException.notFound("danh mục", id));
		if (categoryRepository.countChildren(id) > 0) {
			throw ApiException.conflict("Không thể xoá: danh mục còn danh mục con");
		}
		if (productRepository.countByCategoryIds(List.of(id)) > 0) {
			throw ApiException.conflict("Không thể xoá: danh mục đang có sản phẩm");
		}
		if (productRepository.countAnyByCategory(id) > 0) {
			throw ApiException.conflict("Không thể xoá: danh mục còn sản phẩm đã ẩn (giữ lịch sử đơn hàng). Hãy tắt hiển thị danh mục thay vì xoá.");
		}
		categoryRepository.delete(c);
		audit.log("DELETE", "CATEGORY", id, "Xoá danh mục " + c.getName());
	}

	// ------------------------------------------------ thuong hieu
	public BrandDto saveBrand(Long id, BrandRequest r) {
		Brand b = id == null ? new Brand() : brandRepository.findById(id).orElseThrow(() -> ApiException.notFound("thương hiệu", id));
		String slug = TextUtils.isBlank(r.slug()) ? TextUtils.slugify(r.name()) : TextUtils.slugify(r.slug());
		if (id == null ? brandRepository.existsByNameIgnoreCase(r.name().trim()) : brandRepository.existsByNameIgnoreCaseAndIdNot(r.name().trim(), id)) {
			throw ApiException.conflict("Tên thương hiệu đã tồn tại");
		}
		if (id == null ? brandRepository.existsBySlug(slug) : brandRepository.existsBySlugAndIdNot(slug, id)) {
			throw ApiException.conflict("Slug thương hiệu đã tồn tại: " + slug);
		}
		b.setName(r.name().trim());
		b.setSlug(slug);
		b.setLogoUrl(TextUtils.trimToNull(r.logoUrl()));
		b.setCountry(TextUtils.trimToNull(r.country()));
		b.setDescription(TextUtils.trimToNull(r.description()));
		b = brandRepository.save(b);
		audit.log(id == null ? "CREATE" : "UPDATE", "BRAND", b.getId(), (id == null ? "Thêm" : "Sửa") + " thương hiệu " + b.getName());
		return ProductMapper.brand(b);
	}

	public void deleteBrand(Long id) {
		Brand b = brandRepository.findById(id).orElseThrow(() -> ApiException.notFound("thương hiệu", id));
		if (productRepository.countByBrand(id) > 0) {
			throw ApiException.conflict("Không thể xoá: thương hiệu đang có sản phẩm");
		}
		if (productRepository.countAnyByBrand(id) > 0) {
			throw ApiException.conflict("Không thể xoá: thương hiệu còn sản phẩm đã ẩn (giữ lịch sử đơn hàng)");
		}
		brandRepository.delete(b);
		audit.log("DELETE", "BRAND", id, "Xoá thương hiệu " + b.getName());
	}

	// ------------------------------------------------ nhan vat / chu de
	public FranchiseDto saveFranchise(Long id, FranchiseRequest r) {
		Franchise f = id == null ? new Franchise() : franchiseRepository.findById(id).orElseThrow(() -> ApiException.notFound("nhân vật", id));
		String slug = TextUtils.isBlank(r.slug()) ? TextUtils.slugify(r.name()) : TextUtils.slugify(r.slug());
		if (id == null ? franchiseRepository.existsByNameIgnoreCase(r.name().trim()) : franchiseRepository.existsByNameIgnoreCaseAndIdNot(r.name().trim(), id)) {
			throw ApiException.conflict("Tên nhân vật/chủ đề đã tồn tại");
		}
		if (id == null ? franchiseRepository.existsBySlug(slug) : franchiseRepository.existsBySlugAndIdNot(slug, id)) {
			throw ApiException.conflict("Slug đã tồn tại: " + slug);
		}
		f.setName(r.name().trim());
		f.setSlug(slug);
		f.setImageUrl(TextUtils.trimToNull(r.imageUrl()));
		f.setDescription(TextUtils.trimToNull(r.description()));
		f = franchiseRepository.save(f);
		audit.log(id == null ? "CREATE" : "UPDATE", "FRANCHISE", f.getId(), (id == null ? "Thêm" : "Sửa") + " nhân vật " + f.getName());
		return ProductMapper.franchise(f);
	}

	public void deleteFranchise(Long id) {
		Franchise f = franchiseRepository.findById(id).orElseThrow(() -> ApiException.notFound("nhân vật", id));
		if (productRepository.countByFranchise(id) > 0) {
			throw ApiException.conflict("Không thể xoá: đang có sản phẩm thuộc nhân vật/chủ đề này");
		}
		franchiseRepository.delete(f);
		audit.log("DELETE", "FRANCHISE", id, "Xoá nhân vật/chủ đề " + f.getName());
	}
}
