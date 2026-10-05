import { DatePipe } from '@angular/common';
import { Component, inject, input } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatTooltipModule } from '@angular/material/tooltip';
import { NotificationStore } from '../core/notification.store';

const ICONS: Record<string, string> = {
  ORDER_NEW: 'shopping_bag', ORDER_STATUS: 'local_shipping', ORDER_CANCELLED: 'cancel', RETURN_REQUESTED: 'assignment_return',
  REVIEW_FLAGGED: 'gpp_maybe', LOW_STOCK: 'inventory_2', PAYMENT_AFTER_CANCEL: 'currency_exchange', REFUND_PENDING: 'currency_exchange', REFUND_DONE: 'price_check', PAYMENT_RECEIVED: 'savings', POINTS_EARNED: 'stars',
  POINTS_ADJUSTED: 'stars', WELCOME: 'waving_hand',
};

/** Chuong thong bao dung chung cho cua hang va trang quan tri. */
@Component({
  selector: 'tl-notification-bell',
  imports: [MatMenuModule, MatIconModule, MatButtonModule, MatTooltipModule, DatePipe],
  template: `
    <button class="bell" [class.admin]="variant() === 'admin'" [class.ring]="store.ringing()" [matMenuTriggerFor]="menu"
            (menuOpened)="store.load()" [attr.aria-label]="'Thông báo' + (store.unread() ? ', ' + store.unread() + ' chưa đọc' : '')"
            matTooltip="Thông báo">
      <mat-icon>{{ store.hasUnread() ? 'notifications_active' : 'notifications_none' }}</mat-icon>
      @if (store.unread()) { <span class="count">{{ store.unread() > 99 ? '99+' : store.unread() }}</span> }
      <span class="live" [class.on]="store.connected()" [attr.title]="store.connected() ? 'Đang nhận realtime' : 'Mất kết nối realtime'"></span>
    </button>
    <mat-menu #menu="matMenu" xPosition="before" class="noti-menu">
      <div class="head" (click)="$event.stopPropagation()">
        <b>Thông báo</b>
        @if (store.unread()) { <button mat-button (click)="store.markAllRead()">Đánh dấu đã đọc</button> }
      </div>
      @for (n of store.items(); track n.id) {
        <button mat-menu-item class="item" [class.unread]="!n.read" (click)="store.open(n)">
          <span class="ico t-{{ n.type }}"><mat-icon>{{ icon(n.type) }}</mat-icon></span>
          <span class="txt">
            <b>{{ n.title }}</b>
            @if (n.message) { <small>{{ n.message }}</small> }
            <em>{{ n.createdAt | date: 'HH:mm dd/MM' }}</em>
          </span>
        </button>
      } @empty {
        <div class="empty-noti"><mat-icon>notifications_off</mat-icon>Chưa có thông báo nào</div>
      }
    </mat-menu>
  `,
  styles: `
    .bell { position: relative; width: 44px; height: 44px; border-radius: 50%; border: 0; background: var(--tl-card); color: var(--tl-text);
      display: grid; place-items: center; cursor: pointer; box-shadow: var(--clay-sm); transition: transform .2s var(--ease-bounce); }
    .bell:hover { transform: translateY(-2px); }
    .bell.admin { width: 42px; height: 42px; }
    .bell.ring mat-icon { animation: tl-wiggle .4s ease-in-out 3; }
    .count { position: absolute; top: -3px; right: -3px; min-width: 20px; height: 20px; padding: 0 5px; border-radius: 999px;
      background: var(--grad-cta); color: #fff; font-size: 11px; font-weight: 800; display: grid; place-items: center;
      box-shadow: 0 0 0 2px var(--tl-card); }
    .live { position: absolute; bottom: 4px; right: 5px; width: 8px; height: 8px; border-radius: 50%; background: var(--tl-subtle);
      box-shadow: 0 0 0 2px var(--tl-card); }
    .live.on { background: var(--tl-green); }
    .head { display: flex; align-items: center; justify-content: space-between; gap: 8px; padding: 6px 12px 6px 16px;
      font-family: var(--font-display); font-size: 16px; }
    .item { height: auto !important; min-height: 64px; white-space: normal; line-height: 1.3; padding-top: 8px; padding-bottom: 8px; }
    .item ::ng-deep .mat-mdc-menu-item-text { display: flex; gap: 12px; align-items: flex-start; }
    .item.unread { background: var(--tl-red-soft); }
    .ico { width: 36px; height: 36px; flex: none; border-radius: 12px; display: grid; place-items: center; background: var(--p-sky);
      color: var(--tl-text); }
    .ico mat-icon { margin: 0 !important; font-size: 20px; width: 20px; height: 20px; }
    .t-ORDER_NEW { background: var(--p-mint); } .t-REVIEW_FLAGGED, .t-ORDER_CANCELLED, .t-PAYMENT_AFTER_CANCEL, .t-REFUND_PENDING { background: var(--p-pink); } .t-REFUND_DONE, .t-PAYMENT_RECEIVED { background: var(--p-mint); }
    .t-LOW_STOCK, .t-RETURN_REQUESTED { background: var(--p-peach); } .t-POINTS_EARNED, .t-POINTS_ADJUSTED { background: var(--p-butter); }
    html.dark .ico { filter: saturate(.6) brightness(.8); }
    .txt { display: flex; flex-direction: column; gap: 2px; min-width: 0; }
    .txt b { font-size: 14px; }
    .txt small { color: var(--tl-muted); font-size: 13px; }
    .txt em { color: var(--tl-subtle); font-size: 11px; font-style: normal; }
    .empty-noti { display: flex; flex-direction: column; align-items: center; gap: 6px; padding: 24px; color: var(--tl-muted); }
  `,
})
export class NotificationBellComponent {
  store = inject(NotificationStore);
  variant = input<'shop' | 'admin'>('shop');

  icon(type: string): string {
    return ICONS[type] ?? 'notifications';
  }
}
