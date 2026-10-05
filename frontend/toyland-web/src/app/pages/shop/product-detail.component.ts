import { Component, computed, effect, inject, input, signal, untracked } from '@angular/core';
import { DatePipe, DecimalPipe } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatTabsModule } from '@angular/material/tabs';
import { MatTooltipModule } from '@angular/material/tooltip';
import { Meta, Title } from '@angular/platform-browser';
import { Router, RouterLink } from '@angular/router';
import { ShopApi } from '../../core/api.service';
import { CartStore, MAX_QTY } from '../../core/cart.store';
import { GENDER, PLACEHOLDER_IMG } from '../../core/labels';
import { Page, ProductDetail, RatingSummary, Review, Variant } from '../../core/models';
import { NotifyService, errorMessage } from '../../core/notify.service';
import { RecentlyViewedService } from '../../core/recently-viewed.service';
import { VndPipe } from '../../core/vnd.pipe';
import { WishlistStore } from '../../core/wishlist.store';
import { ProductCardComponent } from '../../shared/product-card.component';
import { StarsComponent } from '../../shared/stars.component';

@Component({
  selector: 'tl-product-detail',
  imports: [RouterLink, DatePipe, DecimalPipe, MatButtonModule, MatIconModule, MatTabsModule, MatTooltipModule, MatPaginatorModule,
    VndPipe, StarsComponent, ProductCardComponent],
  templateUrl: './product-detail.component.html',
  styleUrl: './product-detail.component.scss',
})
export class ProductDetailComponent {
  /** lay tu route /products/:slug (withComponentInputBinding) */
  slug = input.required<string>();

  private api = inject(ShopApi);
  cart = inject(CartStore);
  maxQty = MAX_QTY;
  private notify = inject(NotifyService);
  private router = inject(Router);
  private title = inject(Title);
  private meta = inject(Meta);
  private recent = inject(RecentlyViewedService);
  wishlist = inject(WishlistStore);

  p = signal<ProductDetail | null>(null);
  error = signal<string | null>(null);
  sel1 = signal<string | null>(null);
  sel2 = signal<string | null>(null);
  qty = signal(1);
  activeImage = signal<string | null>(null);
  summary = signal<RatingSummary | null>(null);
  reviews = signal<Page<Review> | null>(null);
  reviewFilter = signal<{ rating: number | null; withImages: boolean }>({ rating: null, withImages: false });
  zoomImg = signal<string | null>(null);
  gender = GENDER;
  ph = PLACEHOLDER_IMG;
  starOptions = [5, 4, 3, 2, 1];

  /** Bien the ung voi lua chon hien tai */
  variant = computed<Variant | null>(() => {
    const p = this.p();
    if (!p) return null;
    if (!p.option1Name) return p.variants[0] ?? null;
    return p.variants.find((v) => v.option1 === this.sel1() && (!p.option2Name || v.option2 === this.sel2())) ?? null;
  });

  images = computed(() => {
    const p = this.p();
    if (!p) return [];
    const list = p.images.map((i) => i.url);
    for (const v of p.variants) if (v.imageUrl && !list.includes(v.imageUrl)) list.push(v.imageUrl);
    return list.length ? list : [PLACEHOLDER_IMG];
  });

  priceRange = computed(() => {
    const prices = (this.p()?.variants ?? []).map((v) => v.price);
    return prices.length ? { min: Math.min(...prices), max: Math.max(...prices) } : { min: 0, max: 0 };
  });

  canBuy = computed(() => {
    const v = this.variant();
    return this.p()?.status === 'ACTIVE' && !!v && v.inStock && this.qty() <= v.stock;
  });
  liked = computed(() => !!this.p() && this.wishlist.ids().has(this.p()!.id));
  dims = computed(() => {
    const p = this.p();
    return p?.lengthCm && p.widthCm && p.heightCm ? `${p.lengthCm} × ${p.widthCm} × ${p.heightCm} cm` : null;
  });
  starRows = computed(() => {
    const s = this.summary();
    if (!s) return [];
    return [5, 4, 3, 2, 1].map((star) => {
      const c = s.counts[star] ?? 0;
      return { star, count: c, pct: s.total ? (c / s.total) * 100 : 0 };
    });
  });

  constructor() {
    effect(() => {
      const slug = this.slug();
      untracked(() => this.load(slug));
    });
  }

