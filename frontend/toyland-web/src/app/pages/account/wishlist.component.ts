import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { RouterLink } from '@angular/router';
import { ShopApi } from '../../core/api.service';
import { ProductCard } from '../../core/models';
import { WishlistStore } from '../../core/wishlist.store';
import { ProductCardComponent } from '../../shared/product-card.component';

@Component({
  selector: 'tl-wishlist',
  imports: [RouterLink, MatButtonModule, MatIconModule, ProductCardComponent],
  template: `
    <div class="card">
      <h2>Sản phẩm yêu thích ({{ visible().length }})</h2>
      @if (visible().length) {
        <div class="products-grid">@for (p of visible(); track p.id) { <tl-product-card [product]="p" /> }</div>
      } @else {
        <div class="empty"><mat-icon>favorite_border</mat-icon><p>Chưa có sản phẩm yêu thích. Bấm ♥ trên sản phẩm để lưu lại nhé!</p>
          <a mat-flat-button routerLink="/products">Khám phá đồ chơi</a></div>
      }
    </div>
  `,
})
export class WishlistComponent implements OnInit {
  private api = inject(ShopApi);
  private store = inject(WishlistStore);
  items = signal<ProductCard[]>([]);
  /** An ngay san pham vua bo tim */
  visible = computed(() => this.items().filter((p) => this.store.ids().has(p.id)));

  ngOnInit(): void {
    this.api.wishlist().subscribe((l) => this.items.set(l));
  }
}
