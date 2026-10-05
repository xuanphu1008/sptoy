import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { forkJoin } from 'rxjs';
import { AdminApi } from '../../core/admin-api.service';
import { ShopApi } from '../../core/api.service';
import { CAMPAIGN_SCOPE } from '../../core/labels';
import { Campaign, CampaignScope } from '../../core/models';
import { NotifyService } from '../../core/notify.service';
import { confirmDialog } from '../../shared/confirm-dialog.component';
import { FieldDef, openForm } from '../../shared/form-dialog.component';
import { flattenCategories } from './products.component';

type Opt = { value: unknown; label: string };

function localNow(offsetDays = 0): string {
  const d = new Date(Date.now() + offsetDays * 86400000);
  d.setMinutes(d.getMinutes() - d.getTimezoneOffset());
  return d.toISOString().slice(0, 16);
}

/** Chuong trinh giam gia tu dong theo pham vi (khong can ma) - giong Flash Sale / Mega Sale */
@Component({
  selector: 'tl-campaigns',
  imports: [DatePipe, MatTableModule, MatButtonModule, MatIconModule],
  template: `
    <div class="page-title"><h1>Chương trình khuyến mãi</h1><span class="spacer"></span>
      <button mat-flat-button (click)="edit(null)"><mat-icon>add</mat-icon>Tạo chương trình</button></div>
    <p class="muted">Giảm giá tự động theo phạm vi (toàn shop, danh mục, thương hiệu, nhân vật hoặc 1 sản phẩm). Nếu nhiều chương trình trùng nhau, áp dụng mức giảm cao nhất.</p>
    <div class="table-wrap">
      <table mat-table [dataSource]="list()">
        <ng-container matColumnDef="name"><th mat-header-cell *matHeaderCellDef>Chương trình</th>
          <td mat-cell *matCellDef="let c"><b>{{ c.name }}</b>@if (c.badgeLabel) { <span class="chip orange">{{ c.badgeLabel }}</span> }<div class="small muted">{{ c.description }}</div></td></ng-container>
        <ng-container matColumnDef="discount"><th mat-header-cell *matHeaderCellDef>Giảm</th><td mat-cell *matCellDef="let c" class="bold price">-{{ c.discountPercent }}%</td></ng-container>
        <ng-container matColumnDef="scope"><th mat-header-cell *matHeaderCellDef>Áp dụng cho</th>
          <td mat-cell *matCellDef="let c">{{ scope[c.scopeType] }}@if (c.scopeType !== 'ALL') { : <b>{{ c.scopeName }}</b> }</td></ng-container>
        <ng-container matColumnDef="time"><th mat-header-cell *matHeaderCellDef>Thời gian</th>
          <td mat-cell *matCellDef="let c" class="small">{{ c.startAt | date: 'dd/MM/yy HH:mm' }}<br />→ {{ c.endAt | date: 'dd/MM/yy HH:mm' }}</td></ng-container>
        <ng-container matColumnDef="status"><th mat-header-cell *matHeaderCellDef>Trạng thái</th>
          <td mat-cell *matCellDef="let c">
            @if (c.running) { <span class="chip green">Đang chạy</span> } @else if (!c.active) { <span class="chip grey">Tắt</span> } @else { <span class="chip yellow">Chưa/đã hết hạn</span> }
          </td></ng-container>
        <ng-container matColumnDef="actions"><th mat-header-cell *matHeaderCellDef></th>
          <td mat-cell *matCellDef="let c" class="nowrap"><div class="cell-actions"><button mat-icon-button (click)="edit(c)" aria-label="Sửa"><mat-icon>edit</mat-icon></button>
            <button mat-icon-button class="danger-icon" (click)="remove(c)" aria-label="Xoá"><mat-icon>delete</mat-icon></button></div></td></ng-container>
        <tr mat-header-row *matHeaderRowDef="cols"></tr>
        <tr mat-row *matRowDef="let row; columns: cols"></tr>
      </table>
    </div>
  `,
})
export class CampaignsComponent implements OnInit {
  private api = inject(AdminApi);
  private shop = inject(ShopApi);
  private dialog = inject(MatDialog);
  private notify = inject(NotifyService);
  scope: Record<string, string> = CAMPAIGN_SCOPE;
  cols = ['name', 'discount', 'scope', 'time', 'status', 'actions'];
  list = signal<Campaign[]>([]);
  private opts: Record<string, Opt[]> = {};

