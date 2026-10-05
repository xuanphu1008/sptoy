package com.toyland.catalog;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.toyland.catalog.dto.CatalogDtos.CategoryNode;

/** Cay danh muc dung trong bo nho (so luong danh muc it). */
public class CategoryTree {

	private final Map<Long, Category> byId = new HashMap<>();
	private final Map<Long, List<Category>> children = new HashMap<>();
	private final List<Category> roots = new ArrayList<>();

	public CategoryTree(List<Category> input) {
		List<Category> all = new ArrayList<>(input);
		all.sort(Comparator.comparingInt(Category::getSortOrder).thenComparing(Category::getId));
		for (Category c : all) {
			byId.put(c.getId(), c);
		}
		for (Category c : all) {
			Long pid = c.getParent() == null ? null : c.getParent().getId();
			if (pid == null) {
				roots.add(c);
			} else {
				children.computeIfAbsent(pid, k -> new ArrayList<>()).add(c);
			}
		}
	}

	public Category get(Long id) {
		return byId.get(id);
	}

	public Category bySlug(String slug) {
		return byId.values().stream().filter(c -> c.getSlug().equals(slug)).findFirst().orElse(null);
	}

	/** id + tat ca con chau. */
	public List<Long> withDescendants(Long id) {
		List<Long> result = new ArrayList<>();
		collect(id, result);
		return result;
	}

	private void collect(Long id, List<Long> acc) {
		acc.add(id);
		for (Category c : children.getOrDefault(id, List.of())) {
			collect(c.getId(), acc);
		}
	}

	/** Duong dan tu goc den danh muc (breadcrumb). */
	public List<Category> pathTo(Long id) {
		List<Category> path = new ArrayList<>();
		for (Category c = byId.get(id); c != null; c = c.getParent() == null ? null : byId.get(c.getParent().getId())) {
			path.add(0, c);
		}
		return path;
	}

	public Category rootOf(Long id) {
		List<Category> p = pathTo(id);
		return p.isEmpty() ? null : p.get(0);
	}

	public List<CategoryNode> toNodes(boolean activeOnly) {
		return toNodes(roots, activeOnly);
	}

	private List<CategoryNode> toNodes(List<Category> list, boolean activeOnly) {
		List<CategoryNode> nodes = new ArrayList<>();
		for (Category c : list) {
			if (activeOnly && !c.isActive()) {
				continue;
			}
			nodes.add(new CategoryNode(c.getId(), c.getName(), c.getSlug(), c.getIcon(), c.getImageUrl(), c.getDescription(),
					c.getParent() == null ? null : c.getParent().getId(), c.getSortOrder(), c.isActive(),
					toNodes(children.getOrDefault(c.getId(), List.of()), activeOnly)));
		}
		return nodes;
	}

	public List<Category> roots() {
		return roots;
	}
}
