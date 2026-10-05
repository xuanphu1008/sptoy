import { Injectable, computed, effect, inject, signal, untracked } from '@angular/core';
import { Router } from '@angular/router';
import { Client, IMessage } from '@stomp/stompjs';
import { firstValueFrom } from 'rxjs';
import { environment } from '../../environments/environment';
import { ShopApi } from './api.service';
import { AuthService } from './auth.service';
import { AppNotification } from './models';
import { NotifyService } from './notify.service';

/** ws://localhost:8080/ws (suy ra tu apiUrl) */
const WS_URL = environment.apiUrl.replace(/^http/, 'ws').replace(/\/api\/v1\/?$/, '/ws');

/** Thoi diem het han (ms) doc tu payload JWT; 0 neu khong doc duoc. */
function tokenExp(token: string | null): number {
  try {
    const payload = JSON.parse(atob((token ?? '').split('.')[1].replace(/-/g, '+').replace(/_/g, '/')));
    return (payload.exp ?? 0) * 1000;
  } catch {
    return 0;
  }
}

/**
 * Thong bao trong ung dung:
 *  - Tai 10 thong bao gan nhat + so chua doc qua REST.
 *  - Mo ket noi WebSocket (STOMP) khi da dang nhap, nhan thong bao moi realtime o /user/queue/notifications.
 *  - Tu dong lam moi access token truoc khi (tai) ket noi.
 */
@Injectable({ providedIn: 'root' })
export class NotificationStore {
  private api = inject(ShopApi);
  private auth = inject(AuthService);
  private notify = inject(NotifyService);
  private router = inject(Router);
  private client: Client | null = null;

  readonly items = signal<AppNotification[]>([]);
  readonly unread = signal(0);
  readonly connected = signal(false);
  readonly hasUnread = computed(() => this.unread() > 0);
  /** Hieu ung rung chuong khi co thong bao moi */
  readonly ringing = signal(false);

  constructor() {
    // dang nhap -> ket noi; dang xuat -> ngat
    effect(() => {
      const user = this.auth.user();
      untracked(() => (user ? this.start() : this.stop()));
    });
  }

  load(): void {
    this.api.notifications({ size: 10 }).subscribe({ next: (p) => this.items.set(p.content), error: () => {} });
    this.api.unreadCount().subscribe({ next: (r) => this.unread.set(r.unread), error: () => {} });
  }

  open(n: AppNotification): void {
    if (!n.read) {
      this.api.readNotification(n.id).subscribe({ error: () => {} });
      this.items.update((list) => list.map((x) => (x.id === n.id ? { ...x, read: true } : x)));
      this.unread.update((c) => Math.max(0, c - 1));
    }
    if (n.link) this.router.navigateByUrl(n.link);
  }

  markAllRead(): void {
    this.api.readAllNotifications().subscribe({ error: () => {} });
    this.items.update((list) => list.map((x) => ({ ...x, read: true })));
    this.unread.set(0);
  }

  private start(): void {
    this.load();
    if (this.client) return;
    const client = new Client({
      brokerURL: WS_URL,
      reconnectDelay: 5000,
      heartbeatIncoming: 0,
      heartbeatOutgoing: 0,
      debug: () => {},
      beforeConnect: async () => {
        // access token song 15 phut: sap het han thi lam moi truoc khi gui khung CONNECT
        const exp = tokenExp(this.auth.accessToken);
        if (exp && exp - Date.now() < 30_000 && this.auth.isLoggedIn()) {
          try {
            await firstValueFrom(this.auth.refresh());
          } catch {
            /* refresh that bai -> interceptor HTTP se dang xuat */
          }
        }
        client.connectHeaders = { Authorization: `Bearer ${this.auth.accessToken ?? ''}` };
      },
      onConnect: () => {
        this.connected.set(true);
        client.subscribe('/user/queue/notifications', (msg: IMessage) => this.receive(JSON.parse(msg.body) as AppNotification));
      },
      onWebSocketClose: () => this.connected.set(false),
      onStompError: () => this.connected.set(false),
    });
    this.client = client;
    client.activate();
  }

  private stop(): void {
    this.client?.deactivate();
    this.client = null;
    this.connected.set(false);
    this.items.set([]);
    this.unread.set(0);
  }

  private receive(n: AppNotification): void {
    this.items.update((list) => [n, ...list.filter((x) => x.id !== n.id)].slice(0, 10));
    this.unread.update((c) => c + 1);
    this.ringing.set(false);
    setTimeout(() => this.ringing.set(true));
    setTimeout(() => this.ringing.set(false), 1200);
    this.notify.info(`🔔 ${n.title}`);
  }
}
