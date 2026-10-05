import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatChipsModule } from '@angular/material/chips';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth.service';
import { AdminApi } from '../../core/admin-api.service';
import { AdminReview, BannedKeyword, Page } from '../../core/models';
import { NotifyService } from '../../core/notify.service';
import { confirmDialog } from '../../shared/confirm-dialog.component';
import { StarsComponent } from '../../shared/stars.component';

/** Hop thoai quan ly tu khoa cam (chi ADMIN). */
@Component({
  selector: 'tl-keywords-dialog',
  imports: [FormsModule, MatDialogModule, MatChipsModule, MatFormFieldModule, MatInputModule, MatButtonModule, MatIconModule, MatTooltipModule],
  template: `
    <h2 mat-dialog-title>Từ khoá cấm trong đánh giá</h2>
    <mat-dialog-content>
      <p class="muted small">Đánh giá chứa từ khoá (so khớp không dấu, nguyên từ) sẽ tự động bị ẩn và chờ duyệt. Ví dụ "lừa đảo" khớp cả "lua dao".</p>
      <form class="add" (submit)="add(); $event.preventDefault()">
        <mat-form-field class="grow" subscriptSizing="dynamic"><mat-label>Thêm từ khoá</mat-label>
          <input matInput [(ngModel)]="keyword" name="kw" maxlength="100" /></mat-form-field>
        <button mat-flat-button type="submit" [disabled]="keyword.trim().length < 2">Thêm</button>
      </form>
      <mat-chip-set aria-label="Danh sách từ khoá cấm">
        @for (k of list(); track k.id) {
          <mat-chip (removed)="remove(k)">{{ k.keyword }}
            <button matChipRemove [attr.aria-label]="'Xoá ' + k.keyword"><mat-icon>cancel</mat-icon></button></mat-chip>
        } @empty { <span class="muted">Chưa có từ khoá nào</span> }
      </mat-chip-set>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-stroked-button (click)="rescan()" matTooltip="Ẩn các đánh giá đang hiện có chứa từ khoá"><mat-icon>manage_search</mat-icon>Quét lại đánh giá cũ</button>
      <button mat-button mat-dialog-close>Đóng</button>
    </mat-dialog-actions>
  `,
  styles: `.add { display: flex; gap: 8px; align-items: center; margin: 8px 0 14px; } .grow { flex: 1; } mat-dialog-content { min-width: min(520px, 86vw); }`,
})
export class KeywordsDialogComponent implements OnInit {
  private api = inject(AdminApi);
  private notify = inject(NotifyService);
  list = signal<BannedKeyword[]>([]);
  keyword = '';
  rescanned = false;

  ngOnInit(): void {
    this.api.keywords().subscribe((l) => this.list.set(l));
  }

  add(): void {
    this.api.addKeyword(this.keyword).subscribe({
      next: (k) => { this.list.update((l) => [...l, k].sort((a, b) => a.keyword.localeCompare(b.keyword))); this.keyword = ''; },
      error: (e) => this.notify.error(e),
    });
  }

  remove(k: BannedKeyword): void {
    this.api.deleteKeyword(k.id).subscribe({ next: () => this.list.update((l) => l.filter((x) => x.id !== k.id)), error: (e) => this.notify.error(e) });
  }

  rescan(): void {
    this.api.rescanKeywords().subscribe({ next: (r) => { this.rescanned = true; this.notify.success(r.message); }, error: (e) => this.notify.error(e) });
  }
}

