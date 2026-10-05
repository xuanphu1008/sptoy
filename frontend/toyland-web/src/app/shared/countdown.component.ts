import { Component, DestroyRef, computed, inject, input, signal } from '@angular/core';

/** Dong ho dem nguoc (chuong trinh KM, han thanh toan) */
@Component({
  selector: 'tl-countdown',
  template: `
    @if (left() > 0) {
      <span class="cd">
        @if (parts().d > 0) { <b>{{ parts().d }}</b><i>ngày</i> }
        <b>{{ pad(parts().h) }}</b>:<b>{{ pad(parts().m) }}</b>:<b>{{ pad(parts().s) }}</b>
      </span>
    } @else {
      <span class="muted">Đã kết thúc</span>
    }
  `,
  styles: `.cd { display: inline-flex; align-items: center; gap: 3px; font-weight: 800; }
    .cd b { background: var(--ink); color: #fff; border-radius: 10px; padding: 3px 7px; min-width: 30px; text-align: center; font-family: var(--font-display); font-variant-numeric: tabular-nums; box-shadow: inset 0 -3px 0 rgb(0 0 0 / 25%); }
    .cd i { font-style: normal; font-size: 12px; margin: 0 4px 0 2px; }`,
})
export class CountdownComponent {
  end = input.required<string>();
  private now = signal(Date.now());
  left = computed(() => Math.max(0, new Date(this.end()).getTime() - this.now()));
  parts = computed(() => {
    const t = Math.floor(this.left() / 1000);
    return { d: Math.floor(t / 86400), h: Math.floor((t % 86400) / 3600), m: Math.floor((t % 3600) / 60), s: t % 60 };
  });

  constructor() {
    const id = setInterval(() => this.now.set(Date.now()), 1000);
    inject(DestroyRef).onDestroy(() => clearInterval(id));
  }

  pad(n: number): string {
    return String(n).padStart(2, '0');
  }
}
