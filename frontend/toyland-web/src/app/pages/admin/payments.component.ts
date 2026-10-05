import { DatePipe } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSelectModule } from '@angular/material/select';
import { MatTabsModule } from '@angular/material/tabs';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { ChartConfiguration } from 'chart.js';
import { BaseChartDirective } from 'ng2-charts';
import { AdminApi } from '../../core/admin-api.service';
import { saveBlob } from '../../core/download';
import { ORDER_STATUS, PAYMENT_PROVIDER, TXN_STATUS } from '../../core/labels';
import { CodRow, Page, PaymentSummary, PaymentTxn, RefundQueueRow } from '../../core/models';
import { NotifyService } from '../../core/notify.service';
import { ThemeService } from '../../core/theme.service';
import { VndPipe } from '../../core/vnd.pipe';
import { PaymentActionData, PaymentActionDialogComponent } from '../../shared/payment-action-dialog.component';

type Tab = 'txns' | 'refunds' | 'cod';

function iso(d: Date): string {
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
}
const money = (v: number | string) => (Math.abs(+v) >= 1e6 ? Math.round(+v / 1e5) / 10 + 'tr' : Math.round(+v / 1e3) + 'k');

/** Thanh toan & doi soat: so thu - chi, hang doi hoan tien, don COD chua thu tien. */
@Component({
  selector: 'tl-payments',
  imports: [FormsModule, DatePipe, RouterLink, MatTabsModule, MatButtonModule, MatButtonToggleModule, MatFormFieldModule, MatInputModule,
    MatSelectModule, MatIconModule, MatTooltipModule, MatPaginatorModule, BaseChartDirective, VndPipe],
  template: `
    <div class="page-title"><h1>Thanh toán & đối soát</h1><span class="spacer"></span>
      <button mat-stroked-button (click)="export()" [disabled]="exporting()"><mat-icon>{{ exporting() ? 'hourglass_top' : 'download' }}</mat-icon>Xuất sổ thu - chi</button>
    </div>

    <div class="filters">
      <mat-button-toggle-group [value]="preset()" hideSingleSelectionIndicator aria-label="Khoảng thời gian nhanh">
        <mat-button-toggle value="today" (click)="setPreset('today')">Hôm nay</mat-button-toggle>
        <mat-button-toggle value="7" (click)="setPreset('7')">7 ngày</mat-button-toggle>
        <mat-button-toggle value="30" (click)="setPreset('30')">30 ngày</mat-button-toggle>
        <mat-button-toggle value="month" (click)="setPreset('month')">Tháng này</mat-button-toggle>
      </mat-button-toggle-group>
      <mat-form-field class="date"><mat-label>Từ ngày</mat-label><input matInput type="date" [(ngModel)]="from" (change)="preset.set(null); reload()" /></mat-form-field>
      <mat-form-field class="date"><mat-label>Đến ngày</mat-label><input matInput type="date" [(ngModel)]="to" (change)="preset.set(null); reload()" /></mat-form-field>
    </div>

    @if (s(); as s) {
      <div class="kpis">
        <div class="stat-card"><div class="ico k3"><mat-icon>account_balance_wallet</mat-icon></div>
          <div><div class="v">{{ s.net | vnd }}</div><div class="l">Thực thu (thu − hoàn) · {{ s.successCount }} giao dịch</div></div></div>
        <div class="stat-card"><div class="ico k2"><mat-icon>credit_card</mat-icon></div>
          <div><div class="v">{{ s.momoIn | vnd }}</div><div class="l">Ví MoMo · thành công {{ s.momoSuccessRate }}% ({{ s.momoFailed }} lỗi)</div></div></div>
        <div class="stat-card"><div class="ico k5"><mat-icon>local_shipping</mat-icon></div>
          <div><div class="v">{{ s.codIn + s.otherIn | vnd }}</div><div class="l">COD {{ s.codIn | vnd }} · tiền mặt/CK {{ s.otherIn | vnd }}</div></div></div>
        <div class="stat-card"><div class="ico k4"><mat-icon>currency_exchange</mat-icon></div>
          <div><div class="v">{{ s.refunded | vnd }}</div><div class="l">Đã hoàn tiền cho khách</div></div></div>
        <button type="button" class="stat-card alert" [class.hot]="s.refundPendingCount > 0" (click)="select('refunds')">
          <div class="ico k1"><mat-icon>pending_actions</mat-icon></div>
          <div><div class="v">{{ s.refundPendingCount }} đơn · {{ s.refundPendingAmount | vnd }}</div><div class="l">Chờ hoàn tiền →</div></div></button>
        <button type="button" class="stat-card alert" (click)="select('cod')">
          <div class="ico k6"><mat-icon>hourglass_top</mat-icon></div>
          <div><div class="v">{{ s.codOutstandingCount }} đơn · {{ s.codOutstandingAmount | vnd }}</div><div class="l">COD chưa thu (đang xử lý / giao) →</div></div></button>
      </div>
      @for (k of [theme.dark()]; track k) {
        <div class="card"><h3>Dòng tiền theo ngày</h3>
          <div class="chart-box"><canvas baseChart type="bar" [data]="chart()" [options]="chartOptions"></canvas></div></div>
      }
    }

    <mat-tab-group [selectedIndex]="tabs.indexOf(tab())" (selectedIndexChange)="select(tabs[$event])" animationDuration="200ms">
      <!-- ====================== SO THU - CHI ====================== -->
      <mat-tab label="Sổ thu - chi">
        <div class="filters inner">
          <mat-form-field class="search-field" subscriptSizing="dynamic"><mat-icon matPrefix>search</mat-icon>
            <input matInput [(ngModel)]="keyword" (keyup.enter)="loadTxns(0)" placeholder="Mã đơn, mã giao dịch, tên khách…" aria-label="Tìm giao dịch" />
            @if (keyword) { <button mat-icon-button matSuffix type="button" (click)="keyword = ''; loadTxns(0)" aria-label="Xoá từ khoá"><mat-icon>close</mat-icon></button> }
          </mat-form-field>
          <mat-form-field><mat-label>Hình thức</mat-label>
            <mat-select [(ngModel)]="provider" (selectionChange)="loadTxns(0)"><mat-option [value]="null">Tất cả</mat-option>
              @for (p of providers; track p[0]) { <mat-option [value]="p[0]">{{ p[1] }}</mat-option> }</mat-select></mat-form-field>
          <mat-form-field><mat-label>Loại</mat-label>
            <mat-select [(ngModel)]="kind" (selectionChange)="loadTxns(0)"><mat-option [value]="null">Thu & chi</mat-option>
              <mat-option value="PAYMENT">Thu tiền</mat-option><mat-option value="REFUND">Hoàn tiền</mat-option></mat-select></mat-form-field>
          <mat-form-field><mat-label>Trạng thái</mat-label>
            <mat-select [(ngModel)]="status" (selectionChange)="loadTxns(0)"><mat-option [value]="null">Tất cả</mat-option>
              @for (p of statuses; track p[0]) { <mat-option [value]="p[0]">{{ p[1] }}</mat-option> }</mat-select></mat-form-field>
        </div>
        <div class="card table-card">
          <table class="rep">
            <thead><tr><th>Thời gian</th><th>Đơn hàng</th><th>Hình thức</th><th>Mã giao dịch</th><th class="r">Số tiền</th><th>Trạng thái</th><th>Ghi chú</th></tr></thead>
            <tbody>
              @for (t of txns()?.content ?? []; track t.id) {
                <tr>
                  <td class="nowrap">{{ t.createdAt | date: 'HH:mm' }}<div class="small muted">{{ t.createdAt | date: 'dd/MM/yyyy' }}</div></td>
                  <td><a [routerLink]="['/admin/orders', t.orderId]" class="bold">{{ t.orderCode }}</a><div class="small muted">{{ t.customerName }}</div></td>
                  <td><span class="prov p-{{ t.provider }}">{{ providerLabel[t.provider] || t.provider }}</span>@if (t.bankCode) { <div class="small muted">{{ t.bankCode }}</div> }</td>
                  <td class="small mono">{{ t.transactionNo || '—' }}<div class="muted">{{ t.txnRef }}</div></td>
                  <td class="r bold" [class.pos]="t.kind === 'PAYMENT' && t.status === 'SUCCESS'" [class.neg]="t.kind === 'REFUND'">
                    {{ t.kind === 'REFUND' ? '−' : '+' }}{{ t.amount | vnd }}</td>
                  <td><span class="chip" [class.green]="t.status === 'SUCCESS'" [class.red]="t.status === 'FAILED'" [class.yellow]="t.status === 'PENDING'">
                    {{ t.kind === 'REFUND' ? 'Đã hoàn' : txnStatus[t.status] }}</span></td>
                  <td class="small note-cell">{{ t.note || '' }}@if (t.createdBy) { <div class="muted">bởi {{ t.createdBy }}</div> }</td>
                </tr>
              } @empty { <tr><td colspan="7" class="muted empty-row">Không có giao dịch phù hợp</td></tr> }
            </tbody>
          </table>
          <mat-paginator [length]="txns()?.totalElements ?? 0" [pageIndex]="txns()?.page ?? 0" [pageSize]="20" [hidePageSize]="true" (page)="goTxns($event)" />
        </div>
      </mat-tab>

      <!-- ====================== CHO HOAN TIEN ====================== -->
      <mat-tab [label]="'Chờ hoàn tiền (' + refunds().length + ')'">
        <p class="muted hint">Đơn đã thu tiền nhưng bị huỷ hoặc khách trả hàng. Chuyển tiền cho khách rồi bấm <b>Ghi nhận hoàn tiền</b> để đóng việc.</p>
        <div class="card table-card">
          <table class="rep">
            <thead><tr><th>Đơn hàng</th><th>Lý do</th><th class="r">Đã thu</th><th class="r">Đã hoàn</th><th class="r">Còn phải hoàn</th><th>Chờ từ</th><th></th></tr></thead>
            <tbody>
              @for (r of refunds(); track r.orderId) {
                <tr>
                  <td><a [routerLink]="['/admin/orders', r.orderId]" class="bold">{{ r.orderCode }}</a><div class="small muted">{{ r.customerName }} · {{ r.phone }}</div></td>
                  <td>{{ r.reason }}<div class="small muted">{{ orderStatus[r.orderStatus].label }} · {{ r.paymentMethod }}</div></td>
                  <td class="r">{{ r.paid | vnd }}</td><td class="r">{{ r.refunded | vnd }}</td><td class="r bold neg">{{ r.remaining | vnd }}</td>
                  <td class="small">{{ r.since | date: 'dd/MM HH:mm' }}</td>
                  <td class="r"><button mat-flat-button (click)="refund(r)"><mat-icon>currency_exchange</mat-icon>Ghi nhận hoàn tiền</button></td>
                </tr>
              } @empty { <tr><td colspan="7" class="empty-row"><mat-icon>task_alt</mat-icon> Không còn đơn nào chờ hoàn tiền</td></tr> }
            </tbody>
          </table>
        </div>
      </mat-tab>

      <!-- ====================== COD CHUA THU ====================== -->
      <mat-tab [label]="'COD chưa thu (' + (cod()?.totalElements ?? 0) + ')'">
        <p class="muted hint">Đơn COD đang xử lý hoặc đang giao, tiền sẽ tự ghi vào sổ khi đơn <b>Đã giao</b>. Nếu khách trả tiền mặt tại cửa hàng hoặc chuyển khoản trước, bấm <b>Ghi nhận đã thu</b>.</p>
        <div class="card table-card">
          <table class="rep">
            <thead><tr><th>Đơn hàng</th><th>Trạng thái</th><th>Ngày đặt</th><th class="r">Tiền cần thu</th><th></th></tr></thead>
            <tbody>
              @for (c of cod()?.content ?? []; track c.orderId) {
                <tr>
                  <td><a [routerLink]="['/admin/orders', c.orderId]" class="bold">{{ c.orderCode }}</a><div class="small muted">{{ c.customerName }} · {{ c.phone }}</div></td>
                  <td><span class="chip st-{{ c.orderStatus }}">{{ orderStatus[c.orderStatus].label }}</span></td>
                  <td class="small">{{ c.createdAt | date: 'dd/MM/yyyy HH:mm' }}</td>
                  <td class="r bold">{{ c.total | vnd }}</td>
                  <td class="r"><button mat-stroked-button (click)="collect(c)"><mat-icon>savings</mat-icon>Ghi nhận đã thu</button></td>
                </tr>
              } @empty { <tr><td colspan="5" class="empty-row"><mat-icon>task_alt</mat-icon> Không có đơn COD nào đang chờ thu</td></tr> }
            </tbody>
          </table>
          @if ((cod()?.totalPages ?? 0) > 1) {
            <mat-paginator [length]="cod()?.totalElements ?? 0" [pageIndex]="cod()?.page ?? 0" [pageSize]="20" [hidePageSize]="true" (page)="loadCod($event.pageIndex)" />
          }
        </div>
      </mat-tab>
    </mat-tab-group>
  `,
  styles: `
    .date { width: 160px; }
    .filters.inner { margin-top: 16px; }
    .kpis { display: grid; grid-template-columns: repeat(auto-fill, minmax(250px, 1fr)); gap: 14px; margin: 0 0 16px; }
    .stat-card.alert { border: 0; font: inherit; color: inherit; text-align: left; cursor: pointer; }
    .stat-card.alert.hot { outline: 2px solid color-mix(in srgb, var(--tl-red) 40%, transparent); }
    .ico.k1 { background: var(--tl-red-soft); color: var(--tl-red); } .ico.k2 { background: var(--tl-blue-soft); color: var(--tl-blue); }
    .ico.k3 { background: var(--tl-green-soft); color: var(--tl-green); } .ico.k4 { background: var(--tl-orange-soft); color: var(--tl-orange); }
    .ico.k5 { background: var(--tl-violet-soft); color: var(--tl-violet); } .ico.k6 { background: var(--tl-yellow-soft); color: var(--tl-yellow-ink); }
    .chart-box { position: relative; height: 260px; }
    .hint { margin: 16px 0 0; font-size: 14px; }
    .table-card { overflow-x: auto; margin-top: 12px; }
    table.rep { width: 100%; border-collapse: collapse; font-size: 14px; font-variant-numeric: tabular-nums; }
    table.rep th, table.rep td { padding: 10px 8px; border-bottom: 1px solid var(--tl-border); text-align: left; vertical-align: middle; }
    table.rep th { color: var(--tl-subtle); font-size: 12px; text-transform: uppercase; letter-spacing: .04em; background: var(--tl-table-head); white-space: nowrap; }
    .r { text-align: right !important; } .nowrap { white-space: nowrap; } .mono { font-family: ui-monospace, monospace; }
    .pos { color: var(--tl-green); } .neg { color: var(--tl-red-dark); }
    .note-cell { max-width: 260px; }
    .prov { display: inline-block; padding: 3px 10px; border-radius: 999px; font-size: 12px; font-weight: 800; background: var(--tl-hover); white-space: nowrap; }
    .p-MOMO { background: #fce4f1; color: #a50064; } .p-COD { background: var(--tl-violet-soft); color: var(--tl-violet); }
    .p-CASH { background: var(--tl-green-soft); color: var(--tl-green); } .p-BANK_TRANSFER { background: var(--tl-orange-soft); color: var(--tl-orange); }
    .empty-row { text-align: center; padding: 28px 8px !important; color: var(--tl-muted); }
    .empty-row mat-icon { vertical-align: middle; color: var(--tl-green); }
  `,
})
export class PaymentsComponent implements OnInit {
  private api = inject(AdminApi);
  private notify = inject(NotifyService);
  private dialog = inject(MatDialog);
  private route = inject(ActivatedRoute);
  theme = inject(ThemeService);

