import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatTabsModule } from '@angular/material/tabs';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ShopApi } from '../../core/api.service';
import { PAYMENT_METHOD, PLACEHOLDER_IMG } from '../../core/labels';
import { OrderStatus, OrderSummary, Page } from '../../core/models';
import { VndPipe } from '../../core/vnd.pipe';

const TABS: { status: OrderStatus | null; label: string }[] = [
  { status: null, label: 'Tất cả' },
  { status: 'PENDING_PAYMENT', label: 'Chờ thanh toán' },
  { status: 'PENDING', label: 'Chờ xác nhận' },
  { status: 'CONFIRMED', label: 'Đã xác nhận' },
  { status: 'PACKING', label: 'Đóng gói' },
  { status: 'SHIPPING', label: 'Đang giao' },
  { status: 'DELIVERED', label: 'Đã giao' },
  { status: 'COMPLETED', label: 'Hoàn tất' },
  { status: 'CANCELLED', label: 'Đã huỷ' },
  { status: 'RETURN_REQUESTED', label: 'Trả hàng' },
];

@Component({
  selector: 'tl-orders',
  imports: [RouterLink, DatePipe, MatTabsModule, MatButtonModule, MatIconModule, MatPaginatorModule, VndPipe],
  template: `
    <div class="card tabs-card">
      <mat-tab-group [selectedIndex]="tabIndex()" (selectedIndexChange)="selectTab($event)" mat-stretch-tabs="false" animationDuration="0ms">
        @for (t of tabs; track t.label) { <mat-tab [label]="t.label" /> }
      </mat-tab-group>
    </div>
    @if (page(); as p) {
      @for (o of p.content; track o.id) {
        <a class="card order" [routerLink]="[o.id]">
          <div class="row between head">
            <span><b>{{ o.orderCode }}</b> <span class="muted small">· {{ o.createdAt | date: 'dd/MM/yyyy HH:mm' }}</span></span>
            <span class="chip st-{{ o.status }}">{{ o.statusLabel }}</span>
          </div>
          <div class="line-item">
            <img [src]="o.firstItemImage || ph" alt="" />
            <div class="info">
              <div class="name">{{ o.firstItemName }}</div>
              <div class="variant">Tổng {{ o.itemCount }} sản phẩm</div>
              <div class="small muted">{{ pm[o.paymentMethod] }}</div>
            </div>
            <div class="right">
              <div class="small muted">Thành tiền</div>
              <div class="price big">{{ o.totalAmount | vnd }}</div>
            </div>
          </div>
          <div class="row end">
            @if (o.status === 'PENDING_PAYMENT') { <span class="chip yellow">Chờ bạn thanh toán</span> }
            @if (o.status === 'DELIVERED') { <span class="chip green">Đánh giá để giúp người mua khác</span> }
            <span class="link">Xem chi tiết →</span>
          </div>
        </a>
      } @empty {
        <div class="card empty"><mat-icon>receipt_long</mat-icon><p>Chưa có đơn hàng nào</p><a mat-flat-button routerLink="/products">Mua sắm ngay</a></div>
      }
      @if (p.totalPages > 1) {
        <mat-paginator [length]="p.totalElements" [pageIndex]="p.page" [pageSize]="p.size" [hidePageSize]="true" (page)="go($event)" />
      }
    }
  `,
  styles: `
    .tabs-card { padding: 0 8px; margin-bottom: 12px; }
    .order { display: block; margin-top: 12px; transition: box-shadow .2s; }
    .order:hover { box-shadow: var(--tl-shadow-hover); }
    .head { border-bottom: 1px solid #f0f0f0; padding-bottom: 10px; }
    .right { text-align: right; }
    .price.big { font-size: 18px; }
    .link { color: var(--tl-red); font-weight: 700; font-size: 14px; }
    mat-paginator { margin-top: 12px; border-radius: 12px; }
  `,
})
export class OrdersComponent implements OnInit {
  private api = inject(ShopApi);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  tabs = TABS;
  pm = PAYMENT_METHOD;
  ph = PLACEHOLDER_IMG;
  tabIndex = signal(0);
  page = signal<Page<OrderSummary> | null>(null);

  ngOnInit(): void {
    this.route.queryParams.subscribe((q) => {
      const idx = Math.max(0, TABS.findIndex((t) => t.status === (q['status'] ?? null)));
      this.tabIndex.set(idx);
      this.api.myOrders({ status: TABS[idx].status, page: q['page'] ?? 0, size: 10 }).subscribe((p) => this.page.set(p));
    });
  }

  selectTab(i: number): void {
    this.router.navigate([], { queryParams: { status: TABS[i].status, page: null } });
  }

  go(e: PageEvent): void {
    this.router.navigate([], { queryParams: { page: e.pageIndex }, queryParamsHandling: 'merge' });
  }
}
