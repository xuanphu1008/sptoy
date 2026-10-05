import { DatePipe, DecimalPipe } from '@angular/common';
import { HttpResponse } from '@angular/common/http';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatTabsModule } from '@angular/material/tabs';
import { MatTooltipModule } from '@angular/material/tooltip';
import { RouterLink } from '@angular/router';
import { ChartConfiguration } from 'chart.js';
import { BaseChartDirective } from 'ng2-charts';
import { AdminApi } from '../../core/admin-api.service';
import {
  CustomerReportRow, InventoryReport, ProductReportRow, ReportGroupBy, SalesReport,
} from '../../core/models';
import { NotifyService } from '../../core/notify.service';
import { ThemeService } from '../../core/theme.service';
import { VndPipe } from '../../core/vnd.pipe';
import { saveBlob } from '../../core/download';

type Tab = 'sales' | 'products' | 'inventory' | 'customers';

function iso(d: Date): string {
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
}

const money = (v: number | string) => (+v >= 1e6 ? Math.round(+v / 1e5) / 10 + 'tr' : Math.round(+v / 1e3) + 'k');

/** Bao cao quan tri + xuat Excel (Apache POI o backend). */
@Component({
  selector: 'tl-reports',
  imports: [FormsModule, DatePipe, DecimalPipe, RouterLink, MatTabsModule, MatButtonModule, MatButtonToggleModule, MatFormFieldModule,
    MatInputModule, MatSelectModule, MatSlideToggleModule, MatIconModule, MatTooltipModule, BaseChartDirective, VndPipe],
  template: `
    <div class="page-title"><h1>Báo cáo</h1><span class="spacer"></span>
      <button mat-flat-button (click)="export()" [disabled]="exporting()"><mat-icon>{{ exporting() ? 'hourglass_top' : 'download' }}</mat-icon>Xuất Excel</button>
    </div>

    @if (tab() !== 'inventory') {
      <div class="filters">
        <mat-button-toggle-group [value]="preset()" hideSingleSelectionIndicator aria-label="Khoảng thời gian nhanh">
          <mat-button-toggle value="7" (click)="setPreset('7')">7 ngày</mat-button-toggle>
          <mat-button-toggle value="30" (click)="setPreset('30')">30 ngày</mat-button-toggle>
          <mat-button-toggle value="month" (click)="setPreset('month')">Tháng này</mat-button-toggle>
          <mat-button-toggle value="90" (click)="setPreset('90')">90 ngày</mat-button-toggle>
          <mat-button-toggle value="year" (click)="setPreset('year')">Năm nay</mat-button-toggle>
        </mat-button-toggle-group>
        <mat-form-field class="date"><mat-label>Từ ngày</mat-label><input matInput type="date" [(ngModel)]="from" (change)="preset.set(null); reload()" /></mat-form-field>
        <mat-form-field class="date"><mat-label>Đến ngày</mat-label><input matInput type="date" [(ngModel)]="to" (change)="preset.set(null); reload()" /></mat-form-field>
        @if (tab() === 'sales') {
          <mat-button-toggle-group [(ngModel)]="groupBy" (change)="reload()" hideSingleSelectionIndicator aria-label="Gom nhóm">
            <mat-button-toggle value="DAY">Theo ngày</mat-button-toggle><mat-button-toggle value="MONTH">Theo tháng</mat-button-toggle>
          </mat-button-toggle-group>
        }
        @if (tab() === 'products') {
          <mat-form-field><mat-label>Sắp xếp theo</mat-label>
            <mat-select [(ngModel)]="sort" (selectionChange)="reload()">
              <mat-option value="revenue">Doanh thu</mat-option><mat-option value="quantity">Số lượng bán</mat-option><mat-option value="profit">Lợi nhuận</mat-option>
            </mat-select></mat-form-field>
        }
      </div>
    }

    <mat-tab-group [selectedIndex]="tabs.indexOf(tab())" (selectedIndexChange)="select(tabs[$event])" animationDuration="200ms">
      <!-- ======================= DOANH THU ======================= -->
      <mat-tab label="Doanh thu & lợi nhuận">
        @if (sales(); as r) {
          <div class="kpis">
            <div class="stat-card"><div class="ico k1"><mat-icon>payments</mat-icon></div><div><div class="v">{{ r.total.netRevenue | vnd }}</div><div class="l">Doanh thu thuần · {{ r.total.orders }} đơn</div></div></div>
            <div class="stat-card"><div class="ico k2"><mat-icon>inventory</mat-icon></div><div><div class="v">{{ r.total.cost | vnd }}</div><div class="l">Giá vốn hàng bán</div></div></div>
            <div class="stat-card"><div class="ico k3"><mat-icon>trending_up</mat-icon></div><div><div class="v">{{ r.total.grossProfit | vnd }}</div><div class="l">Lợi nhuận gộp · biên {{ r.total.margin }}%</div></div></div>
            <div class="stat-card"><div class="ico k4"><mat-icon>sell</mat-icon></div><div><div class="v">{{ r.total.discounts | vnd }}</div><div class="l">Giảm giá (voucher + điểm)</div></div></div>
          </div>
          @for (k of [theme.dark()]; track k) {
            <div class="card"><h3>Doanh thu thuần và lợi nhuận gộp</h3>
              <div class="chart-box"><canvas baseChart type="bar" [data]="salesChart()" [options]="salesOptions"></canvas></div></div>
          }
          <div class="card table-card">
            <table class="rep">
              <thead><tr><th>{{ r.groupBy === 'MONTH' ? 'Tháng' : 'Ngày' }}</th><th class="r">Đơn</th><th class="r">SL bán</th><th class="r">Tiền hàng</th>
                <th class="r">Giảm giá</th><th class="r">DT thuần</th><th class="r">Giá vốn</th><th class="r">LN gộp</th><th class="r">Biên</th></tr></thead>
              <tbody>
                @for (x of nonEmptyRows(); track x.period) {
                  <tr><td>{{ fmtPeriod(x.period) }}</td><td class="r">{{ x.orders }}</td><td class="r">{{ x.itemsSold }}</td><td class="r">{{ x.goodsRevenue | vnd }}</td>
                    <td class="r">{{ x.discounts | vnd }}</td><td class="r bold">{{ x.netRevenue | vnd }}</td><td class="r">{{ x.cost | vnd }}</td>
                    <td class="r" [class.pos]="x.grossProfit > 0" [class.neg]="x.grossProfit < 0">{{ x.grossProfit | vnd }}</td><td class="r">{{ x.margin }}%</td></tr>
                } @empty { <tr><td colspan="9" class="muted">Không có đơn đã giao trong khoảng thời gian này</td></tr> }
              </tbody>
              <tfoot><tr><td>Tổng cộng</td><td class="r">{{ r.total.orders }}</td><td class="r">{{ r.total.itemsSold }}</td><td class="r">{{ r.total.goodsRevenue | vnd }}</td>
                <td class="r">{{ r.total.discounts | vnd }}</td><td class="r">{{ r.total.netRevenue | vnd }}</td><td class="r">{{ r.total.cost | vnd }}</td>
                <td class="r">{{ r.total.grossProfit | vnd }}</td><td class="r">{{ r.total.margin }}%</td></tr></tfoot>
            </table>
            <p class="small muted note">Tính trên đơn <b>đã giao / hoàn tất</b>, gom theo ngày đặt. Giá vốn lấy theo giá vốn tại thời điểm bán.</p>
          </div>
        }
      </mat-tab>

      <!-- ======================= SAN PHAM ======================= -->
      <mat-tab label="Theo sản phẩm">
        <div class="card table-card">
          <table class="rep">
            <thead><tr><th>#</th><th>Sản phẩm</th><th>Danh mục</th><th class="r">SL bán</th><th class="r">Doanh thu</th><th class="r">Giá vốn</th>
              <th class="r">Lợi nhuận</th><th class="r">Biên</th></tr></thead>
            <tbody>
              @for (p of products(); track $index; let i = $index) {
                <tr><td><span class="rank" [class.top]="i < 3">{{ i + 1 }}</span></td>
                  <td>@if (p.productId) { <a [routerLink]="['/admin/products', p.productId]">{{ p.productName }}</a> } @else { {{ p.productName }} }
                    <div class="small muted">{{ p.brand }}</div></td>
                  <td class="small">{{ p.category }}</td><td class="r">{{ p.quantity }}</td><td class="r bold">{{ p.revenue | vnd }}</td>
                  <td class="r">{{ p.cost | vnd }}</td><td class="r pos">{{ p.profit | vnd }}</td>
                  <td class="r"><span class="mbar"><span [style.width.%]="p.margin"></span></span>{{ p.margin }}%</td></tr>
              } @empty { <tr><td colspan="8" class="muted">Chưa có dữ liệu bán hàng</td></tr> }
            </tbody>
          </table>
        </div>
      </mat-tab>

      <!-- ======================= TON KHO ======================= -->
      <mat-tab label="Tồn kho">
        @if (inventory(); as inv) {
          <div class="kpis">
            <div class="stat-card"><div class="ico k2"><mat-icon>warehouse</mat-icon></div><div><div class="v">{{ inv.totalUnits | number }}</div><div class="l">Sản phẩm trong kho</div></div></div>
            <div class="stat-card"><div class="ico k1"><mat-icon>account_balance_wallet</mat-icon></div><div><div class="v">{{ inv.totalStockValue | vnd }}</div><div class="l">Giá trị tồn theo giá vốn</div></div></div>
            <div class="stat-card"><div class="ico k3"><mat-icon>storefront</mat-icon></div><div><div class="v">{{ inv.totalRetailValue | vnd }}</div><div class="l">Giá trị tồn theo giá bán</div></div></div>
            <a class="stat-card" routerLink="/admin/inventory" [queryParams]="{ lowStock: true }"><div class="ico k4"><mat-icon>report</mat-icon></div>
              <div><div class="v">{{ inv.lowStockCount }} · {{ inv.outOfStockCount }}</div><div class="l">Sắp hết · Hết hàng →</div></div></a>
          </div>
          <div class="filters">
            <mat-form-field class="search-field" subscriptSizing="dynamic"><mat-icon matPrefix>search</mat-icon>
              <input matInput [(ngModel)]="invKeyword" placeholder="Lọc theo SKU, tên, danh mục…" aria-label="Lọc tồn kho" /></mat-form-field>
            <mat-slide-toggle [(ngModel)]="invLowOnly">Chỉ sắp hết / hết hàng</mat-slide-toggle>
          </div>
          <div class="card table-card">
            <table class="rep">
              <thead><tr><th>SKU</th><th>Sản phẩm</th><th>Danh mục</th><th class="r">Tồn</th><th class="r">Giá vốn</th><th class="r">Giá trị tồn</th>
                <th class="r">Bán 30 ngày</th><th class="r">Đủ bán</th></tr></thead>
              <tbody>
                @for (v of invRows(); track v.variantId) {
                  <tr [class.low]="v.lowStock"><td class="small">{{ v.sku }}</td><td>{{ v.productName }}@if (v.variantName) { <div class="small muted">{{ v.variantName }}</div> }</td>
                    <td class="small">{{ v.category }}</td>
                    <td class="r"><span class="chip" [class.red]="v.stock === 0" [class.orange]="v.stock > 0 && v.lowStock">{{ v.stock }}</span></td>
                    <td class="r">{{ v.costPrice | vnd }}</td><td class="r bold">{{ v.stockValue | vnd }}</td><td class="r">{{ v.sold30d }}</td>
                    <td class="r small">{{ cover(v.stock, v.sold30d) }}</td></tr>
                } @empty { <tr><td colspan="8" class="muted">Không có biến thể phù hợp</td></tr> }
              </tbody>
            </table>
          </div>
        }
      </mat-tab>

      <!-- ======================= KHACH HANG ======================= -->
      <mat-tab label="Khách hàng">
        <div class="card table-card">
          <table class="rep">
            <thead><tr><th>#</th><th>Khách hàng</th><th class="r">Số đơn</th><th class="r">Tổng chi tiêu</th><th>Đơn gần nhất</th><th class="r">Điểm</th><th>Hạng</th></tr></thead>
            <tbody>
              @for (c of customers(); track c.userId; let i = $index) {
                <tr><td><span class="rank" [class.top]="i < 3">{{ i + 1 }}</span></td><td><b>{{ c.fullName }}</b><div class="small muted">{{ c.email }} · {{ c.phone }}</div></td>
                  <td class="r">{{ c.orders }}</td><td class="r bold">{{ c.spent | vnd }}</td><td class="small">{{ c.lastOrderAt | date: 'dd/MM/yyyy' }}</td>
                  <td class="r">{{ c.points | number }}</td><td><span class="chip" [class.yellow]="c.tier === 'Vàng'" [class.blue]="c.tier === 'Kim cương'" [class.grey]="c.tier === 'Bạc' || c.tier === 'Thành viên'">{{ c.tier }}</span></td></tr>
              } @empty { <tr><td colspan="7" class="muted">Chưa có dữ liệu</td></tr> }
            </tbody>
          </table>
        </div>
      </mat-tab>
    </mat-tab-group>
  `,
  styles: `
    .date { width: 160px; }
    .kpis { display: grid; grid-template-columns: repeat(auto-fill, minmax(230px, 1fr)); gap: 14px; margin: 16px 0; }
    .ico.k1 { background: var(--tl-red-soft); color: var(--tl-red); } .ico.k2 { background: var(--tl-blue-soft); color: var(--tl-blue); }
    .ico.k3 { background: var(--tl-green-soft); color: var(--tl-green); } .ico.k4 { background: var(--tl-orange-soft); color: var(--tl-orange); }
    .chart-box { position: relative; height: 300px; }
    .table-card { overflow-x: auto; margin-top: 16px; }
    table.rep { width: 100%; border-collapse: collapse; font-size: 14px; font-variant-numeric: tabular-nums; }
    table.rep th, table.rep td { padding: 9px 8px; border-bottom: 1px solid var(--tl-border); text-align: left; white-space: nowrap; }
    table.rep td:nth-child(2) { white-space: normal; }
    table.rep th { color: var(--tl-subtle); font-size: 12px; text-transform: uppercase; letter-spacing: .04em; background: var(--tl-table-head); }
    table.rep tfoot td { font-weight: 800; border-top: 2px solid var(--tl-border-strong); border-bottom: 0; }
    .r { text-align: right !important; }
    .pos { color: var(--tl-green); } .neg { color: var(--tl-red-dark); }
    tr.low td { background: color-mix(in srgb, var(--tl-orange-soft) 45%, transparent); }
    .rank { width: 26px; height: 26px; border-radius: 50%; display: inline-grid; place-items: center; background: var(--tl-hover); font-weight: 800; font-size: 12px; }
    .rank.top { background: var(--grad-sun); color: #5a3500; }
    .mbar { display: inline-block; width: 50px; height: 6px; border-radius: 999px; background: var(--tl-hover); margin-right: 6px; vertical-align: middle; overflow: hidden; }
    .mbar span { display: block; height: 100%; background: var(--tl-green); border-radius: inherit; }
    .note { margin: 10px 0 0; }
  `,
})
export class ReportsComponent implements OnInit {
  private api = inject(AdminApi);
  private notify = inject(NotifyService);
  theme = inject(ThemeService);
  tabs: Tab[] = ['sales', 'products', 'inventory', 'customers'];
  tab = signal<Tab>('sales');
  preset = signal<string | null>('30');
  from = '';
  to = '';
  groupBy: ReportGroupBy = 'DAY';
  sort = 'revenue';
  exporting = signal(false);

