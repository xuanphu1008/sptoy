import { Injectable, inject, signal } from '@angular/core';
import { ShopApi } from './api.service';
import { Brand, CategoryNode, Franchise } from './models';

/** Cache danh muc / thuong hieu / nhan vat (tai 1 lan, dung cho menu va bo loc) */
@Injectable({ providedIn: 'root' })
export class CatalogStore {
  private api = inject(ShopApi);
  readonly categories = signal<CategoryNode[]>([]);
  readonly brands = signal<Brand[]>([]);
  readonly franchises = signal<Franchise[]>([]);
  private loaded = false;

  load(): void {
    if (this.loaded) return;
    this.loaded = true;
    this.api.categories().subscribe({ next: (c) => this.categories.set(c), error: () => (this.loaded = false) });
    this.api.brands().subscribe((b) => this.brands.set(b));
    this.api.franchises().subscribe((f) => this.franchises.set(f));
  }

  /** Tim danh muc theo slug trong cay */
  findCategory(slug: string | null | undefined, nodes = this.categories()): CategoryNode | null {
    if (!slug) return null;
    for (const n of nodes) {
      if (n.slug === slug) return n;
      const c = this.findCategory(slug, n.children ?? []);
      if (c) return c;
    }
    return null;
  }
}
