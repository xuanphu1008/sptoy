import { Injectable } from '@angular/core';

const KEY = 'toyland_recent';
const MAX = 12;

/** Luu id san pham da xem gan day (localStorage) */
@Injectable({ providedIn: 'root' })
export class RecentlyViewedService {
  add(productId: number): void {
    const ids = [productId, ...this.ids().filter((x) => x !== productId)].slice(0, MAX);
    localStorage.setItem(KEY, JSON.stringify(ids));
  }

  ids(): number[] {
    try {
      return JSON.parse(localStorage.getItem(KEY) || '[]') as number[];
    } catch {
      return [];
    }
  }
}
