import { Component, inject, input, model, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ShopApi } from '../core/api.service';
import { NotifyService } from '../core/notify.service';

/** Upload nhieu anh -> danh sach URL (dung cho danh gia, tra hang, san pham) */
@Component({
  selector: 'tl-image-upload',
  imports: [MatButtonModule, MatIconModule, MatProgressSpinnerModule],
  template: `
    <div class="imgs">
      @for (u of urls(); track u; let i = $index) {
        <div class="it">
          <img [src]="u" alt="" />
          <button type="button" class="x" (click)="remove(i)" title="Xoá"><mat-icon>close</mat-icon></button>
        </div>
      }
      @if (urls().length < max()) {
        <label class="add">
          @if (uploading()) { <mat-spinner diameter="24" /> } @else { <mat-icon>add_a_photo</mat-icon><span>Thêm ảnh</span> }
          <input type="file" accept="image/*" multiple hidden (change)="pick($event)" [disabled]="uploading()" />
        </label>
      }
    </div>
    <div class="muted small">Tối đa {{ max() }} ảnh, mỗi ảnh ≤ 5MB (jpg, png, webp, gif)</div>
  `,
  styles: `
    .imgs { display: flex; flex-wrap: wrap; gap: 8px; margin-bottom: 4px; }
    .it { position: relative; width: 80px; height: 80px; }
    .it img { width: 100%; height: 100%; object-fit: cover; border-radius: 8px; }
    .x { position: absolute; top: -6px; right: -6px; width: 22px; height: 22px; border-radius: 50%; border: 0; background: var(--ink);
      color: #fff; cursor: pointer; display: grid; place-items: center; padding: 0; }
    .x mat-icon { font-size: 14px; width: 14px; height: 14px; }
    .add { width: 80px; height: 80px; border: 2px dashed var(--tl-border-strong); border-radius: 16px; display: flex; flex-direction: column;
      align-items: center; justify-content: center; cursor: pointer; color: var(--tl-muted); font-size: 11px; }
    .add:hover { border-color: var(--tl-red); color: var(--tl-red); }
  `,
})
export class ImageUploadComponent {
  private api = inject(ShopApi);
  private notify = inject(NotifyService);
  urls = model<string[]>([]);
  folder = input('reviews');
  max = input(5);
  uploading = signal(false);

  pick(ev: Event): void {
    const el = ev.target as HTMLInputElement;
    const files = Array.from(el.files ?? []).slice(0, this.max() - this.urls().length);
    el.value = '';
    if (!files.length) return;
    this.uploading.set(true);
    let pending = files.length;
    for (const f of files) {
      this.api.upload(f, this.folder()).subscribe({
        next: (r) => this.urls.update((u) => [...u, r.url]),
        error: (e) => this.notify.error(e),
        complete: () => undefined,
      }).add(() => {
        pending--;
        if (pending === 0) this.uploading.set(false);
      });
    }
  }

  remove(i: number): void {
    this.urls.update((u) => u.filter((_, idx) => idx !== i));
  }
}
