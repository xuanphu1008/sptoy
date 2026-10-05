import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatIconModule } from '@angular/material/icon';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatTableModule } from '@angular/material/table';
import { Router, RouterLink } from '@angular/router';
import { AdminApi } from '../../core/admin-api.service';
import { RETURN_STATUS } from '../../core/labels';
import { AdminReturnRow, Page, ReturnStatus } from '../../core/models';
import { VndPipe } from '../../core/vnd.pipe';

@Component({
  selector: 'tl-admin-returns',
  imports: [RouterLink, DatePipe, MatTableModule, MatPaginatorModule, MatButtonToggleModule, MatButtonModule, MatIconModule, VndPipe],
  template: `
    <div class="page-title"><h1>Yêu cầu trả hàng / hoàn tiền</h1></div>
    <mat-button-toggle-group [value]="status()" (change)="setStatus($event.value)" class="mb-16" hideSingleSelectionIndicator aria-label="Lọc theo trạng thái">
      <mat-button-toggle value="REQUESTED">Chờ duyệt</mat-button-toggle>
      <mat-button-toggle value="APPROVED">Đã chấp nhận</mat-button-toggle>
      <mat-button-toggle value="REJECTED">Từ chối</mat-button-toggle>
      <mat-button-toggle [value]="null">Tất cả</mat-button-toggle>
    </mat-button-toggle-group>
    <div class="table-wrap mt-16">
      <table mat-table [dataSource]="page()?.content ?? []">
        <ng-container matColumnDef="order"><th mat-header-cell *matHeaderCellDef>Đơn hàng</th>
          <td mat-cell *matCellDef="let r"><a [routerLink]="['/admin/orders', r.orderId]" class="bold">{{ r.orderCode }}</a></td></ng-container>
        <ng-container matColumnDef="customer"><th mat-header-cell *matHeaderCellDef>Khách hàng</th><td mat-cell *matCellDef="let r">{{ r.customerName }}</td></ng-container>
        <ng-container matColumnDef="reason"><th mat-header-cell *matHeaderCellDef>Lý do</th><td mat-cell *matCellDef="let r">{{ r.reason }}</td></ng-container>
        <ng-container matColumnDef="total"><th mat-header-cell *matHeaderCellDef>Giá trị đơn</th>
          <td mat-cell *matCellDef="let r">{{ r.orderTotal | vnd }}@if (r.refundAmount) { <div class="small muted">Hoàn: {{ r.refundAmount | vnd }}</div> }</td></ng-container>
        <ng-container matColumnDef="date"><th mat-header-cell *matHeaderCellDef>Ngày yêu cầu</th><td mat-cell *matCellDef="let r">{{ r.createdAt | date: 'dd/MM/yyyy HH:mm' }}</td></ng-container>
        <ng-container matColumnDef="status"><th mat-header-cell *matHeaderCellDef>Trạng thái</th>
          <td mat-cell *matCellDef="let r"><span class="chip" [class.yellow]="r.status === 'REQUESTED'" [class.green]="r.status === 'APPROVED'" [class.red]="r.status === 'REJECTED'">{{ rs[r.status] }}</span></td></ng-container>
        <tr mat-header-row *matHeaderRowDef="cols"></tr>
        <tr mat-row *matRowDef="let row; columns: cols" (click)="router.navigate(['/admin/orders', row.orderId])" style="cursor: pointer"></tr>
      </table>
      @if (page()?.content?.length === 0) {
        <div class="empty-state">
          <mat-icon>assignment_turned_in</mat-icon>
          <b>{{ status() === 'REQUESTED' ? 'Không có yêu cầu nào đang chờ duyệt' : 'Chưa có yêu cầu nào' }}</b>
          <span>{{ status() === 'REQUESTED' ? 'Mọi yêu cầu trả hàng đã được xử lý.' : 'Yêu cầu trả hàng của khách sẽ hiển thị tại đây.' }}</span>
          @if (status() !== null) { <button mat-stroked-button (click)="setStatus(null)">Xem tất cả yêu cầu</button> }
        </div>
      } @else {
        <mat-paginator [length]="page()?.totalElements ?? 0" [pageIndex]="page()?.page ?? 0" [pageSize]="20" [hidePageSize]="true" (page)="go($event)" />
      }
    </div>
  `,
})
export class AdminReturnsComponent implements OnInit {
  private api = inject(AdminApi);
  router = inject(Router);
  rs: Record<string, string> = RETURN_STATUS;
  cols = ['order', 'customer', 'reason', 'total', 'date', 'status'];
  status = signal<ReturnStatus | null>('REQUESTED');
  page = signal<Page<AdminReturnRow> | null>(null);

  ngOnInit(): void {
    this.load(0);
  }

  setStatus(s: ReturnStatus | null): void {
    this.status.set(s);
    this.load(0);
  }

  go(e: PageEvent): void {
    this.load(e.pageIndex);
  }

  load(page: number): void {
    this.api.returns({ status: this.status(), page, size: 20 }).subscribe((p) => this.page.set(p));
  }
}
