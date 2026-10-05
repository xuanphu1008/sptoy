import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { AdminApi } from '../../core/admin-api.service';
import { Banner } from '../../core/models';
import { NotifyService } from '../../core/notify.service';
import { confirmDialog } from '../../shared/confirm-dialog.component';
import { openForm } from '../../shared/form-dialog.component';

@Component({
  selector: 'tl-banners',
  imports: [DatePipe, MatButtonModule, MatIconModule],
  template: `
    <div class="page-title"><h1>Banner trang chủ</h1><span class="spacer"></span>
      <button mat-flat-button (click)="edit(null)"><mat-icon>add</mat-icon>Thêm banner</button></div>
    <p class="muted">Banner không có ảnh sẽ hiển thị bằng màu nền. Kích thước ảnh khuyến nghị 1280×340.</p>
    <div class="list">
      @for (b of list(); track b.id) {
        <div class="card bn" [class.off]="!b.active">
          <div class="preview" [style.background]="b.imageUrl ? 'url(' + b.imageUrl + ') center/cover' : (b.bgColor || '#E53935')">
            <div><h3>{{ b.title }}</h3><p>{{ b.subtitle }}</p><span class="btn">{{ b.buttonText || 'Mua ngay' }}</span></div>
          </div>
          <div class="row between mt-8">
            <div class="small muted">#{{ b.sortOrder }} · {{ b.linkUrl }}
              @if (b.startAt || b.endAt) { · {{ b.startAt | date: 'dd/MM' }} → {{ b.endAt | date: 'dd/MM' }} }
              @if (!b.active) { · <b>Đang tắt</b> }</div>
            <div><button mat-icon-button (click)="edit(b)" aria-label="Sửa"><mat-icon>edit</mat-icon></button><button mat-icon-button class="danger-icon" (click)="remove(b)" aria-label="Xoá"><mat-icon>delete</mat-icon></button></div>
          </div>
        </div>
      }
    </div>
  `,
  styles: `
    .list { display: grid; grid-template-columns: repeat(auto-fill, minmax(420px, 1fr)); gap: 16px; }
    .bn { margin-top: 0 !important; } .bn.off { opacity: .55; }
    .preview { height: 150px; border-radius: 12px; color: #fff; display: flex; align-items: center; padding: 0 24px; }
    .preview h3 { margin: 0 0 4px; } .preview p { margin: 0 0 8px; font-size: 13px; }
    .btn { background: #fff; color: var(--tl-red); font-weight: 800; padding: 4px 12px; border-radius: 16px; font-size: 12px; }
    @media (max-width: 600px) { .list { grid-template-columns: 1fr; } }
  `,
})
export class BannersComponent implements OnInit {
  private api = inject(AdminApi);
  private dialog = inject(MatDialog);
  private notify = inject(NotifyService);
  list = signal<Banner[]>([]);

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.api.banners().subscribe((l) => this.list.set(l));
  }

  edit(b: Banner | null): void {
    openForm(this.dialog, {
      title: b ? 'Sửa banner' : 'Thêm banner',
      value: (b ? { ...b } : { title: '', subtitle: '', imageUrl: '', linkUrl: '/products', buttonText: 'Mua ngay', bgColor: '#E53935', sortOrder: this.list().length + 1, active: true, startAt: null, endAt: null }) as Record<string, unknown>,
      fields: [
        { key: 'title', label: 'Tiêu đề', required: true, wide: true },
        { key: 'subtitle', label: 'Mô tả phụ', wide: true },
        { key: 'imageUrl', label: 'Ảnh nền', type: 'image', folder: 'banners' },
        { key: 'linkUrl', label: 'Đường dẫn', hint: 'vd: /products?onSale=true' },
        { key: 'buttonText', label: 'Chữ trên nút' },
        { key: 'bgColor', label: 'Màu nền', type: 'color' },
        { key: 'sortOrder', label: 'Thứ tự', type: 'number' },
        { key: 'active', label: 'Hiển thị', type: 'toggle' },
        { key: 'startAt', label: 'Hiện từ (tuỳ chọn)', type: 'datetime' },
        { key: 'endAt', label: 'Hiện đến (tuỳ chọn)', type: 'datetime' },
      ],
      save: (x) => this.api.saveBanner(b?.id ?? null, x),
    }).subscribe((r) => r && this.load());
  }

  remove(b: Banner): void {
    confirmDialog(this.dialog, { title: `Xoá banner "${b.title}"?`, danger: true, confirmText: 'Xoá' })
      .subscribe((ok) => ok && this.api.deleteBanner(b.id).subscribe({ next: (r) => { this.notify.success(r.message); this.load(); }, error: (e) => this.notify.error(e) }));
  }
}
