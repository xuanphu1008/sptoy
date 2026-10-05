import { VerifyEmailBannerComponent } from '../shared/verify-email-banner.component';
import { Component, OnInit, inject, signal } from '@angular/core';
import { toObservable, toSignal } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatBadgeModule } from '@angular/material/badge';
import { MatButtonModule } from '@angular/material/button';
import { MatDividerModule } from '@angular/material/divider';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatTooltipModule } from '@angular/material/tooltip';
import { NavigationEnd, Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { catchError, debounceTime, distinctUntilChanged, filter, of, switchMap } from 'rxjs';
import { ShopApi } from '../core/api.service';
import { AuthService } from '../core/auth.service';
import { CartStore } from '../core/cart.store';
import { CatalogStore } from '../core/catalog.store';
import { AGE_GROUPS } from '../core/labels';
import { Suggestion } from '../core/models';
import { VndPipe } from '../core/vnd.pipe';
import { CartDrawerComponent } from './cart-drawer.component';
import { ShopFooterComponent } from './shop-footer.component';
import { LogoComponent } from '../shared/logo.component';
import { PASTELS } from '../core/labels';
import { NotificationBellComponent } from '../shared/notification-bell.component';

@Component({
  selector: 'tl-shop-layout',
  imports: [NotificationBellComponent, VerifyEmailBannerComponent, 
    RouterOutlet, RouterLink, RouterLinkActive, FormsModule, MatSidenavModule, MatIconModule, MatButtonModule, MatBadgeModule,
    MatMenuModule, MatDividerModule, MatAutocompleteModule, MatTooltipModule, VndPipe, CartDrawerComponent, ShopFooterComponent, LogoComponent,
  ],
  templateUrl: './shop-layout.component.html',
  styleUrl: './shop-layout.component.scss',
})
export class ShopLayoutComponent implements OnInit {
  auth = inject(AuthService);
  cart = inject(CartStore);
  catalog = inject(CatalogStore);
  private api = inject(ShopApi);
  private router = inject(Router);

  ages = AGE_GROUPS;
  keyword = signal('');
  menuOpen = signal(false);
  hoverCat = signal<number | null>(null);
  scrolled = signal(false);

  suggestion = toSignal(
    toObservable(this.keyword).pipe(
      debounceTime(250),
      distinctUntilChanged(),
      switchMap((q) => (q.trim().length < 2 ? of(null) : this.api.suggest(q.trim()).pipe(catchError(() => of(null))))),
    ),
    { initialValue: null as Suggestion | null },
  );

  ngOnInit(): void {
    this.catalog.load();
    this.auth.refreshMe(); // cap nhat trang thai xac nhan email (phien cu trong localStorage chua co truong nay)
    this.router.events.pipe(filter((e) => e instanceof NavigationEnd)).subscribe(() => {
      this.menuOpen.set(false);
      this.hoverCat.set(null);
    });
  }

  pastel(i: number): string {
    return PASTELS[i % PASTELS.length];
  }

  onScroll(ev: Event): void {
    const top = (ev.target as HTMLElement).scrollTop > 8;
    if (top !== this.scrolled()) this.scrolled.set(top);
  }

  search(): void {
    const q = this.keyword().trim();
    this.router.navigate(['/products'], { queryParams: q ? { q } : {} });
  }

  pick(path: unknown[], params?: Record<string, string>): void {
    this.keyword.set('');
    this.router.navigate(path, params ? { queryParams: params } : {});
  }

  logout(): void {
    this.auth.logout();
  }
}