  sales = signal<SalesReport | null>(null);
  products = signal<ProductReportRow[]>([]);
  inventory = signal<InventoryReport | null>(null);
  customers = signal<CustomerReportRow[]>([]);
  invKeyword = '';
  invLowOnly = false;

  nonEmptyRows = computed(() => (this.sales()?.rows ?? []).filter((r) => r.orders > 0));
  salesChart = computed<ChartConfiguration<'bar'>['data']>(() => {
    const rows = this.sales()?.rows ?? [];
    return {
      labels: rows.map((r) => this.fmtPeriod(r.period, true)),
      datasets: [
        { label: 'Doanh thu thuần', data: rows.map((r) => r.netRevenue), backgroundColor: 'rgba(240,82,79,.75)', borderRadius: 8, maxBarThickness: 28 },
        { label: 'Lợi nhuận gộp', data: rows.map((r) => r.grossProfit), backgroundColor: 'rgba(47,179,128,.8)', borderRadius: 8, maxBarThickness: 28 },
      ],
    };
  });
  salesOptions: ChartConfiguration<'bar'>['options'] = {
    responsive: true, maintainAspectRatio: false, interaction: { mode: 'index', intersect: false },
    scales: { y: { beginAtZero: true, ticks: { callback: (v) => money(v) } }, x: { grid: { display: false } } },
  };

