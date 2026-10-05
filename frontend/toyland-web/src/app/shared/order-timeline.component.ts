import { DatePipe } from '@angular/common';
import { Component, computed, input } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { ORDER_STATUS, ORDER_STEPS } from '../core/labels';
import { OrderHistory, OrderStatus } from '../core/models';

/** Thanh tien trinh don hang (kieu Shopee) + lich su thay doi trang thai */
@Component({
  selector: 'tl-order-timeline',
  imports: [MatIconModule, DatePipe],
  template: `
    @if (!abnormal()) {
      <div class="steps">
        @for (s of steps; track s; let i = $index) {
          <div class="st" [class.done]="i <= reached()" [class.cur]="i === reached()">
            <div class="dot"><mat-icon>{{ label[s].icon }}</mat-icon></div>
            <div class="lb">{{ label[s].label }}</div>
            @if (timeOf(s); as t) { <div class="tm">{{ t | date: 'HH:mm dd/MM' }}</div> }
          </div>
        }
      </div>
    } @else {
      <div class="abn chip st-{{ status() }}"><mat-icon>{{ label[status()].icon }}</mat-icon>{{ label[status()].label }}</div>
    }
    @if (showHistory()) {
      <div class="hist">
        @for (h of reversed(); track $index) {
          <div class="h" [class.first]="$first">
            <div class="h-dot"></div>
            <div>
              <div><b>{{ h.label }}</b> <span class="muted small">· {{ h.createdAt | date: 'HH:mm dd/MM/yyyy' }}</span></div>
              @if (h.note) { <div class="small">{{ h.note }}</div> }
              @if (h.changedBy && showActor()) { <div class="small muted">bởi {{ h.changedBy }}</div> }
            </div>
          </div>
        }
      </div>
    }
  `,
  styles: `
    .steps { display: flex; justify-content: space-between; position: relative; margin: 8px 0 20px; overflow-x: auto; }
    .st { flex: 1; min-width: 84px; text-align: center; position: relative; }
    .st::before { content: ''; position: absolute; top: 22px; left: -50%; right: 50%; height: 4px; border-radius: 2px; background: var(--tl-border); z-index: 0; }
    .st:first-child::before { display: none; }
    .st.done::before { background: var(--tl-green); }
    .dot { width: 46px; height: 46px; border-radius: 50%; border: 3px solid var(--tl-border); background: var(--tl-card); display: grid; place-items: center; margin: 0 auto 6px; position: relative; z-index: 1; color: var(--tl-subtle); }
    .st.done .dot { border-color: var(--tl-green); color: var(--tl-green); }
    .st.cur .dot { background: var(--tl-green); color: #fff; }
    .lb { font-size: 13px; font-weight: 700; }
    .tm { font-size: 11px; color: var(--tl-muted); }
    .abn { font-size: 15px; padding: 8px 14px; margin-bottom: 16px; }
    .hist { border-top: 1px dashed var(--tl-border); padding-top: 12px; }
    .h { display: flex; gap: 12px; padding: 6px 0; position: relative; }
    .h-dot { width: 10px; height: 10px; border-radius: 50%; background: var(--tl-border-strong); margin-top: 6px; flex: none; }
    .h.first .h-dot { background: var(--tl-green); box-shadow: 0 0 0 4px var(--tl-green-soft); }
    .h.first b { color: var(--tl-green); }
  `,
})
export class OrderTimelineComponent {
  status = input.required<OrderStatus>();
  history = input<OrderHistory[]>([]);
  showHistory = input(true);
  showActor = input(false);
  label = ORDER_STATUS;
  steps = ORDER_STEPS;

  abnormal = computed(() => !ORDER_STEPS.includes(this.status()));
  reached = computed(() => ORDER_STEPS.indexOf(this.status()));
  reversed = computed(() => [...this.history()].reverse());

  timeOf(s: OrderStatus): string | null {
    const h = [...this.history()].reverse().find((x) => x.toStatus === s);
    return h?.createdAt ?? null;
  }
}
