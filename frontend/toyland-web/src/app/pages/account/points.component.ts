import { DatePipe, DecimalPipe } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { RouterLink } from '@angular/router';
import { ShopApi } from '../../core/api.service';
import { POINT_TYPE } from '../../core/labels';
import { LoyaltySummary, Page, PointTx } from '../../core/models';
import { VndPipe } from '../../core/vnd.pipe';

const TIERS = [
  { code: 'MEMBER', label: 'Thành viên', min: 0, x: '1.0', icon: 'sentiment_satisfied', bg: 'var(--p-sky)' },
  { code: 'SILVER', label: 'Bạc', min: 500, x: '1.1', icon: 'workspace_premium', bg: 'var(--p-lilac)' },
  { code: 'GOLD', label: 'Vàng', min: 1500, x: '1.25', icon: 'military_tech', bg: 'var(--p-butter)' },
  { code: 'DIAMOND', label: 'Kim cương', min: 4000, x: '1.5', icon: 'diamond', bg: 'var(--p-mint)' },
];

/** Diem thuong cua toi: so du, hang thanh vien, cach tich / dung diem, lich su. */
@Component({
  selector: 'tl-points',
  imports: [DatePipe, DecimalPipe, RouterLink, MatIconModule, MatPaginatorModule, VndPipe],
  template: `
    <h2 class="mt-0">Điểm thưởng</h2>
    @if (s(); as s) {
      <section class="hero">
        <div class="coin" aria-hidden="true"><mat-icon>stars</mat-icon></div>
        <div class="grow">
          <div class="small">Điểm hiện có</div>
          <div class="big">{{ s.points | number }} <small>điểm</small></div>
          <div>≈ <b>{{ s.pointsValue | vnd }}</b> giảm giá cho đơn tiếp theo</div>
        </div>
        <div class="tier-badge"><mat-icon>{{ tierIcon() }}</mat-icon><b>{{ s.tier.label }}</b><small>x{{ s.tier.multiplier }} điểm</small></div>
      </section>

      <section class="card">
        @if (s.nextTier; as n) {
          <div class="row between"><b>Còn {{ s.pointsToNextTier | number }} điểm tích luỹ để lên hạng {{ n.label }}</b>
            <span class="muted small">{{ s.lifetimePoints | number }} / {{ n.minPoints | number }}</span></div>
          <div class="bar" role="progressbar" [attr.aria-valuenow]="progress()" aria-valuemin="0" aria-valuemax="100"><span [style.width.%]="progress()"></span></div>
        } @else {
          <b>Bạn đang ở hạng cao nhất — cảm ơn bạn đã luôn đồng hành cùng ToyLand!</b>
        }
        <div class="ladder">
          @for (t of tiers; track t.code) {
            <div class="step" [class.on]="t.code === s.tier.code" [class.done]="s.lifetimePoints >= t.min" [style.--c]="t.bg">
              <span class="ic"><mat-icon>{{ t.icon }}</mat-icon></span><b>{{ t.label }}</b><small>từ {{ t.min | number }} điểm · x{{ t.x }}</small>
            </div>
          }
        </div>
      </section>

      <section class="how">
        <div class="card"><span class="n">1</span><b>Mua sắm</b><p>Mỗi {{ s.earnPerAmount | vnd }} tiền hàng (sau giảm giá) = 1 điểm, nhân hệ số theo hạng.</p></div>
        <div class="card"><span class="n">2</span><b>Nhận điểm</b><p>Điểm được cộng khi đơn <b>hoàn tất</b> (hết thời hạn đổi trả).</p></div>
        <div class="card"><span class="n">3</span><b>Dùng điểm</b><p>1 điểm = {{ s.pointValue }}đ, dùng tối đa {{ s.maxRedeemPercent }}% giá trị hàng ở bước thanh toán.</p></div>
      </section>
    }

    <section class="card">
      <h3>Lịch sử điểm</h3>
      @for (t of history()?.content ?? []; track t.id) {
        <div class="tx">
          <span class="ti t-{{ t.type }}"><mat-icon>{{ icon[t.type] }}</mat-icon></span>
          <div class="grow"><b>{{ types[t.type] }}</b><div class="small muted">{{ t.note }} · {{ t.createdAt | date: 'dd/MM/yyyy HH:mm' }}</div></div>
          <div class="r"><b [class.plus]="t.points > 0" [class.minus]="t.points < 0">{{ t.points > 0 ? '+' : '' }}{{ t.points | number }}</b>
            <div class="small muted">còn {{ t.balanceAfter | number }}</div></div>
        </div>
      } @empty {
        <div class="empty"><mat-icon>savings</mat-icon><p>Chưa có giao dịch điểm. <a routerLink="/products">Mua sắm ngay</a> để bắt đầu tích điểm!</p></div>
      }
      @if ((history()?.totalPages ?? 0) > 1) {
        <mat-paginator [length]="history()?.totalElements ?? 0" [pageIndex]="history()?.page ?? 0" [pageSize]="10" [hidePageSize]="true" (page)="go($event)" />
      }
    </section>
  `,
  styles: `
    .hero { display: flex; align-items: center; gap: 18px; padding: 22px 24px; border-radius: var(--tl-radius); margin-bottom: 16px;
      background: radial-gradient(120% 140% at 100% 0%, var(--p-butter), var(--p-peach) 55%, var(--p-pink)); box-shadow: var(--clay); }
    .coin { width: 72px; height: 72px; border-radius: 50%; display: grid; place-items: center; background: var(--grad-sun); color: #fff;
      box-shadow: var(--clay-btn), inset 0 -6px 0 rgb(0 0 0 / 10%); animation: tl-float 5s ease-in-out infinite; flex: none; }
    .coin mat-icon { font-size: 40px; width: 40px; height: 40px; }
    .big { font-family: var(--font-display); font-size: 40px; line-height: 1.1; font-weight: 800; }
    .big small { font-size: 16px; }
    .grow { flex: 1; min-width: 0; }
    .tier-badge { display: flex; flex-direction: column; align-items: center; gap: 2px; padding: 12px 16px; border-radius: 20px;
      background: rgb(255 255 255 / 70%); box-shadow: var(--clay-sm); }
    .tier-badge mat-icon { font-size: 30px; width: 30px; height: 30px; color: var(--tl-orange); }
    .tier-badge small { color: var(--tl-muted); }
    .bar { height: 12px; border-radius: 999px; background: var(--tl-hover); overflow: hidden; margin: 10px 0 16px; }
    .bar span { display: block; height: 100%; border-radius: inherit; background: var(--grad-cta); transition: width .6s var(--ease-bounce); }
    .ladder { display: grid; grid-template-columns: repeat(4, 1fr); gap: 10px; }
    .step { display: flex; flex-direction: column; align-items: center; text-align: center; gap: 4px; padding: 12px 8px; border-radius: 18px;
      background: var(--tl-hover); opacity: .7; }
    .step.done { opacity: 1; }
    .step.on { background: var(--c); box-shadow: var(--clay-sm); opacity: 1; }
    .step .ic { width: 40px; height: 40px; border-radius: 14px; display: grid; place-items: center; background: var(--tl-card); }
    .step small { color: var(--tl-muted); font-size: 12px; }
    .how { display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px; margin: 16px 0; }
    .how .card { margin: 0; position: relative; padding-top: 22px; }
    .how p { margin: 6px 0 0; font-size: 14px; color: var(--tl-muted); }
    .n { position: absolute; top: -10px; left: 16px; width: 28px; height: 28px; border-radius: 50%; background: var(--grad-cta); color: #fff;
      display: grid; place-items: center; font-weight: 800; box-shadow: var(--clay-sm); }
    .tx { display: flex; align-items: center; gap: 12px; padding: 10px 0; border-bottom: 1px dashed var(--tl-border); }
    .ti { width: 38px; height: 38px; border-radius: 12px; display: grid; place-items: center; background: var(--p-sky); flex: none; }
    .t-EARN { background: var(--p-mint); } .t-REDEEM { background: var(--p-peach); } .t-REFUND { background: var(--p-butter); } .t-ADJUST { background: var(--p-lilac); }
    .r { text-align: right; } .plus { color: var(--tl-green); } .minus { color: var(--tl-red-dark); }
    @media (max-width: 700px) {
      .hero { flex-wrap: wrap; } .ladder { grid-template-columns: repeat(2, 1fr); } .how { grid-template-columns: minmax(0, 1fr); }
    }
  `,
})
export class PointsComponent implements OnInit {
  private api = inject(ShopApi);
  s = signal<LoyaltySummary | null>(null);
  history = signal<Page<PointTx> | null>(null);
  tiers = TIERS;
  types = POINT_TYPE;
  icon: Record<string, string> = { EARN: 'add_circle', REDEEM: 'shopping_cart_checkout', REFUND: 'undo', ADJUST: 'tune' };

  tierIcon = computed(() => TIERS.find((t) => t.code === this.s()?.tier.code)?.icon ?? 'stars');
  progress = computed(() => {
    const s = this.s();
    if (!s?.nextTier) return 100;
    const from = s.tier.minPoints;
    return Math.min(100, Math.round(((s.lifetimePoints - from) / (s.nextTier.minPoints - from)) * 100));
  });

  ngOnInit(): void {
    this.api.myPoints().subscribe((s) => this.s.set(s));
    this.load(0);
  }

  load(p: number): void {
    this.api.myPointHistory({ page: p, size: 10 }).subscribe((h) => this.history.set(h));
  }

  go(e: PageEvent): void {
    this.load(e.pageIndex);
  }
}
