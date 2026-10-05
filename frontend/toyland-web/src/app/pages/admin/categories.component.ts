import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { AdminApi } from '../../core/admin-api.service';
import { CategoryNode } from '../../core/models';
import { NotifyService } from '../../core/notify.service';
import { confirmDialog } from '../../shared/confirm-dialog.component';
import { openForm } from '../../shared/form-dialog.component';
import { flattenCategories } from './products.component';

@Component({
  selector: 'tl-categories',
  imports: [MatButtonModule, MatIconModule, MatTooltipModule],
  template: `
    <div class="page-title"><h1>Danh mục sản phẩm</h1><span class="spacer"></span>
      <button mat-flat-button (click)="edit(null, null)"><mat-icon>add</mat-icon>Thêm danh mục gốc</button></div>
    <p class="muted">Cây danh mục 2 cấp (giống MyKingdom / Shopee). Icon dùng tên Material Icons, ví dụ: <code>toys</code>, <code>extension</code>, <code>smart_toy</code>.</p>
    <div class="card">
      @for (c of tree(); track c.id) {
        <div class="node root" [class.off]="!c.active">
          <mat-icon class="ic">{{ c.icon || 'category' }}</mat-icon>
          <div class="grow"><b>{{ c.name }}</b> <span class="muted small">/{{ c.slug }} · thứ tự {{ c.sortOrder }}</span>
            @if (!c.active) { <span class="chip grey">Ẩn</span> }</div>
          <button mat-button (click)="edit(null, c.id)"><mat-icon>add</mat-icon>Danh mục con</button>
          <button mat-icon-button (click)="edit(c, null)" matTooltip="Sửa"><mat-icon>edit</mat-icon></button>
          <button mat-icon-button (click)="remove(c)" matTooltip="Xoá"><mat-icon>delete</mat-icon></button>
        </div>
        @for (s of c.children; track s.id) {
          <div class="node child" [class.off]="!s.active">
            <mat-icon class="ic">subdirectory_arrow_right</mat-icon>
            <div class="grow">{{ s.name }} <span class="muted small">/{{ s.slug }} · thứ tự {{ s.sortOrder }}</span>
              @if (!s.active) { <span class="chip grey">Ẩn</span> }</div>
            <button mat-icon-button (click)="edit(s, null)" matTooltip="Sửa"><mat-icon>edit</mat-icon></button>
            <button mat-icon-button (click)="remove(s)" matTooltip="Xoá"><mat-icon>delete</mat-icon></button>
          </div>
        }
      }
    </div>
  `,
  styles: `
    .node { display: flex; align-items: center; gap: 10px; padding: 8px 4px; border-bottom: 1px solid var(--tl-border); }
    .root { background: var(--tl-hover); border-radius: 14px; margin-top: 8px; }
    .child { padding-left: 32px; }
    .ic { color: var(--tl-red); }
    .off { opacity: .55; }
  `,
})
export class CategoriesComponent implements OnInit {
  private api = inject(AdminApi);
  private dialog = inject(MatDialog);
  private notify = inject(NotifyService);
  tree = signal<CategoryNode[]>([]);
  roots = computed(() => flattenCategories(this.tree()).filter((c) => c.depth === 0));

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.api.categories().subscribe((t) => this.tree.set(t));
  }

  edit(c: CategoryNode | null, parentId: number | null): void {
    const value = c ? { ...c } : { name: '', slug: '', parentId, description: '', imageUrl: '', icon: '', sortOrder: 0, active: true };
    openForm(this.dialog, {
      title: c ? `Sửa danh mục: ${c.name}` : parentId ? 'Thêm danh mục con' : 'Thêm danh mục gốc',
      value: value as Record<string, unknown>,
      fields: [
        { key: 'name', label: 'Tên danh mục', required: true },
        { key: 'slug', label: 'Slug', hint: 'Để trống để tự tạo' },
        { key: 'parentId', label: 'Danh mục cha', type: 'select', options: [{ value: null, label: '(Danh mục gốc)' }, ...this.roots().filter((r) => r.id !== c?.id).map((r) => ({ value: r.id, label: r.name }))] },
        { key: 'icon', label: 'Icon (Material)', hint: 'vd: toys, extension' },
        { key: 'sortOrder', label: 'Thứ tự hiển thị', type: 'number' },
        { key: 'active', label: 'Hiển thị trên cửa hàng', type: 'toggle' },
        { key: 'imageUrl', label: 'Ảnh danh mục', type: 'image', folder: 'categories' },
        { key: 'description', label: 'Mô tả', type: 'textarea' },
      ],
      save: (v) => this.api.saveCategory(c?.id ?? null, v),
    }).subscribe((r) => r && this.load());
  }

  remove(c: CategoryNode): void {
    confirmDialog(this.dialog, { title: `Xoá danh mục "${c.name}"?`, message: 'Chỉ xoá được khi danh mục không còn danh mục con và sản phẩm.', danger: true, confirmText: 'Xoá' })
      .subscribe((ok) => ok && this.api.deleteCategory(c.id).subscribe({ next: (r) => { this.notify.success(r.message); this.load(); }, error: (e) => this.notify.error(e) }));
  }
}
