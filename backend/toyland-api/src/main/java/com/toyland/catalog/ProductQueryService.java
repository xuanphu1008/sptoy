package com.toyland.catalog;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.toyland.catalog.dto.CatalogDtos.FacetItem;
import com.toyland.catalog.dto.CatalogDtos.Facets;
import com.toyland.catalog.dto.CatalogDtos.ProductCard;
import com.toyland.catalog.dto.CatalogDtos.ProductDetail;
import com.toyland.catalog.dto.CatalogDtos.RatingSummary;
import com.toyland.catalog.dto.CatalogDtos.Suggestion;
import com.toyland.common.dto.PageResponse;
import com.toyland.common.exception.ApiException;
import com.toyland.common.util.TextUtils;
import com.toyland.promotion.Campaign;
import com.toyland.promotion.CampaignScope;
import com.toyland.promotion.PricingService;
import com.toyland.review.ReviewStatsApi;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

/** Truy van san pham cho trang cua hang: tim kiem, loc, dem facet, goi y, chi tiet. */
@Service
@Transactional(readOnly = true)
public class ProductQueryService {

	private final ProductRepository productRepository;
	private final CategoryRepository categoryRepository;
	private final ReviewStatsApi reviewStats;
	private final PricingService pricingService;
	private final ProductMapper mapper;
	private final EntityManager em;

	public ProductQueryService(ProductRepository productRepository, CategoryRepository categoryRepository,
			ReviewStatsApi reviewStats, PricingService pricingService, ProductMapper mapper, EntityManager em) {
		this.productRepository = productRepository;
		this.categoryRepository = categoryRepository;
		this.reviewStats = reviewStats;
		this.pricingService = pricingService;
		this.mapper = mapper;
		this.em = em;
	}

	/** Tham so tim kiem dang chuoi (tu query string). */
	public record SearchParams(String keyword, String category, List<String> brands, List<String> franchises,
			List<String> ages, List<String> genders, BigDecimal minPrice, BigDecimal maxPrice, Boolean inStock,
			Boolean onSale, Boolean featured) {
	}

	public CategoryTree tree() {
		return new CategoryTree(categoryRepository.findAllOrdered());
	}

	public ProductFilter toFilter(SearchParams sp, CategoryTree tree, List<Campaign> campaigns) {
		List<Long> catIds = null;
		if (!TextUtils.isBlank(sp.category())) {
			Category c = tree.bySlug(sp.category());
			catIds = c == null ? List.of(-1L) : tree.withDescendants(c.getId());
		}
		List<AgeGroup> ages = sp.ages() == null ? null
				: sp.ages().stream().map(a -> parseEnum(AgeGroup.class, a)).filter(a -> a != null).toList();
		List<Gender> genders = sp.genders() == null ? null
				: sp.genders().stream().map(g -> parseEnum(Gender.class, g)).filter(g -> g != null).toList();
		return new ProductFilter(sp.keyword(), catIds, lower(sp.brands()), lower(sp.franchises()), ages, genders,
				sp.minPrice(), sp.maxPrice(), sp.inStock(), sp.onSale(), sp.featured(), null, true, saleScope(campaigns, tree));
	}

