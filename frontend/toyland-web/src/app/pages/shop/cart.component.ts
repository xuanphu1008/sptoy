import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { Router, RouterLink } from '@angular/router';
import { ShopApi } from '../../core/api.service';
import { AuthService } from '../../core/auth.service';
import { CartStore, MAX_QTY } from '../../core/cart.store';
import { PLACEHOLDER_IMG } from '../../core/labels';
import { CartLine, Voucher } from '../../core/models';
import { NotifyService } from '../../core/notify.service';
import { VndPipe } from '../../core/vnd.pipe';
import { confirmDialog } from '../../shared/confirm-dialog.component';

@Component({
  selector: 'tl-cart',
  imports: [RouterLink, MatButtonModule, MatIconModule, MatProgressBarModule, VndPipe],
  template: `
    <div class="container page">
      <h1>Giỏ hàng của bạn</h1>
      @if (cart.cart().lines.length) {
        <div class="layout">
          <div>
            <div class="card ship">
              @if (remain() > 0) {
                <mat-icon>local_shipping</mat-icon> Mua thêm <b class="price">{{ remain() | vnd }}</b> để được <b>miễn phí giao hàng</b>
              } @else {
                <mat-icon>celebration</mat-icon> Chúc mừng! Đơn hàng của bạn được <b>miễn phí giao hàng</b>
              }
              <mat-progress-bar mode="determinate" [value]="progress()" />
            </div>
            <div class="card">
              <div class="row between">
                <b>{{ cart.count() }} sản phẩm</b>
                <button mat-button (click)="clear()"><mat-icon>delete_sweep</mat-icon>Xoá tất cả</button>
              </div>
              @for (l of cart.cart().lines; track l.variantId) {
                <div class="line-item" [class.bad]="!l.available">
                  <img [src]="l.imageUrl || ph" alt="" />
                  <div class="info">
                    <a class="name" [routerLink]="['/products', l.productSlug]">{{ l.productName }}</a>
                    @if (l.variantName) { <div class="variant">Phân loại: {{ l.variantName }}</div> }
                    <div class="row small">
                      <span class="price">{{ l.unitPrice | vnd }}</span>
                      @if (l.originalPrice && l.originalPrice > l.unitPrice) { <span class="price-old">{{ l.originalPrice | vnd }}</span> }
                    </div>
                    @if (!l.available) { <div class="warn"><mat-icon>error</mat-icon>{{ l.message }}</div> }
                    @else if (l.stock <= 5) { <div class="muted small">Chỉ còn {{ l.stock }} sản phẩm</div> }
                  </div>
                  <div class="qty">
                    <button (click)="setQty(l, l.quantity - 1)" [disabled]="l.quantity <= 1">−</button>
                    <input type="number" [value]="l.quantity" (change)="typed(l, $event)" min="1" [max]="l.stock" />
                    <button (click)="setQty(l, l.quantity + 1)" [disabled]="l.quantity >= l.stock || l.quantity >= maxQty">+</button>
                  </div>
                  <div class="total price hide-mobile">{{ l.lineTotal | vnd }}</div>
                  <button mat-icon-button (click)="remove(l)"><mat-icon>delete_outline</mat-icon></button>
                </div>
              }
            </div>
          </div>

          <aside>
            <div class="card">
              <h3>Tóm tắt đơn hàng</h3>
              <div class="sum-row"><span>Tạm tính ({{ cart.count() }} sản phẩm)</span><span>{{ cart.cart().subtotal | vnd }}</span></div>
              <div class="sum-row muted"><span>Phí vận chuyển</span><span>Tính ở bước thanh toán</span></div>
              <div class="sum-row total"><span>Tổng cộng</span><span class="v">{{ cart.cart().subtotal | vnd }}</span></div>
              @if (cart.cart().hasIssues) {
                <div class="warn mt-8"><mat-icon>error</mat-icon>Giỏ hàng có sản phẩm hết hàng / ngừng bán. Vui lòng điều chỉnh trước khi đặt.</div>
              }
              <button mat-flat-button class="full checkout" [disabled]="cart.cart().hasIssues" (click)="checkout()">Tiến hành đặt hàng</button>
              <a mat-button class="full" routerLink="/products"><mat-icon>arrow_back</mat-icon>Tiếp tục mua sắm</a>
            </div>
            @if (vouchers().length) {
              <div class="card">
                <h4><mat-icon>confirmation_number</mat-icon> Mã giảm giá dành cho bạn</h4>
                @for (v of vouchers(); track v.id) {
                  <div class="voucher">
                    <div class="grow"><b>{{ v.code }}</b><div class="small muted">{{ v.summary }}</div></div>
                    <button mat-button (click)="copy(v.code)">Sao chép</button>
                  </div>
                }
                <div class="small muted">Nhập mã ở bước thanh toán.</div>
              </div>
            }
          </aside>
        </div>
      } @else {
        <div class="card empty">
          <mat-icon>shopping_cart</mat-icon>
          <h3>Giỏ hàng trống</h3>
          <p>Hãy chọn thêm những món đồ chơi bé yêu thích nhé!</p>
          <a mat-flat-button routerLink="/products">Mua sắm ngay</a>
        </div>
      }
    </div>
  `,
  styles: `
    .layout { display: grid; grid-template-columns: minmax(0, 1fr) 360px; gap: 20px; align-items: start; }
    .ship { background: var(--tl-yellow-soft); }
    .ship mat-icon { vertical-align: -6px; color: var(--tl-orange); }
    .ship mat-progress-bar { margin-top: 10px; border-radius: 4px; }
    .line-item .qty { flex: none; }
    .line-item.bad { opacity: .75; }
    .line-item .total { width: 120px; text-align: right; }
    .warn { color: var(--tl-red-dark); font-size: 13px; font-weight: 700; display: flex; align-items: center; gap: 4px; }
    .warn mat-icon { font-size: 18px; width: 18px; height: 18px; }
    .checkout { height: 48px; font-size: 16px; font-weight: 800; margin: 16px 0 8px; }
    aside { position: sticky; top: 130px; }
    h4 { display: flex; align-items: center; gap: 6px; }
    .voucher { display: flex; align-items: center; border: 1px dashed var(--tl-red); background: var(--tl-red-soft); border-radius: 10px; padding: 6px 6px 6px 12px; margin-bottom: 8px; }
    @media (max-width: 900px) { .layout { grid-template-columns: minmax(0, 1fr); } aside { position: static; } }
  `,
})
export class CartComponent implements OnInit {
  cart = inject(CartStore);
  private api = inject(ShopApi);
  private auth = inject(AuthService);
  private notify = inject(NotifyService);
  private router = inject(Router);
  private dialog = inject(MatDialog);
  vouchers = signal<Voucher[]>([]);
  ph = PLACEHOLDER_IMG;