  tabs: Tab[] = ['txns', 'refunds', 'cod'];
  tab = signal<Tab>('txns');
  preset = signal<string | null>('30');
  from = '';
  to = '';
  keyword = '';
  provider: string | null = null;
  kind: string | null = null;
  status: string | null = null;
  exporting = signal(false);

  s = signal<PaymentSummary | null>(null);
  txns = signal<Page<PaymentTxn> | null>(null);
  refunds = signal<RefundQueueRow[]>([]);
  cod = signal<Page<CodRow> | null>(null);

  providerLabel = PAYMENT_PROVIDER;
  txnStatus = TXN_STATUS;
  orderStatus = ORDER_STATUS;
  providers = Object.entries(PAYMENT_PROVIDER);
  statuses = Object.entries(TXN_STATUS);

  chart = computed<ChartConfiguration<'bar'>['data']>(() => {
    const d = this.s()?.daily ?? [];
    return {
      labels: d.map((x) => `${x.date.slice(8, 10)}/${x.date.slice(5, 7)}`),
      datasets: [
        { label: 'Tiền thu', data: d.map((x) => x.in), backgroundColor: 'rgba(47,179,128,.8)', borderRadius: 8, maxBarThickness: 26 },
        { label: 'Tiền hoàn', data: d.map((x) => -x.out), backgroundColor: 'rgba(240,82,79,.75)', borderRadius: 8, maxBarThickness: 26 },
      ],
    };
  });
  chartOptions: ChartConfiguration<'bar'>['options'] = {
    responsive: true, maintainAspectRatio: false, interaction: { mode: 'index', intersect: false },
    scales: { x: { stacked: true, grid: { display: false } }, y: { stacked: true, ticks: { callback: (v) => money(v) } } },
  };