@Component({
  selector: 'tl-admin-reviews',
  imports: [RouterLink, FormsModule, DatePipe, MatFormFieldModule, MatInputModule, MatSelectModule, MatButtonModule, MatIconModule,
    MatPaginatorModule, MatSlideToggleModule, MatButtonToggleModule, StarsComponent],
  template: `
    <div class="page-title"><h1>Đánh giá sản phẩm</h1><span class="spacer"></span>
      @if (auth.isAdmin()) { <button mat-stroked-button (click)="keywords()"><mat-icon>gpp_maybe</mat-icon>Từ khoá cấm</button> }
    </div>
    <div class="filters">
      <mat-form-field class="search-field" subscriptSizing="dynamic"><mat-icon matPrefix>search</mat-icon><input matInput [(ngModel)]="keyword" (keyup.enter)="load(0)" placeholder="Tìm sản phẩm, khách hàng, nội dung…" aria-label="Tìm đánh giá" />@if (keyword) { <button mat-icon-button matSuffix type="button" (click)="keyword = ''; load(0)" aria-label="Xoá từ khoá"><mat-icon>close</mat-icon></button> }</mat-form-field>
      <mat-form-field><mat-label>Số sao</mat-label>
        <mat-select [(ngModel)]="rating" (selectionChange)="load(0)"><mat-option [value]="null">Tất cả</mat-option>
          @for (n of [5, 4, 3, 2, 1]; track n) { <mat-option [value]="n">{{ n }} sao</mat-option> }</mat-select></mat-form-field>
      <mat-button-toggle-group [(ngModel)]="flagged" (change)="load(0)" hideSingleSelectionIndicator aria-label="Kiểm duyệt">
        <mat-button-toggle [value]="null">Tất cả</mat-button-toggle>
        <mat-button-toggle [value]="true"><mat-icon>gpp_maybe</mat-icon> Chờ duyệt</mat-button-toggle>
      </mat-button-toggle-group>
      <mat-form-field><mat-label>Hiển thị</mat-label>
        <mat-select [(ngModel)]="hidden" (selectionChange)="load(0)"><mat-option [value]="null">Tất cả</mat-option>
          <mat-option [value]="false">Đang hiện</mat-option><mat-option [value]="true">Đã ẩn</mat-option></mat-select></mat-form-field>
    </div>
    @for (r of page()?.content ?? []; track r.id) {
      <article class="card rv" [class.is-hidden]="r.hidden" [class.is-flagged]="r.flagged">
        @if (r.flagged) {
          <div class="flag"><mat-icon>gpp_maybe</mat-icon><span><b>Tự động ẩn:</b> {{ r.flagReason }}</span><span class="spacer"></span>
            <button mat-flat-button (click)="approve(r)"><mat-icon>check</mat-icon>Duyệt & hiển thị</button></div>
        }
        <header class="rv-head">
          <div class="grow">
            <a [routerLink]="['/products', r.productSlug]" target="_blank" class="rv-product">{{ r.productName }}</a>
            <div class="rv-meta">
              <tl-stars [value]="r.rating" [size]="15" />
              <span>{{ r.userName }}</span><span class="dot">·</span><span>{{ r.userEmail }}</span>
              <span class="dot">·</span><span>{{ r.createdAt | date: 'dd/MM/yyyy HH:mm' }}</span>
              @if (r.hidden) { <span class="chip grey"><mat-icon>visibility_off</mat-icon>Đang ẩn</span> }
            </div>
          </div>
          <mat-slide-toggle [checked]="!r.hidden" (change)="toggle(r, !$event.checked)" labelPosition="before"
                            [attr.aria-label]="r.hidden ? 'Hiện đánh giá' : 'Ẩn đánh giá'">Hiển thị</mat-slide-toggle>
        </header>
        @if (r.comment) { <p class="rv-comment">{{ r.comment }}</p> } @else { <p class="rv-comment muted">(Không có nội dung)</p> }
        @if (r.imageUrls.length) { <div class="imgs">@for (u of r.imageUrls; track u) { <a [href]="u" target="_blank"><img [src]="u" alt="Ảnh đánh giá" /></a> }</div> }
        @if (r.adminReply) { <div class="reply"><mat-icon>storefront</mat-icon><div><b>Shop phản hồi</b><div>{{ r.adminReply }}</div></div></div> }
        <footer class="rv-actions">
          <button mat-button (click)="reply(r)"><mat-icon>{{ r.adminReply ? 'edit' : 'reply' }}</mat-icon>{{ r.adminReply ? 'Sửa phản hồi' : 'Phản hồi' }}</button>
        </footer>
      </article>
    } @empty { <div class="card empty-state"><mat-icon>rate_review</mat-icon><b>Không có đánh giá phù hợp</b><span>Thử đổi số sao, trạng thái hiển thị hoặc từ khoá.</span></div> }
    @if ((page()?.totalPages ?? 0) > 1) {
      <mat-paginator [length]="page()!.totalElements" [pageIndex]="page()!.page" [pageSize]="20" [hidePageSize]="true" (page)="go($event)" />
    }
  `,
  styles: `
    .rv { padding: 16px 20px 8px; }
    .card.rv + .card.rv { margin-top: 12px; }
    .rv.is-hidden { background: var(--tl-hover); }
    .rv.is-flagged { box-shadow: inset 0 0 0 2px var(--tl-orange-soft), var(--tl-shadow); }
    .flag { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; margin: -4px -8px 10px; padding: 8px 10px; border-radius: 14px;
      background: var(--tl-orange-soft); color: var(--tl-orange); font-size: 14px; }
    .flag b { color: inherit; }
    .rv.is-hidden .rv-comment { color: var(--tl-muted); }
    .rv-head { display: flex; gap: 16px; align-items: flex-start; }
    .rv-product { font-weight: 800; font-size: 15px; }
    .rv-product:hover { color: var(--tl-red-dark); text-decoration: underline; }
    .rv-meta { display: flex; flex-wrap: wrap; align-items: center; gap: 6px; margin-top: 4px; font-size: 13px; color: var(--tl-muted); }
    .rv-meta .dot { color: var(--tl-border-strong); }
    .rv-meta .chip mat-icon { font-size: 14px; width: 14px; height: 14px; }
    .rv-comment { margin: 10px 0 8px; line-height: 1.6; }
    .rv-actions { margin-left: -12px; }
    .imgs { display: flex; gap: 6px; } .imgs img { width: 64px; height: 64px; object-fit: cover; border-radius: 8px; }
    .reply { display: flex; gap: 10px; background: var(--tl-blue-soft); border: 0; border-left: 4px solid var(--tl-blue); border-radius: 8px; padding: 10px 12px; margin: 4px 0 6px; font-size: 14px; }
    .reply mat-icon { color: var(--tl-blue); font-size: 20px; width: 20px; height: 20px; flex: none; }
  `,
})
export class AdminReviewsComponent implements OnInit {
  private api = inject(AdminApi);
  private dialog = inject(MatDialog);
  private notify = inject(NotifyService);
  page = signal<Page<AdminReview> | null>(null);
  keyword = '';
  rating: number | null = null;
  hidden: boolean | null = null;
  flagged: boolean | null = null;
  auth = inject(AuthService);
  private route = inject(ActivatedRoute);

