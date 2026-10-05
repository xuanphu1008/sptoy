import { HttpClient } from '@angular/common/http';
import { Injectable, effect, inject, signal, untracked } from '@angular/core';
import { Router } from '@angular/router';
import { environment } from '../../environments/environment';
import { SILENT } from './api.service';
import { AuthService } from './auth.service';
import { NotifyService } from './notify.service';

/** Danh sach id san pham yeu thich (de to trai tim tren card) */
@Injectable({ providedIn: 'root' })
export class WishlistStore {
  private http = inject(HttpClient);
  private auth = inject(AuthService);
  private router = inject(Router);
  private notify = inject(NotifyService);
  private api = `${environment.apiUrl}/me/wishlist`;

  readonly ids = signal<Set<number>>(new Set());

  constructor() {
    effect(() => {
      const ok = this.auth.isLoggedIn() && !this.auth.isStaff();
      untracked(() => {
        if (!ok) this.ids.set(new Set());
        else this.http.get<number[]>(`${this.api}/ids`, SILENT).subscribe({ next: (ids) => this.ids.set(new Set(ids)), error: () => {} });
      });
    });
  }

  has(id: number): boolean {
    return this.ids().has(id);
  }

  toggle(id: number): void {
    if (!this.auth.isLoggedIn()) {
      this.notify.info('Vui lòng đăng nhập để lưu sản phẩm yêu thích');
      this.router.navigate(['/login'], { queryParams: { returnUrl: this.router.url } });
      return;
    }
    const liked = this.has(id);
    const req = liked ? this.http.delete(`${this.api}/${id}`, SILENT) : this.http.post(`${this.api}/${id}`, {}, SILENT);
    req.subscribe({
      next: () => {
        const next = new Set(this.ids());
        if (liked) next.delete(id);
        else next.add(id);
        this.ids.set(next);
        this.notify.info(liked ? 'Đã bỏ khỏi danh sách yêu thích' : 'Đã thêm vào danh sách yêu thích ♥');
      },
      error: (e) => this.notify.error(e),
    });
  }
}
