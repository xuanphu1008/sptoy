import { DatePipe, DecimalPipe } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { RouterLink } from '@angular/router';
import { ChartConfiguration } from 'chart.js';
import { BaseChartDirective } from 'ng2-charts';
import { AdminApi } from '../../core/admin-api.service';
import { Dashboard } from '../../core/models';
import { ThemeService } from '../../core/theme.service';
import { VndPipe } from '../../core/vnd.pipe';

/** Bang mau bieu do hai hoa voi pastel cua giao dien (du dam de phan biet, du mem de khong choi) */
const COLORS = ['#f0524f', '#4f8ef7', '#2fb380', '#f5b82e', '#8b6cf0', '#f58b5b', '#20b2c4', '#ec6fa0', '#7c88a6', '#b5cf3a', '#5b5378'];

function iso(d: Date): string {
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
}

@Component({
  selector: 'tl-dashboard',
  imports: [RouterLink, FormsModule, DatePipe, DecimalPipe, MatButtonModule, MatButtonToggleModule, MatDatepickerModule, MatFormFieldModule,
    MatInputModule, MatIconModule, BaseChartDirective, VndPipe],
  templateUrl: './dashboard.component.html',
  styles: `
    .stats { display: grid; grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); gap: 14px; margin-bottom: 16px; }
    .charts { display: grid; grid-template-columns: 2fr 1fr; gap: 16px; }
    .charts .card { margin-top: 0; }
    .chart-box { position: relative; height: 300px; }
    .ico.c-red { background: var(--tl-red-soft); color: var(--tl-red); } .ico.c-blue { background: var(--tl-blue-soft); color: var(--tl-blue); }
    .ico.c-green { background: var(--tl-green-soft); color: var(--tl-green); } .ico.c-orange { background: var(--tl-orange-soft); color: var(--tl-orange); }
    .ico.c-violet { background: var(--tl-violet-soft); color: var(--tl-violet); } .ico.c-yellow { background: var(--tl-yellow-soft); color: var(--tl-yellow-ink); }
    .ico.c-grey { background: var(--mat-sys-surface-container-highest); color: var(--tl-muted); }
    .card h3 { font-size: 18px; }
    .chart-box.sm { height: 260px; }
    table.simple { width: 100%; border-collapse: collapse; font-size: 14px; }
    table.simple td, table.simple th { padding: 10px 6px; border-bottom: 1px solid var(--tl-border); text-align: left; }
    table.simple th { color: var(--tl-muted); font-weight: 600; font-size: 13px; }
    .r { text-align: right !important; }
    .rank { width: 24px; height: 24px; border-radius: 50%; background: var(--tl-red-soft); color: var(--tl-red); display: inline-grid; place-items: center; font-weight: 800; font-size: 12px; }
    .range mat-form-field { width: 250px; }
    @media (max-width: 1100px) { .charts { grid-template-columns: minmax(0, 1fr); } }
  `,
})
export class DashboardComponent implements OnInit {
  private api = inject(AdminApi);
  theme = inject(ThemeService);
  d = signal<Dashboard | null>(null);
  preset = signal<number | null>(30);
  from: Date | null = null;
  to: Date | null = null;

  revenueChart = computed<ChartConfiguration<'line'>['data']>(() => {
    const s = this.d()?.revenueSeries ?? [];
    return {
      labels: s.map((p) => p.date.slice(8, 10) + '/' + p.date.slice(5, 7)),
      datasets: [
        { label: 'Doanh thu (₫)', data: s.map((p) => p.revenue), borderColor: '#f0524f', backgroundColor: 'rgba(240,82,79,.14)', fill: true, tension: .4, cubicInterpolationMode: 'monotone', yAxisID: 'y', pointRadius: 0, pointHoverRadius: 5, borderWidth: 3 },
        { label: 'Số đơn', data: s.map((p) => p.orders), borderColor: '#8b6cf0', backgroundColor: '#8b6cf0', tension: .4, cubicInterpolationMode: 'monotone', yAxisID: 'y1', type: 'line', pointRadius: 0, pointHoverRadius: 5, borderWidth: 2, borderDash: [6, 4] },
      ],
    };
  });
  revenueOptions: ChartConfiguration<'line'>['options'] = {
    responsive: true, maintainAspectRatio: false, interaction: { mode: 'index', intersect: false },
    scales: {
      y: { beginAtZero: true, ticks: { callback: (v) => (+v >= 1e6 ? +v / 1e6 + 'tr' : +v / 1e3 + 'k') } },
      y1: { beginAtZero: true, position: 'right', grid: { drawOnChartArea: false }, ticks: { precision: 0 } },
    },
  };

  statusChart = computed<ChartConfiguration<'doughnut'>['data']>(() => {
    const s = (this.d()?.statusCounts ?? []).filter((x) => x.count > 0);
    return { labels: s.map((x) => x.label), datasets: [{ data: s.map((x) => x.count), backgroundColor: COLORS, borderColor: this.theme.dark() ? '#1f1a2e' : '#fff', borderWidth: 3, hoverOffset: 6 }] };
  });
  categoryChart = computed<ChartConfiguration<'bar'>['data']>(() => {
    const s = this.d()?.categoryRevenue ?? [];
    return { labels: s.map((x) => x.label), datasets: [{ label: 'Doanh thu', data: s.map((x) => x.amount ?? 0), backgroundColor: COLORS, borderRadius: 10, borderSkipped: false, barThickness: 16 }] };
  });
  paymentChart = computed<ChartConfiguration<'doughnut'>['data']>(() => {
    const s = this.d()?.paymentShare ?? [];
    return { labels: s.map((x) => x.label), datasets: [{ data: s.map((x) => x.count), backgroundColor: ['#2fb380', '#4f8ef7'], borderColor: this.theme.dark() ? '#1f1a2e' : '#fff', borderWidth: 3, hoverOffset: 6 }] };
  });
  pieOptions: ChartConfiguration<'doughnut'>['options'] = { responsive: true, maintainAspectRatio: false, cutout: '62%', plugins: { legend: { position: 'right', labels: { usePointStyle: true, pointStyle: 'circle', padding: 14 } } } };
  barOptions: ChartConfiguration<'bar'>['options'] = {
    responsive: true, maintainAspectRatio: false, indexAxis: 'y', plugins: { legend: { display: false } },
    scales: { x: { ticks: { callback: (v) => (+v >= 1e6 ? +v / 1e6 + 'tr' : +v / 1e3 + 'k') } } },
  };

  ngOnInit(): void {
    this.setPreset(30);
  }

  setPreset(days: number): void {
    this.preset.set(days);
    const to = new Date();
    const from = new Date();
    from.setDate(to.getDate() - (days - 1));
    this.from = from;
    this.to = to;
    this.load();
  }

  custom(): void {
    if (!this.from || !this.to) return;
    this.preset.set(null);
    this.load();
  }

  load(): void {
    this.api.dashboard({ from: this.from ? iso(this.from) : null, to: this.to ? iso(this.to) : null }).subscribe((d) => this.d.set(d));
  }
}