  ngOnInit(): void {
    const t = this.route.snapshot.queryParamMap.get('tab');
    if (t === 'refunds' || t === 'cod') this.tab.set(t);
    this.setPreset('30');
    this.loadQueues();
  }

  setPreset(p: string): void {
    this.preset.set(p);
    const now = new Date();
    const start = new Date();
    if (p === 'month') start.setDate(1);
    else if (p !== 'today') start.setDate(now.getDate() - (+p - 1));
    this.from = iso(start);
    this.to = iso(now);
    this.reload();
  }

  select(t: Tab): void {
    this.tab.set(t);
  }

  reload(): void {
    this.api.paymentSummary({ from: this.from, to: this.to }).subscribe({ next: (s) => this.s.set(s), error: (e) => this.notify.error(e) });
    this.loadTxns(0);
  }

  private q() {
    return { from: this.from, to: this.to, keyword: this.keyword.trim(), provider: this.provider, kind: this.kind, status: this.status };
  }

  loadTxns(page: number): void {
    this.api.paymentTxns({ ...this.q(), page, size: 20 }).subscribe({ next: (r) => this.txns.set(r), error: (e) => this.notify.error(e) });
  }

  goTxns(e: PageEvent): void {
    this.loadTxns(e.pageIndex);
  }

  loadQueues(): void {
    this.api.refundQueue().subscribe((r) => this.refunds.set(r));
    this.loadCod(0);
  }

  loadCod(page: number): void {
    this.api.codOutstanding({ page, size: 20 }).subscribe((r) => this.cod.set(r));
  }

  refund(r: RefundQueueRow): void {
    this.open({ mode: 'refund', orderId: r.orderId, orderCode: r.orderCode, customerName: r.customerName, amount: r.remaining, paymentMethod: r.paymentMethod });
  }

  collect(c: CodRow): void {
    this.open({ mode: 'collect', orderId: c.orderId, orderCode: c.orderCode, customerName: c.customerName, amount: c.total, paymentMethod: 'COD' });
  }

  private open(data: PaymentActionData): void {
    this.dialog.open(PaymentActionDialogComponent, { data, autoFocus: false }).afterClosed().subscribe((ok) => {
      if (ok) {
        this.loadQueues();
        this.reload();
      }
    });
  }

  export(): void {
    this.exporting.set(true);
    this.api.exportPaymentTxns(this.q()).subscribe({
      next: (res) => {
        this.exporting.set(false);
        this.notify.success(`Đã tải ${saveBlob(res, 'so-thu-chi.xlsx')}`);
      },
      error: (e) => {
        this.exporting.set(false);
        this.notify.error(e);
      },
    });
  }
}