  ngOnInit(): void {
    this.flagged = this.route.snapshot.queryParamMap.get('flagged') === 'true' ? true : null;
    this.load(0);
  }

  load(page: number): void {
    this.api.reviews({ keyword: this.keyword.trim(), rating: this.rating, hidden: this.hidden, flagged: this.flagged, page, size: 20 })
      .subscribe((p) => this.page.set(p));
  }

  approve(r: AdminReview): void {
    this.api.approveReview(r.id).subscribe({
      next: (x) => { this.replace(x); this.notify.success('Đã duyệt, đánh giá đang hiển thị'); },
      error: (e) => this.notify.error(e),
    });
  }

  keywords(): void {
    this.dialog.open(KeywordsDialogComponent, { autoFocus: false }).afterClosed().subscribe(() => this.load(0));
  }

  go(e: PageEvent): void {
    this.load(e.pageIndex);
  }

  private replace(r: AdminReview): void {
    const p = this.page();
    if (p) this.page.set({ ...p, content: p.content.map((x) => (x.id === r.id ? r : x)) });
  }

  toggle(r: AdminReview, hidden: boolean): void {
    this.api.reviewVisibility(r.id, hidden).subscribe({
      next: (x) => {
        this.replace(x);
        this.notify.info(hidden ? 'Đã ẩn đánh giá' : 'Đã hiện đánh giá');
      },
      error: (e) => this.notify.error(e),
    });
  }

  reply(r: AdminReview): void {
    confirmDialog<string>(this.dialog, {
      title: 'Phản hồi đánh giá', confirmText: 'Gửi phản hồi', input: { label: 'Nội dung phản hồi', value: r.adminReply ?? '' },
    }).subscribe((text) => {
      if (text === undefined) return;
      this.api.reviewReply(r.id, text).subscribe({ next: (x) => this.replace(x), error: (e) => this.notify.error(e) });
    });
  }
}
