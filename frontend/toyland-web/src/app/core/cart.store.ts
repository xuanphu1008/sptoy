import { HttpClient } from '@angular/common/http';
import { Injectable, computed, effect, inject, signal, untracked } from '@angular/core';
import { Observable, of, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { SILENT } from './api.service';
import { AuthService } from './auth.service';
import { CartView, ShippingConfig } from './models';

const GUEST_KEY = 'toyland_guest_cart';
export const MAX_QTY = 99;
const EMPTY: CartView = { lines: [], totalQuantity: 0, subtotal: 0, hasIssues: false };

interface GuestLine { variantId: number; quantity: number; }

/**
 * Gio hang:
 *  - Khach chua dang nhap: luu localStorage (variantId, quantity), gia/ton kho lay tu /cart/preview.
 *  - Da dang nhap: luu DB qua /cart. Khi vua dang nhap -> gop gio hang khach vao tai khoan (/cart/merge).
 */
@Injectable({ providedIn: 'root' })
export class CartStore {
  private http = inject(HttpClient);
  private auth = inject(AuthService);
  private api = `${environment.apiUrl}/cart`;

  readonly cart = signal<CartView>(EMPTY);
  readonly count = computed(() => this.cart().totalQuantity);
  readonly drawerOpen = signal(false);
  /** true trong ~0.6s sau khi them vao gio -> icon gio hang "nay" (micro-interaction) */
  readonly bumping = signal(false);
  /** Cau hinh tu backend (/shipping/config): nguong freeship, phi goi qua */
  readonly config = signal<ShippingConfig>({ ghnEnabled: false, freeShipThreshold: 500000, fixedFee: 30000, giftWrapFee: 20000 });

  constructor() {
    this.http.get<ShippingConfig>(`${environment.apiUrl}/shipping/config`, SILENT).subscribe({ next: (c) => this.config.set(c), error: () => {} });
    effect(() => {
      const loggedIn = this.auth.isLoggedIn();
      untracked(() => (loggedIn ? this.onLogin() : this.reload()));
    });
  }

  reload(): void {
    this.load().subscribe({ error: () => this.cart.set(EMPTY) });
  }

  private load(): Observable<CartView> {
    if (this.auth.isLoggedIn()) return this.http.get<CartView>(this.api, SILENT).pipe(tap((c) => this.cart.set(c)));
    const lines = this.guestLines();
    if (!lines.length) {
      this.cart.set(EMPTY);
      return of(EMPTY);
    }
    return this.http.post<CartView>(`${this.api}/preview`, lines, SILENT).pipe(tap((c) => this.cart.set(c)));
  }

  private onLogin(): void {
    if (this.auth.isStaff()) return this.reload();
    const lines = this.guestLines();
    if (!lines.length) return this.reload();
    this.http.post<CartView>(`${this.api}/merge`, lines, SILENT).subscribe({
      next: (c) => {
        localStorage.removeItem(GUEST_KEY);
        this.cart.set(c);
      },
      error: () => this.reload(),
    });
  }

  add(variantId: number, quantity = 1): Observable<CartView> {
    if (this.auth.isLoggedIn()) {
      return this.http.post<CartView>(this.api, { variantId, quantity }).pipe(tap((c) => { this.cart.set(c); this.bump(); }));
    }
    const lines = this.guestLines();
    const found = lines.find((l) => l.variantId === variantId);
    if (found) found.quantity = Math.min(MAX_QTY, found.quantity + quantity);
    else lines.push({ variantId, quantity: Math.min(MAX_QTY, quantity) });
    return this.saveGuest(lines).pipe(tap(() => this.bump()));
  }

  /** key = itemId (dang nhap) hoac variantId (khach) */
  update(key: { itemId?: number | null; variantId: number }, quantity: number): Observable<CartView> {
    if (this.auth.isLoggedIn() && key.itemId) {
      return this.http.put<CartView>(`${this.api}/${key.itemId}`, { quantity }).pipe(tap((c) => this.cart.set(c)));
    }
    const lines = this.guestLines().map((l) => (l.variantId === key.variantId ? { ...l, quantity } : l));
    return this.saveGuest(lines);
  }

  remove(key: { itemId?: number | null; variantId: number }): Observable<CartView> {
    if (this.auth.isLoggedIn() && key.itemId) {
      return this.http.delete<CartView>(`${this.api}/${key.itemId}`).pipe(tap((c) => this.cart.set(c)));
    }
    return this.saveGuest(this.guestLines().filter((l) => l.variantId !== key.variantId));
  }

  clear(): Observable<CartView> {
    if (this.auth.isLoggedIn()) return this.http.delete<CartView>(this.api).pipe(tap((c) => this.cart.set(c)));
    return this.saveGuest([]);
  }

  /** Sau khi mua lai (reorder) backend tra ve gio hang moi */
  set(view: CartView): void {
    this.cart.set(view);
  }

  private bump(): void {
    this.bumping.set(false);
    setTimeout(() => this.bumping.set(true));
    setTimeout(() => this.bumping.set(false), 650);
  }

  private saveGuest(lines: GuestLine[]): Observable<CartView> {
    localStorage.setItem(GUEST_KEY, JSON.stringify(lines.filter((l) => l.quantity > 0)));
    return this.load();
  }

  private guestLines(): GuestLine[] {
    try {
      return JSON.parse(localStorage.getItem(GUEST_KEY) || '[]') as GuestLine[];
    } catch {
      return [];
    }
  }
}