  maxQty = MAX_QTY;
  remain = computed(() => Math.max(0, this.cart.config().freeShipThreshold - this.cart.cart().subtotal));
  progress = computed(() => Math.min(100, (this.cart.cart().subtotal / (this.cart.config().freeShipThreshold || 1)) * 100));

  ngOnInit(): void {
    this.cart.reload();
    this.api.availableVouchers().subscribe({ next: (v) => this.vouchers.set(v.slice(0, 4)), error: () => {} });
  }

  setQty(l: CartLine, q: number): void {
    if (q < 1) return;
    this.cart.update(l, q).subscribe({ error: (e) => this.notify.error(e) });
  }

  typed(l: CartLine, ev: Event): void {
    const q = Math.max(1, Math.min(l.stock || 1, MAX_QTY, Math.floor(+(ev.target as HTMLInputElement).value || 1)));
    (ev.target as HTMLInputElement).value = String(q);
    this.setQty(l, q);
  }

  remove(l: CartLine): void {
    this.cart.remove(l).subscribe({ next: () => this.notify.info('Đã xoá sản phẩm khỏi giỏ'), error: (e) => this.notify.error(e) });
  }

  clear(): void {
    confirmDialog(this.dialog, { title: 'Xoá toàn bộ giỏ hàng?', confirmText: 'Xoá hết', danger: true }).subscribe((ok) => {
      if (ok) this.cart.clear().subscribe();
    });
  }

  copy(code: string): void {
    navigator.clipboard?.writeText(code).then(() => this.notify.info(`Đã sao chép mã ${code}`));
  }

  checkout(): void {
    if (!this.auth.isLoggedIn()) {
      this.notify.info('Vui lòng đăng nhập để đặt hàng. Giỏ hàng của bạn sẽ được giữ nguyên.');
      this.router.navigate(['/login'], { queryParams: { returnUrl: '/checkout' } });
      return;
    }
    this.router.navigate(['/checkout']);
  }
}
