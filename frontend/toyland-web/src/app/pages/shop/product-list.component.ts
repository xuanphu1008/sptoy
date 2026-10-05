import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatChipsModule } from '@angular/material/chips';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatIconModule } from '@angular/material/icon';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSelectModule } from '@angular/material/select';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { Title } from '@angular/platform-browser';
import { ActivatedRoute, Params, Router, RouterLink } from '@angular/router';
import { ShopApi } from '../../core/api.service';
import { CatalogStore } from '../../core/catalog.store';
import { Facets, FacetItem, Page, ProductCard } from '../../core/models';
import { ProductCardComponent } from '../../shared/product-card.component';

type ListKey = 'brand' | 'franchise' | 'age' | 'gender';

const PRICE_RANGES = [
  { label: 'Dưới 200.000₫', min: null, max: 200000 },
  { label: '200.000₫ - 500.000₫', min: 200000, max: 500000 },
  { label: '500.000₫ - 1.000.000₫', min: 500000, max: 1000000 },
  { label: '1.000.000₫ - 2.000.000₫', min: 1000000, max: 2000000 },
  { label: 'Trên 2.000.000₫', min: 2000000, max: null },
];

@Component({
  selector: 'tl-product-list',
  imports: [
    RouterLink, FormsModule, MatButtonModule, MatCheckboxModule, MatChipsModule, MatExpansionModule, MatIconModule, MatPaginatorModule,
    MatSelectModule, MatSidenavModule, MatSlideToggleModule, MatFormFieldModule, MatInputModule, ProductCardComponent,
  ],
  templateUrl: './product-list.component.html',
  styleUrl: './product-list.component.scss',
})
export class ProductListComponent implements OnInit {
  private api = inject(ShopApi);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private title = inject(Title);
  catalog = inject(CatalogStore);

  params = signal<Params>({});
  result = signal<Page<ProductCard> | null>(null);
  facets = signal<Facets | null>(null);
  filtersOpen = signal(false);
  minPrice: number | null = null;
  maxPrice: number | null = null;
  priceRanges = PRICE_RANGES;
  sorts = [
    { value: 'newest', label: 'Mới nhất' }, { value: 'bestseller', label: 'Bán chạy' }, { value: 'price_asc', label: 'Giá thấp → cao' },
    { value: 'price_desc', label: 'Giá cao → thấp' }, { value: 'rating', label: 'Đánh giá cao' }, { value: 'name', label: 'Tên A → Z' },
  ];

  category = computed(() => this.catalog.findCategory(this.params()['category']));
  heading = computed(() => {
    const p = this.params();
    if (p['q']) return `Kết quả tìm kiếm cho "${p['q']}"`;
    if (this.category()) return this.category()!.name;
    if (p['onSale']) return 'Sản phẩm đang giảm giá';
    if (p['featured']) return 'Sản phẩm nổi bật';
    return 'Tất cả sản phẩm';
  });

  /** Cac bo loc dang ap dung -> hien dang chip co the xoa */
  activeChips = computed(() => {
    const p = this.params();
    const f = this.facets();
    const chips: { key: string; value?: string; label: string }[] = [];
    if (p['q']) chips.push({ key: 'q', label: `Từ khoá: ${p['q']}` });
    if (p['category']) chips.push({ key: 'category', label: this.category()?.name ?? p['category'] });
    const lookup = (list: FacetItem[] | undefined, v: string) => list?.find((x) => x.value === v)?.label ?? v;
    for (const v of this.list('brand')) chips.push({ key: 'brand', value: v, label: lookup(f?.brands, v) });
    for (const v of this.list('franchise')) chips.push({ key: 'franchise', value: v, label: lookup(f?.franchises, v) });
    for (const v of this.list('age')) chips.push({ key: 'age', value: v, label: lookup(f?.ages, v) });
    for (const v of this.list('gender')) chips.push({ key: 'gender', value: v, label: lookup(f?.genders, v) });
    if (p['minPrice'] || p['maxPrice']) {
      chips.push({ key: 'price', label: `Giá: ${p['minPrice'] ? (+p['minPrice']).toLocaleString('vi-VN') : '0'} - ${p['maxPrice'] ? (+p['maxPrice']).toLocaleString('vi-VN') + '₫' : '∞'}` });
    }
    if (p['inStock']) chips.push({ key: 'inStock', label: 'Còn hàng' });
    if (p['onSale']) chips.push({ key: 'onSale', label: 'Đang giảm giá' });
    if (p['featured']) chips.push({ key: 'featured', label: 'Nổi bật' });
    return chips;
  });

  ngOnInit(): void {
    this.catalog.load();
    this.route.queryParams.subscribe((p) => {
      this.params.set(p);
      this.minPrice = p['minPrice'] ? +p['minPrice'] : null;
      this.maxPrice = p['maxPrice'] ? +p['maxPrice'] : null;
      const query = { ...p, size: 24 };
      this.api.products(query).subscribe((r) => this.result.set(r));
      this.api.facets(query).subscribe((f) => this.facets.set(f));
      this.title.setTitle(`${this.heading()} - ToyLand`);
    });
  }

  list(key: ListKey): string[] {
    const v = this.params()[key];
    if (!v) return [];
    return (Array.isArray(v) ? v : String(v).split(',')).filter(Boolean);
  }

  has(key: ListKey, value: string): boolean {
    return this.list(key).includes(value);
  }

  toggle(key: ListKey, value: string): void {
    const cur = this.list(key);
    const next = cur.includes(value) ? cur.filter((x) => x !== value) : [...cur, value];
    this.navigate({ [key]: next.length ? next.join(',') : null });
  }

  setCategory(slug: string | null): void {
    this.navigate({ category: slug });
  }

  setFlag(key: 'inStock' | 'onSale', on: boolean): void {
    this.navigate({ [key]: on ? true : null });
  }

  applyPrice(min: number | null, max: number | null): void {
    this.navigate({ minPrice: min || null, maxPrice: max || null });
  }

  isRange(r: { min: number | null; max: number | null }): boolean {
    const p = this.params();
    return (p['minPrice'] ? +p['minPrice'] : null) === r.min && (p['maxPrice'] ? +p['maxPrice'] : null) === r.max;
  }

  removeChip(c: { key: string; value?: string }): void {
    if (c.value) return this.toggle(c.key as ListKey, c.value);
    if (c.key === 'price') return this.applyPrice(null, null);
    this.navigate({ [c.key]: null });
  }

  clearAll(): void {
    this.router.navigate(['/products'], { queryParams: this.params()['q'] ? { q: this.params()['q'] } : {} });
  }

  sortBy(sort: string): void {
    this.navigate({ sort }, false);
  }

  page(e: PageEvent): void {
    this.navigate({ page: e.pageIndex }, false);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  private navigate(patch: Params, resetPage = true): void {
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { ...patch, ...(resetPage ? { page: null } : {}) },
      queryParamsHandling: 'merge',
    });
  }
}