  ngOnInit(): void {
    this.setPreset('30');
  }

  invRows() {
    const kw = this.invKeyword.trim().toLowerCase();
    return (this.inventory()?.rows ?? []).filter((v) => (!this.invLowOnly || v.lowStock)
      && (!kw || `${v.sku} ${v.productName} ${v.variantName ?? ''} ${v.category}`.toLowerCase().includes(kw)));
  }

  setPreset(p: string): void {
    this.preset.set(p);
    const now = new Date();
    const start = new Date();
    if (p === 'month') start.setDate(1);
    else if (p === 'year') start.setMonth(0, 1);
    else start.setDate(now.getDate() - (+p - 1));
    this.groupBy = p === 'year' ? 'MONTH' : 'DAY';
    this.from = iso(start);
    this.to = iso(now);
    this.reload();
  }

  select(t: Tab): void {
    this.tab.set(t);
    this.reload();
  }

  reload(): void {
    const q = { from: this.from, to: this.to };
    switch (this.tab()) {
      case 'sales': this.api.salesReport({ ...q, groupBy: this.groupBy }).subscribe({ next: (r) => this.sales.set(r), error: (e) => this.notify.error(e) }); break;
      case 'products': this.api.productReport({ ...q, sort: this.sort, limit: 100 }).subscribe({ next: (r) => this.products.set(r), error: (e) => this.notify.error(e) }); break;
      case 'inventory': this.api.inventoryReport().subscribe({ next: (r) => this.inventory.set(r), error: (e) => this.notify.error(e) }); break;
      case 'customers': this.api.customerReport({ ...q, limit: 100 }).subscribe({ next: (r) => this.customers.set(r), error: (e) => this.notify.error(e) }); break;
    }
  }