  private load(slug: string): void {
    this.p.set(null);
    this.error.set(null);
    this.api.productBySlug(slug).subscribe({
      next: (p) => {
        this.p.set(p);
        this.qty.set(1);
        const first = p.variants.find((v) => v.inStock) ?? p.variants[0];
        this.sel1.set(first?.option1 ?? null);
        this.sel2.set(first?.option2 ?? null);
        this.activeImage.set(first?.imageUrl || p.images[0]?.url || null);
        this.title.setTitle(p.metaTitle || `${p.name} - ToyLand`);
        this.meta.updateTag({ name: 'description', content: p.metaDescription || p.shortDescription || p.name });
        this.recent.add(p.id);
        this.api.ratingSummary(p.id).subscribe((s) => this.summary.set(s));
        this.reviewFilter.set({ rating: null, withImages: false });
        this.loadReviews(0);
      },
      error: (e) => this.error.set(errorMessage(e)),
    });
  }

  loadReviews(page: number): void {
    const p = this.p();
    if (!p) return;
    const f = this.reviewFilter();
    this.api.reviews(p.id, { rating: f.rating, withImages: f.withImages || null, page, size: 5 }).subscribe((r) => this.reviews.set(r));
  }

  filterReviews(rating: number | null, withImages = false): void {
    this.reviewFilter.set({ rating, withImages });
    this.loadReviews(0);
  }

  reviewPage(e: PageEvent): void {
    this.loadReviews(e.pageIndex);
  }

  /** Gia tri thuoc tinh co ton tai / con hang khi ket hop voi lua chon con lai? */
  optionState(which: 1 | 2, value: string): 'ok' | 'oos' | 'none' {
    const p = this.p()!;
    const match = p.variants.filter((v) =>
      which === 1
        ? v.option1 === value && (!p.option2Name || !this.sel2() || v.option2 === this.sel2())
        : v.option2 === value && (!this.sel1() || v.option1 === this.sel1()),
    );
    if (!match.length) {
      // Van cho chon neu gia tri ton tai voi to hop khac (se tu doi thuoc tinh con lai)
      const any = p.variants.filter((v) => (which === 1 ? v.option1 === value : v.option2 === value));
      if (!any.length) return 'none';
      return any.some((v) => v.inStock) ? 'ok' : 'oos';
    }
    return match.some((v) => v.inStock) ? 'ok' : 'oos';
  }

  choose(which: 1 | 2, value: string): void {
    const p = this.p()!;
    if (which === 1) {
      this.sel1.set(value);
      // Neu to hop (value, sel2) khong ton tai -> tu chon option2 hop le dau tien
      if (p.option2Name && !p.variants.some((v) => v.option1 === value && v.option2 === this.sel2())) {
        const v = p.variants.find((x) => x.option1 === value && x.inStock) ?? p.variants.find((x) => x.option1 === value);
        this.sel2.set(v?.option2 ?? null);
      }
    } else {
      this.sel2.set(value);
      if (!p.variants.some((v) => v.option2 === value && v.option1 === this.sel1())) {
        const v = p.variants.find((x) => x.option2 === value && x.inStock) ?? p.variants.find((x) => x.option2 === value);
        this.sel1.set(v?.option1 ?? null);
      }
    }
    this.qty.set(1);
    const img = this.variant()?.imageUrl;
    if (img) this.activeImage.set(img);
  }

  changeQty(d: number): void {
    const max = Math.min(MAX_QTY, this.variant()?.stock ?? 1);
    this.qty.update((q) => Math.min(max, Math.max(1, q + d)));
  }

  addToCart(buyNow = false): void {
    const v = this.variant();
    if (!v) {
      this.notify.error('Vui lòng chọn phân loại sản phẩm');
      return;
    }
    this.cart.add(v.id, this.qty()).subscribe({
      next: () => {
        if (buyNow) this.router.navigate(['/cart']);
        else {
          this.notify.success('Đã thêm vào giỏ hàng');
          this.cart.drawerOpen.set(true);
        }
      },
      error: (e) => this.notify.error(e),
    });
  }

  share(): void {
    navigator.clipboard?.writeText(location.href).then(() => this.notify.info('Đã sao chép liên kết sản phẩm'));
  }
}
