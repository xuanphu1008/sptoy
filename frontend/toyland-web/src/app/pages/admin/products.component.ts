import { DatePipe, DecimalPipe } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatMenuModule } from '@angular/material/menu';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSelectModule } from '@angular/material/select';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { Router, RouterLink } from '@angular/router';
import { AdminApi } from '../../core/admin-api.service';
import { PLACEHOLDER_IMG, PRODUCT_STATUS } from '../../core/labels';
import { AdminProductRow, CategoryNode, Page, ProductStatus } from '../../core/models';
import { NotifyService } from '../../core/notify.service';
import { VndPipe } from '../../core/vnd.pipe';
import { confirmDialog } from '../../shared/confirm-dialog.component';

/** Lam phang cay danh muc de dung trong select (co thut le) */
export function flattenCategories(nodes: CategoryNode[], depth = 0): { id: number; name: string; depth: number }[] {
  return nodes.flatMap((n) => [{ id: n.id, name: n.name, depth }, ...flattenCategories(n.children ?? [], depth + 1)]);
}

@Component({
  selector: 'tl-admin-products',
  imports: [RouterLink, FormsModule, DatePipe, DecimalPipe, MatTableModule, MatPaginatorModule, MatFormFieldModule, MatInputModule, MatSelectModule,
    MatButtonModule, MatIconModule, MatMenuModule, MatTooltipModule, VndPipe],
  template: `
    <div class="page-title">
      <h1>Sản phẩm</h1><span class="spacer"></span>
      <a mat-flat-button routerLink="/admin/products/new"><mat-icon>add</mat-icon>Thêm sản phẩm</a>
    </div>
    <div class="filters">
      <mat-form-field class="search-field" subscriptSizing="dynamic"><mat-icon matPrefix>search</mat-icon><input matInput [(ngModel)]="keyword" (keyup.enter)="load(0)" placeholder="Tìm theo tên sản phẩm hoặc SKU…" aria-label="Tìm sản phẩm" />@if (keyword) { <button mat-icon-button matSuffix type="button" (click)="keyword = ''; load(0)" aria-label="Xoá từ khoá"><mat-icon>close</mat-icon></button> }</mat-form-field>
      <mat-form-field><mat-label>Danh mục</mat-label>
        <mat-select [(ngModel)]="categoryId" (selectionChange)="load(0)"><mat-option [value]="null">Tất cả</mat-option>
          @for (c of cats(); track c.id) { <mat-option [value]="c.id">{{ '— '.repeat(c.depth) }}{{ c.name }}</mat-option> }</mat-select></mat-form-field>
      <mat-form-field><mat-label>Trạng thái</mat-label>
        <mat-select [(ngModel)]="status" (selectionChange)="load(0)"><mat-option [value]="null">Tất cả</mat-option>
          @for (s of statuses; track s[0]) { <mat-option [value]="s[0]">{{ s[1] }}</mat-option> }</mat-select></mat-form-field>
      <mat-form-field><mat-label>Sắp xếp</mat-label>
        <mat-select [(ngModel)]="sort" (selectionChange)="load(0)">
          <mat-option value="updated">Mới cập nhật</mat-option><mat-option value="name">Tên A-Z</mat-option>
          <mat-option value="stock_asc">Tồn kho thấp</mat-option><mat-option value="bestseller">Bán chạy</mat-option>
        </mat-select></mat-form-field>
    </div>
    <div class="table-wrap">
      <table mat-table [dataSource]="page()?.content ?? []">
        <ng-container matColumnDef="product"><th mat-header-cell *matHeaderCellDef>Sản phẩm</th>
          <td mat-cell *matCellDef="let p"><div class="row">
            <img [src]="p.imageUrl || ph" class="thumb" alt="" />
            <div><a [routerLink]="[p.id]" class="bold">{{ p.name }}</a>
              <div class="small muted">{{ p.categoryName }}@if (p.brandName) { · {{ p.brandName }} } · {{ p.variantCount }} biến thể @if (p.featured) { · ⭐ }</div></div>
          </div></td></ng-container>
        <ng-container matColumnDef="price"><th mat-header-cell *matHeaderCellDef>Giá bán</th>
          <td mat-cell *matCellDef="let p" class="nowrap">{{ p.minPrice | vnd }}@if (p.maxPrice > p.minPrice) { <div class="small muted">→ {{ p.maxPrice | vnd }}</div> }</td></ng-container>
        <ng-container matColumnDef="stock"><th mat-header-cell *matHeaderCellDef>Tồn kho</th>
          <td mat-cell *matCellDef="let p"><span class="chip" [class.red]="p.totalStock === 0" [class.orange]="p.totalStock > 0 && p.totalStock <= 5" [class.green]="p.totalStock > 5">{{ p.totalStock }}</span></td></ng-container>
        <ng-container matColumnDef="sold"><th mat-header-cell *matHeaderCellDef>Đã bán</th>
          <td mat-cell *matCellDef="let p">{{ p.soldCount }}<div class="small muted">★ {{ p.avgRating | number: '1.1-1' }}</div></td></ng-container>
        <ng-container matColumnDef="status"><th mat-header-cell *matHeaderCellDef>Trạng thái</th>
          <td mat-cell *matCellDef="let p">
            <button mat-button [matMenuTriggerFor]="sm" class="st"><span class="chip" [class.green]="p.status === 'ACTIVE'" [class.blue]="p.status === 'COMING_SOON'" [class.grey]="p.status !== 'ACTIVE' && p.status !== 'COMING_SOON'">{{ ps[p.status] }}</span><mat-icon iconPositionEnd>arrow_drop_down</mat-icon></button>
            <mat-menu #sm="matMenu">@for (s of statuses; track s[0]) { <button mat-menu-item (click)="setStatus(p, s[0])">{{ s[1] }}</button> }</mat-menu>
          </td></ng-container>
        <ng-container matColumnDef="updated"><th mat-header-cell *matHeaderCellDef>Cập nhật</th><td mat-cell *matCellDef="let p" class="small">{{ p.updatedAt | date: 'dd/MM/yy HH:mm' }}</td></ng-container>
        <ng-container matColumnDef="actions"><th mat-header-cell *matHeaderCellDef></th>
          <td mat-cell *matCellDef="let p" class="nowrap"><div class="cell-actions">
            <a mat-icon-button [routerLink]="['/products', p.slug]" target="_blank" matTooltip="Xem trên cửa hàng" aria-label="Xem trên cửa hàng"><mat-icon>open_in_new</mat-icon></a>
            <a mat-icon-button [routerLink]="[p.id]" matTooltip="Sửa" aria-label="Sửa sản phẩm"><mat-icon>edit</mat-icon></a>
            <button mat-icon-button class="danger-icon" (click)="remove(p)" matTooltip="Xoá" aria-label="Xoá sản phẩm"><mat-icon>delete</mat-icon></button>
          </div></td></ng-container>
        <tr mat-header-row *matHeaderRowDef="cols"></tr>
        <tr mat-row *matRowDef="let row; columns: cols"></tr>
      </table>
      @if (page()?.content?.length === 0) { <div class="empty">Không có sản phẩm</div> }
      <mat-paginator [length]="page()?.totalElements ?? 0" [pageIndex]="page()?.page ?? 0" [pageSize]="20" [hidePageSize]="true" (page)="go($event)" />
    </div>
  `,
  styles: `.st { padding: 0; min-width: 0; }`,
})
export class AdminProductsComponent implements OnInit {
  private api = inject(AdminApi);
  private dialog = inject(MatDialog);
  private notify = inject(NotifyService);
  router = inject(Router);
  ph = PLACEHOLDER_IMG;
  ps: Record<string, string> = PRODUCT_STATUS;
  statuses = Object.entries(PRODUCT_STATUS) as [ProductStatus, string][];
  cols = ['product', 'price', 'stock', 'sold', 'status', 'updated', 'actions'];
  page = signal<Page<AdminProductRow> | null>(null);
  tree = signal<CategoryNode[]>([]);
  cats = computed(() => flattenCategories(this.tree()));
  keyword = '';
  categoryId: number | null = null;
  status: ProductStatus | null = null;
  sort = 'updated';
  current = 0;

  ngOnInit(): void {
    this.api.categories().subscribe((c) => this.tree.set(c));
    this.load(0);
  }

  load(page: number): void {
    this.current = page;
    this.api.products({ keyword: this.keyword.trim(), categoryId: this.categoryId, status: this.status, sort: this.sort, page, size: 20 })
      .subscribe((p) => this.page.set(p));
  }

  go(e: PageEvent): void {
    this.load(e.pageIndex);
  }

  setStatus(p: AdminProductRow, s: ProductStatus): void {
    this.api.productStatus(p.id, s).subscribe({
      next: (r) => {
        this.notify.success(r.message);
        this.load(this.current);
      },
      error: (e) => this.notify.error(e),
    });
  }

  remove(p: AdminProductRow): void {
    confirmDialog(this.dialog, {
      title: `Xoá "${p.name}"?`, danger: true, confirmText: 'Xoá',
      message: 'Sản phẩm đã có đơn hàng sẽ được ẩn (xoá mềm) để giữ lịch sử đơn.',
    }).subscribe((ok) => {
      if (ok) this.api.deleteProduct(p.id).subscribe({ next: (r) => { this.notify.success(r.message); this.load(this.current); }, error: (e) => this.notify.error(e) });
    });
  }
}