  export(): void {
    const t = this.tab();
    const q: Record<string, unknown> = t === 'inventory' ? {} : { from: this.from, to: this.to };
    if (t === 'sales') q['groupBy'] = this.groupBy;
    if (t === 'products') q['sort'] = this.sort;
    this.exporting.set(true);
    this.api.exportReport(t, q).subscribe({
      next: (res: HttpResponse<Blob>) => {
        this.exporting.set(false);
        const name = saveBlob(res, `bao-cao-${t}.xlsx`);
        this.notify.success(`Đã tải ${name}`);
      },
      error: (e) => {
        this.exporting.set(false);
        this.notify.error(e);
      },
    });
  }

  /** "2026-09-28" -> "28/09" ; "2026-09" -> "09/2026" */
  fmtPeriod(p: string, short = false): string {
    if (p.length === 7) return `${p.slice(5, 7)}/${p.slice(0, 4)}`;
    return short ? `${p.slice(8, 10)}/${p.slice(5, 7)}` : `${p.slice(8, 10)}/${p.slice(5, 7)}/${p.slice(0, 4)}`;
  }

  /** So ngay du ban theo toc do ban 30 ngay gan nhat */
  cover(stock: number, sold30: number): string {
    if (stock === 0) return 'Hết hàng';
    if (!sold30) return '—';
    const days = Math.round(stock / (sold30 / 30));
    return days > 365 ? '> 1 năm' : `${days} ngày`;
  }
}
