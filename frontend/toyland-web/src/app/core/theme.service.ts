import { DOCUMENT } from '@angular/common';
import { Injectable, inject, signal } from '@angular/core';
import { Chart } from 'chart.js';

const KEY = 'toyland_admin_theme';

/**
 * Che do Sang/Toi cho TRANG QUAN TRI (cua hang luon sang cho tre em & phu huynh).
 * Bat/tat bang class "dark" tren <html> de ca dialog/menu (nam ngoai layout) cung doi mau.
 */
@Injectable({ providedIn: 'root' })
export class ThemeService {
  private doc = inject(DOCUMENT);
  readonly dark = signal(this.load());
  private active = false;

  /** Goi khi vao/ra khu vuc admin */
  attach(active: boolean): void {
    this.active = active;
    this.apply();
  }

  toggle(): void {
    this.dark.update((d) => !d);
    try { localStorage.setItem(KEY, this.dark() ? 'dark' : 'light'); } catch { /* bo qua */ }
    this.apply();
  }

  private apply(): void {
    const on = this.active && this.dark();
    this.doc.documentElement.classList.toggle('dark', on);
    // Mau chu/luoi cua bieu do theo che do
    Chart.defaults.color = on ? '#b2abcc' : '#5b5378';
    Chart.defaults.borderColor = on ? 'rgba(255,255,255,.07)' : 'rgba(43,35,80,.08)';
    Chart.defaults.font.family = 'Nunito, sans-serif';
  }

  private load(): boolean {
    try {
      const v = localStorage.getItem(KEY);
      return v ? v === 'dark' : window.matchMedia?.('(prefers-color-scheme: dark)').matches ?? false;
    } catch {
      return false;
    }
  }
}
