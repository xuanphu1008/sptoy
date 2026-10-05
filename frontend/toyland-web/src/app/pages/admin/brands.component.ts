import { Component, OnInit, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatTabsModule } from '@angular/material/tabs';
import { AdminApi } from '../../core/admin-api.service';
import { ShopApi } from '../../core/api.service';
import { Brand, Franchise } from '../../core/models';
import { NotifyService } from '../../core/notify.service';
import { confirmDialog } from '../../shared/confirm-dialog.component';
import { openForm } from '../../shared/form-dialog.component';

@Component({
  selector: 'tl-brands',
  imports: [MatTabsModule, MatButtonModule, MatIconModule],
  template: `
    <div class="page-title"><h1>Thương hiệu & Nhân vật</h1></div>
    <div class="card">
      <mat-tab-group mat-stretch-tabs="false" animationDuration="0ms">
        <mat-tab label="Thương hiệu ({{ brands().length }})">
          <div class="row end mt-16"><button mat-flat-button (click)="editBrand(null)"><mat-icon>add</mat-icon>Thêm thương hiệu</button></div>
          <div class="list">
            @for (b of brands(); track b.id) {
              <div class="it">
                @if (b.logoUrl) { <img [src]="b.logoUrl" alt="" /> } @else { <div class="ph">{{ b.name.charAt(0) }}</div> }
                <div class="grow"><b>{{ b.name }}</b><div class="small muted">{{ b.country || '—' }} · /{{ b.slug }}</div></div>
                <button mat-icon-button (click)="editBrand(b)" aria-label="Sửa"><mat-icon>edit</mat-icon></button>
                <button mat-icon-button class="danger-icon" (click)="removeBrand(b)" aria-label="Xoá"><mat-icon>delete</mat-icon></button>
              </div>
            }
          </div>
        </mat-tab>
        <mat-tab label="Nhân vật / Chủ đề ({{ franchises().length }})">
          <div class="row end mt-16"><button mat-flat-button (click)="editFranchise(null)"><mat-icon>add</mat-icon>Thêm nhân vật</button></div>
          <div class="list">
            @for (f of franchises(); track f.id) {
              <div class="it">
                @if (f.imageUrl) { <img [src]="f.imageUrl" alt="" /> } @else { <div class="ph">{{ f.name.charAt(0) }}</div> }
                <div class="grow"><b>{{ f.name }}</b><div class="small muted clamp-2">{{ f.description }}</div></div>
                <button mat-icon-button (click)="editFranchise(f)" aria-label="Sửa"><mat-icon>edit</mat-icon></button>
                <button mat-icon-button class="danger-icon" (click)="removeFranchise(f)" aria-label="Xoá"><mat-icon>delete</mat-icon></button>
              </div>
            }
          </div>
        </mat-tab>
      </mat-tab-group>
    </div>
  `,
  styles: `
    .list { display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 10px; margin-top: 12px; }
    .it { display: flex; gap: 10px; align-items: center; border: 0; background: var(--tl-card); box-shadow: var(--clay-sm); border-radius: 16px; padding: 8px 10px; }
    .it img, .ph { width: 44px; height: 44px; border-radius: 10px; object-fit: contain; background: var(--tl-hover); }
    .ph { display: grid; place-items: center; font-weight: 900; color: var(--tl-red); background: var(--tl-red-soft); }
  `,
})
export class BrandsComponent implements OnInit {
  private api = inject(AdminApi);
  private shop = inject(ShopApi);
  private dialog = inject(MatDialog);
  private notify = inject(NotifyService);
  brands = signal<Brand[]>([]);
  franchises = signal<Franchise[]>([]);

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.shop.brands().subscribe((b) => this.brands.set(b));
    this.shop.franchises().subscribe((f) => this.franchises.set(f));
  }

  editBrand(b: Brand | null): void {
    openForm(this.dialog, {
      title: b ? `Sửa thương hiệu: ${b.name}` : 'Thêm thương hiệu',
      value: (b ? { ...b } : { name: '', slug: '', country: '', logoUrl: '', description: '' }) as Record<string, unknown>,
      fields: [
        { key: 'name', label: 'Tên thương hiệu', required: true },
        { key: 'slug', label: 'Slug', hint: 'Để trống để tự tạo' },
        { key: 'country', label: 'Quốc gia' },
        { key: 'logoUrl', label: 'Logo', type: 'image', folder: 'brands' },
        { key: 'description', label: 'Mô tả', type: 'textarea' },
      ],
      save: (v) => this.api.saveBrand(b?.id ?? null, v),
    }).subscribe((r) => r && this.load());
  }

  editFranchise(f: Franchise | null): void {
    openForm(this.dialog, {
      title: f ? `Sửa: ${f.name}` : 'Thêm nhân vật / chủ đề',
      value: (f ? { ...f } : { name: '', slug: '', imageUrl: '', description: '' }) as Record<string, unknown>,
      fields: [
        { key: 'name', label: 'Tên', required: true },
        { key: 'slug', label: 'Slug', hint: 'Để trống để tự tạo' },
        { key: 'imageUrl', label: 'Ảnh', type: 'image', folder: 'franchises' },
        { key: 'description', label: 'Mô tả', type: 'textarea' },
      ],
      save: (v) => this.api.saveFranchise(f?.id ?? null, v),
    }).subscribe((r) => r && this.load());
  }

  removeBrand(b: Brand): void {
    confirmDialog(this.dialog, { title: `Xoá thương hiệu "${b.name}"?`, danger: true, confirmText: 'Xoá' })
      .subscribe((ok) => ok && this.api.deleteBrand(b.id).subscribe({ next: (r) => { this.notify.success(r.message); this.load(); }, error: (e) => this.notify.error(e) }));
  }

  removeFranchise(f: Franchise): void {
    confirmDialog(this.dialog, { title: `Xoá "${f.name}"?`, danger: true, confirmText: 'Xoá' })
      .subscribe((ok) => ok && this.api.deleteFranchise(f.id).subscribe({ next: (r) => { this.notify.success(r.message); this.load(); }, error: (e) => this.notify.error(e) }));
  }
}
