package com.toyland.catalog;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.toyland.common.util.TextUtils;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

/** Xay dung dieu kien tim kiem dong (JPA Criteria / Specification) tu ProductFilter. */
public final class ProductSpecs {

	private ProductSpecs() {
	}

	public static Specification<Product> of(ProductFilter f) {
		return (root, query, cb) -> {
			List<Predicate> ps = new ArrayList<>();
			ps.add(cb.isFalse(root.get("deleted")));

			if (f.publicOnly()) {
				ps.add(root.get("status").in(ProductStatus.ACTIVE, ProductStatus.COMING_SOON));
			} else if (f.statuses() != null && !f.statuses().isEmpty()) {
				ps.add(root.get("status").in(f.statuses()));
			}
			if (!TextUtils.isBlank(f.keyword())) {
				for (String token : TextUtils.normalize(f.keyword()).split(" ")) {
					if (!token.isBlank()) {
						ps.add(cb.like(root.get("searchText"), "%" + token + "%"));
					}
				}
			}
			if (f.categoryIds() != null && !f.categoryIds().isEmpty()) {
				ps.add(root.get("category").get("id").in(f.categoryIds()));
			}
			if (f.brandSlugs() != null && !f.brandSlugs().isEmpty()) {
				ps.add(root.get("brand").get("slug").in(f.brandSlugs()));
			}
			if (f.franchiseSlugs() != null && !f.franchiseSlugs().isEmpty() && query != null) {
				Subquery<Long> sq = query.subquery(Long.class);
				Root<Product> p2 = sq.from(Product.class);
				Join<Product, Franchise> fr = p2.join("franchises");
				sq.select(p2.get("id")).where(cb.equal(p2.get("id"), root.get("id")), fr.get("slug").in(f.franchiseSlugs()));
				ps.add(cb.exists(sq));
			}
			if (f.ages() != null && !f.ages().isEmpty()) {
				List<Predicate> or = new ArrayList<>();
				for (AgeGroup g : f.ages()) {
					or.add(cb.and(cb.greaterThanOrEqualTo(root.get("ageMinMonths"), g.getFromMonths()),
							cb.lessThan(root.get("ageMinMonths"), g.getToMonthsExclusive())));
				}
				ps.add(cb.or(or.toArray(new Predicate[0])));
			}
			if (f.genders() != null && !f.genders().isEmpty()) {
				ps.add(root.get("gender").in(f.genders()));
			}
			if (f.minPrice() != null) {
				ps.add(cb.greaterThanOrEqualTo(root.get("minPrice"), f.minPrice()));
			}
			if (f.maxPrice() != null) {
				ps.add(cb.lessThanOrEqualTo(root.get("minPrice"), f.maxPrice()));
			}
			if (Boolean.TRUE.equals(f.inStock())) {
				ps.add(cb.greaterThan(root.get("totalStock"), 0));
			}
			if (Boolean.TRUE.equals(f.featured())) {
				ps.add(cb.isTrue(root.get("featured")));
			}
			if (Boolean.TRUE.equals(f.onSale()) && query != null) {
				ps.add(onSale(root, query, cb, f.saleScope()));
			}
			return cb.and(ps.toArray(new Predicate[0]));
		};
	}

	private static Predicate onSale(Root<Product> root, jakarta.persistence.criteria.CommonAbstractCriteria query,
			CriteriaBuilder cb, ProductFilter.SaleScope scope) {
		List<Predicate> or = new ArrayList<>();
		// Bien the co gia goc > gia ban
		Subquery<Long> sq = query.subquery(Long.class);
		Root<ProductVariant> v = sq.from(ProductVariant.class);
		sq.select(v.get("id")).where(cb.equal(v.get("product").get("id"), root.get("id")), cb.isTrue(v.get("active")),
				cb.isNotNull(v.get("compareAtPrice")), cb.greaterThan(v.get("compareAtPrice"), v.get("price")));
		or.add(cb.exists(sq));
		if (scope != null) {
			if (scope.all()) {
				return cb.conjunction();
			}
			if (!scope.categoryIds().isEmpty()) {
				// scope.categoryIds() da gom ca danh muc con (service mo rong truoc)
				or.add(root.get("category").get("id").in(scope.categoryIds()));
			}
			if (!scope.brandIds().isEmpty()) {
				Join<Product, Brand> b = root.join("brand", JoinType.LEFT);
				or.add(b.get("id").in(scope.brandIds()));
			}
			if (!scope.productIds().isEmpty()) {
				or.add(root.get("id").in(scope.productIds()));
			}
			if (!scope.franchiseIds().isEmpty()) {
				Subquery<Long> fq = query.subquery(Long.class);
				Root<Product> p2 = fq.from(Product.class);
				Join<Product, Franchise> fr = p2.join("franchises");
				fq.select(p2.get("id")).where(cb.equal(p2.get("id"), root.get("id")), fr.get("id").in(scope.franchiseIds()));
				or.add(cb.exists(fq));
			}
		}
		return cb.or(or.toArray(new Predicate[0]));
	}
}
