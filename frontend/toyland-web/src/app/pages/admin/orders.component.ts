import { DatePipe } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatChipsModule } from '@angular/material/chips';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatMenuModule } from '@angular/material/menu';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSelectModule } from '@angular/material/select';
import { MatTableModule } from '@angular/material/table';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AdminApi } from '../../core/admin-api.service';
import { ORDER_STATUS, ORDER_STATUSES, PAYMENT_STATUS } from '../../core/labels';
import { OrderStatus, OrderSummary, Page } from '../../core/models';
import { NotifyService } from '../../core/notify.service';
import { VndPipe } from '../../core/vnd.pipe';
import { confirmDialog } from '../../shared/confirm-dialog.component';

function iso(d: Date | null): string | null {
  return d ? `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}` : null;
}

@Component({
  selector: 'tl-admin-orders',
  imports: [RouterLink, FormsModule, DatePipe, MatTableModule, MatPaginatorModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatButtonModule,
    MatIconModule, MatCheckboxModule, MatChipsModule, MatMenuModule, MatDatepickerModule, VndPipe],
  template: `
    <div class="page-title"><h1>Quản lý đơn hàng</h1></div>

    <div class="filters">
      <!-- Trang thai: 1 hang cuon ngang (khong xuong 2 dong), nam chung khung voi bo loc -->
      <mat-chip-listbox class="status-chips scroll" [value]="status ?? 'ALL'" (change)="setStatus($event.value)" aria-label="Lọc theo trạng thái">
        <mat-chip-option value="ALL">Tất cả</mat-chip-option>
        @for (s of statuses; track s) { <mat-chip-option [value]="s">{{ label[s].label }}</mat-chip-option> }
      </mat-chip-listbox>
      <mat-form-field class="search-field" subscriptSizing="dynamic"><mat-icon matPrefix>search</mat-icon><input matInput [(ngModel)]="keyword" (keyup.enter)="search()" placeholder="Tìm mã đơn, tên, SĐT, email…" aria-label="Tìm đơn hàng" />@if (keyword) { <button mat-icon-button matSuffix type="button" (click)="keyword = ''; search()" aria-label="Xoá từ khoá"><mat-icon>close</mat-icon></button> }</mat-form-field>
      <mat-form-field><mat-label>Thanh toán</mat-label>
        <mat-select [(ngModel)]="paymentMethod" (selectionChange)="search()">
          <mat-option [value]="null">Tất cả</mat-option><mat-option value="COD">COD</mat-option><mat-option value="MOMO">MoMo</mat-option>
        </mat-select></mat-form-field>
      <mat-form-field><mat-label>TT thanh toán</mat-label>
        <mat-select [(ngModel)]="paymentStatus" (selectionChange)="search()">
          <mat-option [value]="null">Tất cả</mat-option>
          @for (p of paymentStatuses; track p[0]) { <mat-option [value]="p[0]">{{ p[1] }}</mat-option> }
        </mat-select></mat-form-field>
      <mat-form-field class="date-range"><mat-label>Ngày đặt</mat-label>
        <mat-date-range-input [rangePicker]="rp"><input matStartDate [(ngModel)]="from" /><input matEndDate [(ngModel)]="to" (dateChange)="search()" /></mat-date-range-input>
        <mat-datepicker-toggle matIconSuffix [for]="rp" /><mat-date-range-picker #rp /></mat-form-field>
      <span class="spacer"></span>
      <button mat-button (click)="reset()"><mat-icon>filter_alt_off</mat-icon>Xoá lọc</button>
    </div>

    @if (selected().size) {
      <div class="bulk">
        <b>Đã chọn {{ selected().size }} đơn</b>
        <button mat-flat-button [matMenuTriggerFor]="bulkMenu"><mat-icon>published_with_changes</mat-icon>Chuyển trạng thái</button>
        <mat-menu #bulkMenu="matMenu">
          @for (s of bulkTargets; track s) { <button mat-menu-item (click)="bulk(s)"><mat-icon>{{ label[s].icon }}</mat-icon>{{ label[s].label }}</button> }
        </mat-menu>
        <button mat-button (click)="toggleAll(false)">Bỏ chọn</button>
      </div>
    }

    <div class="table-wrap">
      <table mat-table [dataSource]="page()?.content ?? []">
        <ng-container matColumnDef="sel">
          <th mat-header-cell *matHeaderCellDef><mat-checkbox [checked]="allSelected()" [indeterminate]="selected().size > 0 && !allSelected()" (change)="toggleAll($event.checked)" /></th>
          <td mat-cell *matCellDef="let o"><mat-checkbox [checked]="selected().has(o.id)" (change)="toggle(o.id)" (click)="$event.stopPropagation()" /></td>
        </ng-container>
        <ng-container matColumnDef="code">
          <th mat-header-cell *matHeaderCellDef>Mã đơn</th>
          <td mat-cell *matCellDef="let o"><a [routerLink]="[o.id]" class="bold">{{ o.orderCode }}</a><div class="small muted">{{ o.createdAt | date: 'dd/MM/yyyy HH:mm' }}</div></td>
        </ng-container>
        <ng-container matColumnDef="customer">
          <th mat-header-cell *matHeaderCellDef>Khách hàng</th>
          <td mat-cell *matCellDef="let o">{{ o.receiverName }}<div class="small muted">{{ o.phone }}</div></td>
        </ng-container>
        <ng-container matColumnDef="items">
          <th mat-header-cell *matHeaderCellDef>Sản phẩm</th>
          <td mat-cell *matCellDef="let o"><div class="ellipsis prod">{{ o.firstItemName }}</div><div class="small muted">Tổng SL: {{ o.itemCount }}</div></td>
        </ng-container>
        <ng-container matColumnDef="payment">
          <th mat-header-cell *matHeaderCellDef>Thanh toán</th>
          <td mat-cell *matCellDef="let o">{{ o.paymentMethod }}<div class="small" [class.ok]="o.paymentStatus === 'PAID'">{{ ps[o.paymentStatus] }}</div></td>
        </ng-container>
        <ng-container matColumnDef="total">
          <th mat-header-cell *matHeaderCellDef class="right">Tổng tiền</th>
          <td mat-cell *matCellDef="let o" class="right bold">{{ o.totalAmount | vnd }}</td>
        </ng-container>
        <ng-container matColumnDef="status">
          <th mat-header-cell *matHeaderCellDef>Trạng thái</th>
          <td mat-cell *matCellDef="let o"><span class="chip st-{{ o.status }}">{{ o.statusLabel }}</span></td>
        </ng-container>
        <tr mat-header-row *matHeaderRowDef="cols"></tr>
        <tr mat-row *matRowDef="let row; columns: cols" (click)="open(row)" class="clickable"></tr>
      </table>
      @if (page()?.content?.length === 0) {
        <div class="empty-state"><mat-icon>receipt_long</mat-icon><b>Không có đơn hàng phù hợp</b><span>Thử bỏ bớt bộ lọc hoặc đổi từ khoá tìm kiếm.</span>
          <button mat-stroked-button (click)="reset()">Xoá bộ lọc</button></div>
      }
      <mat-paginator [length]="page()?.totalElements ?? 0" [pageIndex]="page()?.page ?? 0" [pageSize]="page()?.size ?? 20" [pageSizeOptions]="[10, 20, 50]" (page)="go($event)" />
    </div>
  `,
  styles: `
    .date-range { width: 250px !important; }
    .bulk { display: flex; gap: 12px; align-items: center; background: var(--tl-blue-soft); padding: 8px 12px; border-radius: 12px; margin-bottom: 12px; }
    .prod { max-width: 260px; }
    .ok { color: var(--tl-green); font-weight: 700; }
    .clickable { cursor: pointer; }
  `,
})
export class AdminOrdersComponent implements OnInit {
  private api = inject(AdminApi);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private dialog = inject(MatDialog);
  private notify = inject(NotifyService);

