import { Component, DestroyRef, OnInit, computed, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { RouterLink } from '@angular/router';
import { ShopApi } from '../../core/api.service';
import { AGE_GROUPS, PASTELS } from '../../core/labels';
import { Banner, HomeData, ProductCard } from '../../core/models';
import { RecentlyViewedService } from '../../core/recently-viewed.service';
import { CountdownComponent } from '../../shared/countdown.component';
import { HeroToysComponent } from '../../shared/hero-toys.component';
import { ProductCardComponent } from '../../shared/product-card.component';

/** Nhan tuoi hien thi to tren the "mua theo do tuoi" */
const AGE_BIG: Record<string, { big: string; unit: string; hint: string }> = {
  BABY: { big: '0-1', unit: 'tuổi', hint: 'Xúc xắc, đồ gặm nướu' },
  TODDLER: { big: '1-3', unit: 'tuổi', hint: 'Khối xếp, xe đẩy' },
  PRESCHOOL: { big: '3-6', unit: 'tuổi', hint: 'Lắp ráp, nhập vai' },
  SCHOOL: { big: '6-12', unit: 'tuổi', hint: 'STEM, board game' },
  TEEN: { big: '12+', unit: 'tuổi', hint: 'Mô hình, sưu tầm' },
};

@Component({
  selector: 'tl-home',
  imports: [RouterLink, MatButtonModule, MatIconModule, ProductCardComponent, CountdownComponent, HeroToysComponent],
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss',
})
export class HomeComponent implements OnInit {
  private api = inject(ShopApi);
  private recent = inject(RecentlyViewedService);
  data = signal<HomeData | null>(null);
  recentProducts = signal<ProductCard[]>([]);
  slide = signal(0);
  paused = signal(false);
  ages = AGE_GROUPS;
  ageBig = AGE_BIG;
  pastels = PASTELS;

  banner = computed<Banner | null>(() => this.data()?.banners[this.slide()] ?? null);

  constructor() {
    const id = setInterval(() => {
      const n = this.data()?.banners.length ?? 0;
      if (n > 1 && !this.paused()) this.slide.update((s) => (s + 1) % n);
    }, 6000);
    inject(DestroyRef).onDestroy(() => clearInterval(id));
  }

  ngOnInit(): void {
    this.api.home().subscribe((d) => this.data.set(d));
    const ids = this.recent.ids();
    if (ids.length) this.api.byIds(ids).subscribe((p) => this.recentProducts.set(p));
  }

  go(i: number): void {
    const n = this.data()?.banners.length ?? 1;
    this.slide.set((i + n) % n);
  }

  pastel(i: number): string {
    return PASTELS[i % PASTELS.length];
  }

  /** Nen hero: pha mau banner voi trang -> pastel (claymorphism) */
  heroBg(b: Banner | null): string {
    const c = b?.bgColor || '#f0524f';
    return `radial-gradient(120% 90% at 85% 20%, color-mix(in srgb, ${c} 38%, #fff) 0%, color-mix(in srgb, ${c} 16%, #fff) 55%, #fff8f1 100%)`;
  }

  accent(b: Banner | null): string {
    return `color-mix(in srgb, ${b?.bgColor || '#f0524f'} 80%, #2b2350)`;
  }

  /** "/products?onSale=true" -> path + queryParams cho routerLink */
  link(url?: string | null): { path: string; query: Record<string, string> } {
    const [path, qs] = (url || '/products').split('?');
    const query: Record<string, string> = {};
    new URLSearchParams(qs || '').forEach((v, k) => (query[k] = v));
    return { path, query };
  }
}
