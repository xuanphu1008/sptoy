import { Component, computed, inject, output } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { Router, RouterLink } from '@angular/router';
import { CartStore, MAX_QTY } from '../core/cart.store';
import { PLACEHOLDER_IMG } from '../core/labels';
import { CartLine } from '../core/models';
import { NotifyService } from '../core/notify.service';
import { VndPipe } from '../core/vnd.pipe';

/** Gio hang mini truot ra tu ben phai */
@Component({
  selector: 'tl-cart-drawer',
  imports: [RouterLink, MatButtonModule, MatIconModule, MatProgressBarModule, VndPipe],
  template: `
    <div class="wrap">
      <div class="head row between">
        <h3 class="mb-0">Giỏ hàng ({{ cart.count() }})</h3>
        <button mat-icon-button (click)="close.emit()"><mat-icon>close</mat-icon></button>
      </div>
      @if (cart.cart().lines.length) {
        <div class="ship">
          @if (remain() > 0) {
            Mua thêm <b class="price">{{ remain() | vnd }}</b> để được <b>miễn phí giao hàng</b>
          } @else {
            <mat-icon>celebration</mat-icon> Đơn hàng của bạn được <b>miễn phí giao hàng</b>!
          }
          <mat-progress-bar mode="determinate" [value]="progress()" />
        </div>
        <div class="lines">
          @for (l of cart.cart().lines; track l.variantId) {
            <div class="line-item">
              <img [src]="l.imageUrl || ph" alt="" />
              <div class="info">
                <a class="name clamp-2" [routerLink]="['/products', l.productSlug]" (click)="close.emit()">{{ l.productName }}</a>
                @if (l.variantName) { <div class="variant">{{ l.variantName }}</div> }
                @if (!l.available) { <div class="warn">{{ l.message }}</div> }
                <div class="row between mt-8">
                  <div class="qty">
                    <button (click)="setQty(l, l.quantity - 1)" [disabled]="l.quantity <= 1">−</button>
                    <input type="number" [value]="l.quantity" readonly />
                    <button (click)="setQty(l, l.quantity + 1)" [disabled]="l.quantity >= l.stock || l.quantity >= maxQty">+</button>
                  </div>
                  <span class="price">{{ l.lineTotal | vnd }}</span>
                </div>
              </div>
              <button mat-icon-button (click)="remove(l)"><mat-icon>delete_outline</mat-icon></button>
            </div>
          }
        </div>
        <div class="foot">
          <div class="sum-row total"><span>Tạm tính</span><span class="v">{{ cart.cart().subtotal | vnd }}</span></div>
          <div class="row mt-8">
            <a mat-stroked-button class="grow" routerLink="/cart" (click)="close.emit()">Xem giỏ hàng</a>
            <button mat-flat-button class="grow" (click)="checkout()" [disabled]="cart.cart().hasIssues">Thanh toán</button>
          </div>
        </div>
      } @else {
        <div class="empty">
          <mat-icon>remove_shopping_cart</mat-icon>
          <p>Giỏ hàng của bạn đang trống</p>
          <a mat-flat-button routerLink="/products" (click)="close.emit()">Tiếp tục mua sắm</a>
        </div>
      }
    </div>
  `,
  styles: `
    .wrap { display: flex; flex-direction: column; height: 100%; }
    .head { padding: 12px 16px; border-bottom: 1px solid #eee; }
    .ship { background: var(--tl-yellow-soft); padding: 10px 16px; font-size: 13px; }
    .ship mat-progress-bar { margin-top: 6px; border-radius: 4px; }
    .ship mat-icon { font-size: 18px; vertical-align: -4px; color: var(--tl-orange); }
    .lines { flex: 1; overflow: auto; padding: 0 16px; }
    .warn { color: var(--tl-red); font-size: 12px; font-weight: 700; }
    .foot { padding: 12px 16px 16px; border-top: 1px solid #eee; }
  `,
})
export class CartDrawerComponent {
  cart = inject(CartStore);
  private notify = inject(NotifyService);
  private router = inject(Router);
  close = output<void>();
  ph = PLACEHOLDER_IMG;

  maxQty = MAX_QTY;
  remain = computed(() => Math.max(0, this.cart.config().freeShipThreshold - this.cart.cart().subtotal));
  progress = computed(() => Math.min(100, (this.cart.cart().subtotal / (this.cart.config().freeShipThreshold || 1)) * 100));

  setQty(l: CartLine, q: number): void {
    if (q < 1) return;
    this.cart.update(l, q).subscribe({ error: (e) => this.notify.error(e) });
  }

  remove(l: CartLine): void {
    this.cart.remove(l).subscribe({ error: (e) => this.notify.error(e) });
  }

  checkout(): void {
    this.close.emit();
    this.router.navigate(['/checkout']);
  }
}