  label = ORDER_STATUS;
  statuses = ORDER_STATUSES;
  ps: Record<string, string> = PAYMENT_STATUS;
  paymentStatuses = Object.entries(PAYMENT_STATUS);
  bulkTargets: OrderStatus[] = ['CONFIRMED', 'PACKING', 'SHIPPING', 'DELIVERED', 'COMPLETED'];
  cols = ['sel', 'code', 'customer', 'items', 'payment', 'total', 'status'];

  page = signal<Page<OrderSummary> | null>(null);
  selected = signal(new Set<number>());
  allSelected = computed(() => {
    const rows = this.page()?.content ?? [];
    return rows.length > 0 && rows.every((r) => this.selected().has(r.id));
  });

  status: OrderStatus | null = null;
  keyword = '';
  paymentMethod: string | null = null;
  paymentStatus: string | null = null;
  from: Date | null = null;
  to: Date | null = null;
  pageIndex = 0;
  size = 20;

  ngOnInit(): void {
    this.status = (this.route.snapshot.queryParams['status'] as OrderStatus) ?? null;
    this.load();
  }

  load(): void {
    this.api.orders({
      status: this.status, keyword: this.keyword.trim(), paymentMethod: this.paymentMethod, paymentStatus: this.paymentStatus,
      from: iso(this.from), to: iso(this.to), page: this.pageIndex, size: this.size,
    }).subscribe((p) => {
      this.page.set(p);
      this.selected.set(new Set());
    });
  }

  search(): void {
    this.pageIndex = 0;
    this.load();
  }

  setStatus(s: OrderStatus | 'ALL' | null | undefined): void {
    this.status = !s || s === 'ALL' ? null : s;
    this.router.navigate([], { queryParams: { status: this.status }, replaceUrl: true });
    this.search();
  }

  reset(): void {
    this.keyword = '';
    this.paymentMethod = this.paymentStatus = null;
    this.from = this.to = null;
    this.setStatus(null);
  }

  go(e: PageEvent): void {
    this.pageIndex = e.pageIndex;
    this.size = e.pageSize;
    this.load();
  }

  toggle(id: number): void {
    const s = new Set(this.selected());
    if (s.has(id)) s.delete(id);
    else s.add(id);
    this.selected.set(s);
  }

  toggleAll(on: boolean): void {
    this.selected.set(on ? new Set((this.page()?.content ?? []).map((o) => o.id)) : new Set());
  }

  open(o: OrderSummary): void {
    this.router.navigate(['/admin/orders', o.id]);
  }

  bulk(s: OrderStatus): void {
    const ids = [...this.selected()];
    confirmDialog<string>(this.dialog, {
      title: `Chuyển ${ids.length} đơn sang "${ORDER_STATUS[s].label}"?`,
      message: 'Đơn không hợp lệ với trạng thái mới sẽ được bỏ qua.', input: { label: 'Ghi chú (không bắt buộc)' },
    }).subscribe((note) => {
      if (note === undefined) return;
      this.api.bulkStatus(ids, s, note || null).subscribe({
        next: (r) => {
          this.notify.success(r.message);
          this.load();
        },
        error: (e) => this.notify.error(e),
      });
    });
  }
}