	public PageResponse<ProductCard> search(SearchParams sp, int page, int size, String sort) {
		List<Campaign> campaigns = pricingService.runningCampaigns();
		CategoryTree tree = tree();
		ProductFilter f = toFilter(sp, tree, campaigns);
		Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 60), sortOf(sort));
		Page<Product> result = productRepository.findAll(ProductSpecs.of(f), pageable);
		return PageResponse.of(result, p -> mapper.card(p, campaigns));
	}

	public static Sort sortOf(String sort) {
		if (sort == null) {
			sort = "newest";
		}
		return switch (sort) {
		case "price_asc" -> Sort.by(Sort.Direction.ASC, "minPrice").and(Sort.by("id"));
		case "price_desc" -> Sort.by(Sort.Direction.DESC, "minPrice").and(Sort.by("id"));
		case "bestseller" -> Sort.by(Sort.Direction.DESC, "soldCount").and(Sort.by(Sort.Direction.DESC, "id"));
		case "rating" -> Sort.by(Sort.Direction.DESC, "avgRating", "reviewCount").and(Sort.by(Sort.Direction.DESC, "id"));
		case "name" -> Sort.by(Sort.Direction.ASC, "name");
		default -> Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by(Sort.Direction.DESC, "id"));
		};
	}

	/** Dem so san pham theo tung lua chon cua bo loc (bo qua chinh chieu dang dem). */
	public Facets facets(SearchParams sp) {
		List<Campaign> campaigns = pricingService.runningCampaigns();
		CategoryTree tree = tree();
		ProductFilter f = toFilter(sp, tree, campaigns);
		CriteriaBuilder cb = em.getCriteriaBuilder();

		// --- danh muc: dem theo danh muc la roi cong don len danh muc goc
		Map<Long, Long> byLeaf = groupCount(f.withoutCategories(), "category");
		Map<Long, Long> byRoot = new LinkedHashMap<>();
		byLeaf.forEach((id, cnt) -> {
			Category root = tree.rootOf(id);
			if (root != null) {
				byRoot.merge(root.getId(), cnt, Long::sum);
			}
		});
		List<FacetItem> cats = new ArrayList<>();
		// neu dang chon 1 danh muc goc -> hien danh muc con cua no
		Category selected = TextUtils.isBlank(sp.category()) ? null : tree.bySlug(sp.category());
		if (selected != null) {
			Category root = tree.rootOf(selected.getId());
			for (Long childId : tree.withDescendants(root.getId())) {
				Category c = tree.get(childId);
				long cnt = tree.withDescendants(childId).stream().mapToLong(id -> byLeaf.getOrDefault(id, 0L)).sum();
				cats.add(new FacetItem(c.getSlug(), (c.getParent() == null ? "" : "— ") + c.getName(), cnt));
			}
		} else {
			for (Category r : tree.roots()) {
				if (r.isActive()) {
					cats.add(new FacetItem(r.getSlug(), r.getName(), byRoot.getOrDefault(r.getId(), 0L)));
				}
			}
		}

		// --- thuong hieu
		List<FacetItem> brands = new ArrayList<>();
		{
			CriteriaQuery<Object[]> q = cb.createQuery(Object[].class);
			Root<Product> root = q.from(Product.class);
			Join<Product, Brand> b = root.join("brand");
			q.multiselect(b.get("slug"), b.get("name"), cb.count(root));
			q.where(where(f.withoutBrands(), root, q, cb));
			q.groupBy(b.get("slug"), b.get("name"));
			for (Object[] r : em.createQuery(q).getResultList()) {
				brands.add(new FacetItem((String) r[0], (String) r[1], ((Number) r[2]).longValue()));
			}
			brands.sort(Comparator.comparing(FacetItem::label));
		}
		// --- nhan vat
		List<FacetItem> franchises = new ArrayList<>();
		{
			CriteriaQuery<Object[]> q = cb.createQuery(Object[].class);
			Root<Product> root = q.from(Product.class);
			Join<Product, Franchise> fr = root.join("franchises");
			q.multiselect(fr.get("slug"), fr.get("name"), cb.count(root));
			q.where(where(f.withoutFranchises(), root, q, cb));
			q.groupBy(fr.get("slug"), fr.get("name"));
			for (Object[] r : em.createQuery(q).getResultList()) {
				franchises.add(new FacetItem((String) r[0], (String) r[1], ((Number) r[2]).longValue()));
			}
			franchises.sort(Comparator.comparing(FacetItem::label));
		}
		// --- do tuoi
		List<FacetItem> ages = new ArrayList<>();
		for (AgeGroup g : AgeGroup.values()) {
			ages.add(new FacetItem(g.name(), g.getLabel(), productRepository.count(ProductSpecs.of(f.withAges(List.of(g))))));
		}
		// --- gioi tinh
		List<FacetItem> genders = new ArrayList<>();
		{
			CriteriaQuery<Object[]> q = cb.createQuery(Object[].class);
			Root<Product> root = q.from(Product.class);
			q.multiselect(root.get("gender"), cb.count(root));
			q.where(where(f.withoutGenders(), root, q, cb));
			q.groupBy(root.get("gender"));
			Map<Gender, Long> m = new HashMap<>();
			for (Object[] r : em.createQuery(q).getResultList()) {
				m.put((Gender) r[0], ((Number) r[1]).longValue());
			}
			genders.add(new FacetItem("BOY", "Bé trai", m.getOrDefault(Gender.BOY, 0L)));
			genders.add(new FacetItem("GIRL", "Bé gái", m.getOrDefault(Gender.GIRL, 0L)));
			genders.add(new FacetItem("UNISEX", "Cả bé trai & bé gái", m.getOrDefault(Gender.UNISEX, 0L)));
		}
		return new Facets(cats, brands, franchises, ages, genders);
	}

	private Map<Long, Long> groupCount(ProductFilter f, String association) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Object[]> q = cb.createQuery(Object[].class);
		Root<Product> root = q.from(Product.class);
		Join<Product, ?> j = root.join(association);
		q.multiselect(j.get("id"), cb.count(root));
		q.where(where(f, root, q, cb));
		q.groupBy(j.get("id"));
		Map<Long, Long> m = new HashMap<>();
		for (Object[] r : em.createQuery(q).getResultList()) {
			m.put(((Number) r[0]).longValue(), ((Number) r[1]).longValue());
		}
		return m;
	}

	private Predicate where(ProductFilter f, Root<Product> root, CriteriaQuery<?> q, CriteriaBuilder cb) {
		Specification<Product> spec = ProductSpecs.of(f);
		Predicate p = spec.toPredicate(root, q, cb);
		return p == null ? cb.conjunction() : p;
	}

	/** Goi y khi go o tim kiem. */
	public Suggestion suggest(String q) {
		if (TextUtils.isBlank(q) || q.trim().length() < 2) {
			return new Suggestion(List.of(), List.of(), List.of());
		}
		List<Campaign> campaigns = pricingService.runningCampaigns();
		ProductFilter f = new ProductFilter(q, null, null, null, null, null, null, null, null, null, null, null, true, null);
		List<Product> products = productRepository.findAll(ProductSpecs.of(f), PageRequest.of(0, 6, sortOf("bestseller"))).getContent();
		String nq = TextUtils.normalize(q);
		CategoryTree tree = tree();
		List<FacetItem> cats = new ArrayList<>();
		for (Long id : tree.roots().stream().flatMap(r -> tree.withDescendants(r.getId()).stream()).toList()) {
			Category c = tree.get(id);
			if (c.isActive() && TextUtils.normalize(c.getName()).contains(nq)) {
				cats.add(new FacetItem(c.getSlug(), c.getName(), 0));
			}
		}
		List<FacetItem> brands = em.createQuery("SELECT b FROM Brand b ORDER BY b.name", Brand.class).getResultList().stream()
				.filter(b -> TextUtils.normalize(b.getName()).contains(nq)).map(b -> new FacetItem(b.getSlug(), b.getName(), 0)).toList();
		return new Suggestion(mapper.cards(products, campaigns), cats.stream().limit(5).toList(), brands.stream().limit(5).toList());
	}

	public ProductDetail detailBySlug(String slug) {
		Product p = productRepository.findBySlugAndDeletedFalse(slug).orElseThrow(() -> ApiException.notFound("sản phẩm", slug));
		return detail(p);
	}

	public ProductDetail detailById(Long id) {
		Product p = productRepository.findByIdAndDeletedFalse(id).orElseThrow(() -> ApiException.notFound("sản phẩm", id));
		return detail(p);
	}

	private ProductDetail detail(Product p) {
		if (p.getStatus() == ProductStatus.DRAFT || p.getStatus() == ProductStatus.INACTIVE) {
			throw ApiException.notFound("sản phẩm", p.getSlug());
		}
		List<Campaign> campaigns = pricingService.runningCampaigns();
		CategoryTree tree = tree();
		Category root = tree.rootOf(p.getCategory().getId());
		List<Long> catIds = root == null ? List.of(p.getCategory().getId()) : tree.withDescendants(root.getId());
		List<Product> related = productRepository.findRelated(catIds, p.getId(), PageRequest.of(0, 8));
		return mapper.detail(p, tree, campaigns, mapper.cards(related, campaigns));
	}

	public List<ProductCard> cardsByIds(List<Long> ids) {
		if (ids == null || ids.isEmpty()) {
			return List.of();
		}
		List<Campaign> campaigns = pricingService.runningCampaigns();
		Map<Long, Product> byId = new HashMap<>();
		productRepository.findAllActiveByIds(ids).forEach(p -> byId.put(p.getId(), p));
		List<ProductCard> out = new ArrayList<>();
		for (Long id : ids) {
			Product p = byId.get(id);
			if (p != null && (p.getStatus() == ProductStatus.ACTIVE || p.getStatus() == ProductStatus.COMING_SOON)) {
				out.add(mapper.card(p, campaigns));
			}
		}
		return out;
	}

	public List<ProductCard> list(ProductFilter f, String sort, int limit, List<Campaign> campaigns) {
		return mapper.cards(productRepository.findAll(ProductSpecs.of(f), PageRequest.of(0, limit, sortOf(sort))).getContent(), campaigns);
	}

	public RatingSummary ratingSummary(Long productId) {
		Map<Integer, Long> counts = new LinkedHashMap<>();
		for (int i = 5; i >= 1; i--) {
			counts.put(i, 0L);
		}
		long total = 0;
		long sum = 0;
		for (Object[] r : reviewStats.countByRating(productId)) {
			int rating = ((Number) r[0]).intValue();
			long c = ((Number) r[1]).longValue();
			counts.put(rating, c);
			total += c;
			sum += rating * c;
		}
		BigDecimal avg = total == 0 ? BigDecimal.ZERO
				: BigDecimal.valueOf(sum).divide(BigDecimal.valueOf(total), 1, RoundingMode.HALF_UP);
		return new RatingSummary(avg, total, counts, reviewStats.countWithImages(productId));
	}

	/** Pham vi cac chuong trinh giam gia dang chay, danh muc da mo rong ca danh muc con. */
	public ProductFilter.SaleScope saleScope(List<Campaign> campaigns, CategoryTree tree) {
		boolean all = false;
		List<Long> cats = new ArrayList<>();
		List<Long> brands = new ArrayList<>();
		List<Long> frs = new ArrayList<>();
		List<Long> prods = new ArrayList<>();
		for (Campaign c : campaigns) {
			if (c.getScopeType() == CampaignScope.ALL) {
				all = true;
			} else if (c.getScopeId() != null) {
				switch (c.getScopeType()) {
				case CATEGORY -> cats.addAll(tree.withDescendants(c.getScopeId()));
				case BRAND -> brands.add(c.getScopeId());
				case FRANCHISE -> frs.add(c.getScopeId());
				case PRODUCT -> prods.add(c.getScopeId());
				default -> {
				}
				}
			}
		}
		return new ProductFilter.SaleScope(all, cats, brands, frs, prods);
	}

	private static List<String> lower(List<String> list) {
		return list == null ? null : list.stream().filter(s -> !TextUtils.isBlank(s)).map(s -> s.trim().toLowerCase()).toList();
	}

	private static <E extends Enum<E>> E parseEnum(Class<E> type, String v) {
		try {
			return Enum.valueOf(type, v.trim().toUpperCase());
		} catch (Exception e) {
			return null;
		}
	}
}
