import { Component, computed, inject, input, signal } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { Router, RouterLink } from '@angular/router';
import { CartStore } from '../core/cart.store';
import { PLACEHOLDER_IMG } from '../core/labels';
import { ProductCard } from '../core/models';
import { NotifyService } from '../core/notify.service';
import { VndPipe } from '../core/vnd.pipe';
import { WishlistStore } from '../core/wishlist.store';
import { StarsComponent } from './stars.component';

/** The san pham dung chung (trang chu, danh sach, lien quan, yeu thich) */
@Component({
  selector: 'tl-product-card',
  imports: [RouterLink, MatIconModule, MatTooltipModule, VndPipe, StarsComponent],
  template: `
    @let p = product();
    <article class="pc" [class.oos]="!p.inStock">
      <a class="img" [routerLink]="['/products', p.slug]" [attr.aria-label]="p.name">
        <img [src]="p.imageUrl || placeholder" [alt]="p.name" loading="lazy" class="main" />
        @if (p.hoverImageUrl) {
          <img [src]="p.hoverImageUrl" alt="" loading="lazy" class="hover" />
        }
        <div class="badges">
          @if (p.discountPercent > 0) { <span class="b sale">-{{ p.discountPercent }}%</span> }
          @if (p.isNew) { <span class="b new">Mới</span> }
          @if (p.onlineExclusive) { <span class="b online">Online</span> }
          @if (p.badge) { <span class="b promo">{{ p.badge }}</span> }
        </div>
        @if (p.status === 'COMING_SOON') {
          <div class="ribbon">Sắp về hàng</div>
        } @else if (!p.inStock) {
          <div class="ribbon">Tạm hết hàng</div>
        }
      </a>
      <button type="button" class="heart" [class.on]="liked()" [class.pop]="popping()" (click)="like()"
              [matTooltip]="liked() ? 'Bỏ yêu thích' : 'Yêu thích'" [attr.aria-pressed]="liked()" aria-label="Yêu thích">
        <mat-icon>{{ liked() ? 'favorite' : 'favorite_border' }}</mat-icon>
      </button>
      <div class="body">
        <div class="meta">
          @if (p.brandName) { <span class="brand">{{ p.brandName }}</span> }
          <span class="age"><mat-icon>child_care</mat-icon>{{ p.ageLabel }}</span>
        </div>
        <a class="name clamp-2" [routerLink]="['/products', p.slug]" [title]="p.name">{{ p.name }}</a>
        <div class="rating">
          @if (p.reviewCount > 0) {
            <tl-stars [value]="p.avgRating" [size]="14" /> <span>({{ p.reviewCount }})</span>
          }
          @if (p.soldCount > 0) { <span class="sold">Đã bán {{ sold() }}</span> }
        </div>
        <div class="foot">
          <div class="prices">
            <span class="price">{{ p.price | vnd }}</span>
            @if (p.originalPrice && p.originalPrice > p.price) { <span class="price-old">{{ p.originalPrice | vnd }}</span> }
          </div>
          <button type="button" class="add" [disabled]="!p.inStock" (click)="quickAdd()"
                  [matTooltip]="p.variantCount > 1 ? 'Chọn mẫu' : 'Thêm vào giỏ'" [attr.aria-label]="p.variantCount > 1 ? 'Chọn mẫu' : 'Thêm vào giỏ'">
            <mat-icon>{{ p.variantCount > 1 ? 'tune' : 'add_shopping_cart' }}</mat-icon>
          </button>
        </div>
      </div>
    </article>
  `,
  styles: `
    .pc { position: relative; background: var(--tl-card); border-radius: 26px; box-shadow: var(--clay-sm); height: 100%; padding: 8px;
      display: flex; flex-direction: column; transition: box-shadow .3s ease-out, transform .3s var(--ease-bounce); }
    .pc:hover { box-shadow: var(--clay-hover); transform: translateY(-6px); }
    .img { position: relative; display: block; aspect-ratio: 1; background: var(--p-cream); border-radius: 20px; overflow: hidden; }
    .img img { width: 100%; height: 100%; object-fit: cover; transition: opacity .35s, transform .5s var(--ease-bounce); }
    .img .hover { position: absolute; inset: 0; opacity: 0; }
    .pc:hover .img .hover { opacity: 1; }
    .pc:hover .img .main { transform: scale(1.06) rotate(-1deg); }
    .oos .img img { filter: grayscale(.7); opacity: .6; }
    /* nhan dan kieu sticker: nghieng nhe, vien trang */
    .badges { position: absolute; top: 10px; left: 10px; display: flex; flex-direction: column; gap: 6px; align-items: flex-start; }
    .b { font-size: 11px; font-weight: 800; padding: 3px 9px; border-radius: 999px; border: 2px solid #fff; box-shadow: 0 3px 6px rgb(43 35 80 / 15%); }
    .b.sale { background: var(--grad-cta); color: #fff; transform: rotate(-4deg); }
    .b.new { background: var(--p-mint); color: #146b45; transform: rotate(3deg); }
    .b.online { background: var(--p-sky); color: #1e4f9c; }
    .b.promo { background: var(--p-butter); color: #7a5300; transform: rotate(-2deg); }
    .ribbon { position: absolute; bottom: 10px; left: 10px; right: 10px; background: rgb(43 35 80 / 78%); color: #fff; text-align: center;
      font-weight: 800; font-size: 13px; padding: 7px; border-radius: 999px; backdrop-filter: blur(4px); }
    .heart { position: absolute; top: 16px; right: 16px; width: 38px; height: 38px; border: 0; border-radius: 50%; cursor: pointer;
      display: grid; place-items: center; background: rgb(255 255 255 / 92%); color: var(--ink-3); box-shadow: var(--clay-sm);
      transition: color .2s, transform .2s var(--ease-bounce); }
    .heart:hover { transform: scale(1.1); color: var(--tl-red); }
    .heart.on { color: var(--tl-red); }
    .heart.pop mat-icon { animation: tl-heart-pop .5s var(--ease-bounce); }
    .heart mat-icon { font-size: 22px; width: 22px; height: 22px; }
    .body { padding: 12px 8px 6px; display: flex; flex-direction: column; gap: 6px; flex: 1; }
    .meta { display: flex; gap: 6px; font-size: 12px; align-items: center; flex-wrap: wrap; }
    .brand { color: var(--tl-red-dark); font-weight: 800; text-transform: uppercase; letter-spacing: .04em; font-size: 11px; }
    .age { display: inline-flex; align-items: center; gap: 2px; background: var(--p-butter); color: #7a5300; padding: 2px 8px 2px 5px; border-radius: 999px; font-weight: 800; font-size: 11px; }
    .age mat-icon { font-size: 14px; width: 14px; height: 14px; }
    .name { font-weight: 800; font-size: 15px; line-height: 1.35; min-height: 40px; color: var(--ink); }
    .name:hover { color: var(--tl-red-dark); }
    .rating { display: flex; align-items: center; gap: 4px; font-size: 12px; color: var(--ink-3); min-height: 16px; font-weight: 600; }
    .sold { margin-left: auto; }
    .foot { display: flex; align-items: flex-end; gap: 8px; margin-top: auto; padding-top: 4px; }
    .prices { display: flex; flex-direction: column; flex: 1; min-width: 0; }
    .prices .price { font-size: 19px; font-family: var(--font-display); line-height: 1.1; }
    .add { width: 46px; height: 46px; flex: none; border: 0; border-radius: 16px; cursor: pointer; color: #fff; display: grid; place-items: center;
      background: var(--grad-cta); box-shadow: var(--clay-btn); transition: transform .2s var(--ease-bounce), border-radius .3s var(--ease-bounce); }
    .add:hover:not(:disabled) { transform: translateY(-2px) rotate(-6deg); border-radius: 50%; }
    .add:active:not(:disabled) { transform: scale(.92); }
    .add:disabled { background: var(--mat-sys-surface-container-highest); color: var(--ink-3); box-shadow: none; cursor: not-allowed; }
    @media (max-width: 600px) { .pc { border-radius: 20px; padding: 6px; } .img { border-radius: 16px; } .name { font-size: 13px; }
      .prices .price { font-size: 16px; } .add { width: 40px; height: 40px; border-radius: 14px; } .heart { top: 12px; right: 12px; width: 34px; height: 34px; } }
  `,
})
export class ProductCardComponent {
  product = input.required<ProductCard>();
  wishlist = inject(WishlistStore);
  private cart = inject(CartStore);
  private router = inject(Router);
  private notify = inject(NotifyService);
  placeholder = PLACEHOLDER_IMG;
  popping = signal(false);

  liked = computed(() => this.wishlist.ids().has(this.product().id));
  sold = computed(() => {
    const s = this.product().soldCount;
    return s >= 1000 ? (s / 1000).toFixed(1).replace('.0', '') + 'k' : String(s);
  });

  like(): void {
    this.popping.set(false);
    setTimeout(() => this.popping.set(true));
    setTimeout(() => this.popping.set(false), 550);
    this.wishlist.toggle(this.product().id);
  }

  quickAdd(): void {
    const p = this.product();
    if (p.variantCount > 1 || !p.defaultVariantId) {
      this.router.navigate(['/products', p.slug]);
      return;
    }
    this.cart.add(p.defaultVariantId, 1).subscribe({
      next: () => {
        this.notify.success('Đã thêm vào giỏ hàng');
        this.cart.drawerOpen.set(true);
      },
      error: (e) => this.notify.error(e),
    });
  }
}