  ngOnInit(): void {
    this.load();
    forkJoin({
      cats: this.api.categories(), brands: this.shop.brands(), fr: this.shop.franchises(), products: this.api.products({ page: 0, size: 500, sort: 'name' }),
    }).subscribe((r) => {
      this.opts = {
        CATEGORY: flattenCategories(r.cats).map((c) => ({ value: c.id, label: '— '.repeat(c.depth) + c.name })),
        BRAND: r.brands.map((b) => ({ value: b.id, label: b.name })),
        FRANCHISE: r.fr.map((f) => ({ value: f.id, label: f.name })),
        PRODUCT: r.products.content.map((p) => ({ value: p.id, label: p.name })),
      };
    });
  }

  load(): void {
    this.api.campaigns().subscribe((l) => this.list.set(l));
  }

  edit(c: Campaign | null): void {
    const value: Record<string, unknown> = c ? { ...c } : {
      name: '', description: '', discountPercent: 10, scopeType: 'ALL', scopeId: null, badgeLabel: 'Sale', startAt: localNow(), endAt: localNow(7), active: true,
    };
    // Moi loai pham vi co 1 o chon rieng -> gop lai thanh scopeId khi luu
    const scopeFields: FieldDef[] = (['CATEGORY', 'BRAND', 'FRANCHISE', 'PRODUCT'] as CampaignScope[]).map((t) => {
      value['scope_' + t] = c?.scopeType === t ? c.scopeId : null;
      return { key: 'scope_' + t, label: `Chọn ${CAMPAIGN_SCOPE[t].toLowerCase()}`, type: 'select' as const, options: this.opts[t] ?? [], required: true, showIf: (x: Record<string, unknown>) => x['scopeType'] === t };
    });
    openForm(this.dialog, {
      title: c ? `Sửa: ${c.name}` : 'Tạo chương trình khuyến mãi',
      value,
      fields: [
        { key: 'name', label: 'Tên chương trình', required: true, wide: true },
        { key: 'discountPercent', label: 'Giảm (%)', type: 'number', required: true },
        { key: 'badgeLabel', label: 'Nhãn hiển thị', hint: 'vd: Flash Sale, Trung thu' },
        { key: 'scopeType', label: 'Phạm vi', type: 'select', options: Object.entries(CAMPAIGN_SCOPE).map(([v, l]) => ({ value: v, label: l })) },
        ...scopeFields,
        { key: 'startAt', label: 'Bắt đầu', type: 'datetime', required: true },
        { key: 'endAt', label: 'Kết thúc', type: 'datetime', required: true },
        { key: 'active', label: 'Kích hoạt', type: 'toggle' },
        { key: 'description', label: 'Mô tả', type: 'textarea' },
      ],
      save: (x) => {
        const t = x['scopeType'] as CampaignScope;
        const body = {
          name: x['name'], description: x['description'], discountPercent: x['discountPercent'], scopeType: t,
          scopeId: t === 'ALL' ? null : x['scope_' + t], badgeLabel: x['badgeLabel'], startAt: x['startAt'], endAt: x['endAt'], active: x['active'],
        };
        return this.api.saveCampaign(c?.id ?? null, body);
      },
    }).subscribe((r) => r && this.load());
  }

  remove(c: Campaign): void {
    confirmDialog(this.dialog, { title: `Xoá chương trình "${c.name}"?`, danger: true, confirmText: 'Xoá' })
      .subscribe((ok) => ok && this.api.deleteCampaign(c.id).subscribe({ next: (r) => { this.notify.success(r.message); this.load(); }, error: (e) => this.notify.error(e) }));
  }
}
