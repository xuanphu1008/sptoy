import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSelectModule } from '@angular/material/select';
import { MatTableModule } from '@angular/material/table';
import { AdminApi } from '../../core/admin-api.service';
import { AUDIT_ACTION, AUDIT_ENTITY } from '../../core/labels';
import { AuditLog, Page } from '../../core/models';

const ACTION_CLASS: Record<string, string> = {
  CREATE: 'green', UPDATE: 'blue', DELETE: 'red', STATUS: 'yellow', ADJUST: 'orange', LOGIN: 'grey', EXPORT: 'grey',
  HIDE: 'orange', MODERATE: 'orange', APPROVE: 'green', REJECT: 'red', DISABLE: 'grey',
};

/** Nhat ky hoat dong cua nhan vien / quan tri (truy vet ai thay doi gi). */
@Component({
  selector: 'tl-audit-logs',
  imports: [FormsModule, DatePipe, MatTableModule, MatPaginatorModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatButtonModule, MatIconModule],
  template: `
    <div class="page-title"><h1>Nhật ký hoạt động</h1></div>
    <p class="muted">Mọi thao tác thay đổi dữ liệu của nhân viên và quản trị viên đều được ghi lại: người thực hiện, thời điểm, địa chỉ IP.</p>
    <div class="filters">
      <mat-form-field class="search-field" subscriptSizing="dynamic"><mat-icon matPrefix>search</mat-icon>
        <input matInput [(ngModel)]="keyword" (keyup.enter)="load(0)" placeholder="Tìm nội dung, mã đơn, SKU…" aria-label="Tìm trong nhật ký" />
        @if (keyword) { <button mat-icon-button matSuffix type="button" (click)="keyword = ''; load(0)" aria-label="Xoá từ khoá"><mat-icon>close</mat-icon></button> }
      </mat-form-field>
      <mat-form-field><mat-label>Đối tượng</mat-label>
        <mat-select [(ngModel)]="entityType" (selectionChange)="load(0)"><mat-option [value]="null">Tất cả</mat-option>
          @for (e of entities; track e[0]) { <mat-option [value]="e[0]">{{ e[1] }}</mat-option> }</mat-select></mat-form-field>
      <mat-form-field><mat-label>Thao tác</mat-label>
        <mat-select [(ngModel)]="action" (selectionChange)="load(0)"><mat-option [value]="null">Tất cả</mat-option>
          @for (a of actions; track a[0]) { <mat-option [value]="a[0]">{{ a[1] }}</mat-option> }</mat-select></mat-form-field>
      <mat-form-field><mat-label>Người thực hiện</mat-label><input matInput [(ngModel)]="actor" (keyup.enter)="load(0)" placeholder="vd: staff" /></mat-form-field>
      <mat-form-field><mat-label>Từ ngày</mat-label><input matInput type="date" [(ngModel)]="from" (change)="load(0)" /></mat-form-field>
      <mat-form-field><mat-label>Đến ngày</mat-label><input matInput type="date" [(ngModel)]="to" (change)="load(0)" /></mat-form-field>
    </div>
    <div class="table-wrap">
      <table mat-table [dataSource]="page()?.content ?? []">
        <ng-container matColumnDef="time"><th mat-header-cell *matHeaderCellDef>Thời gian</th>
          <td mat-cell *matCellDef="let a" class="nowrap">{{ a.createdAt | date: 'HH:mm:ss' }}<div class="small muted">{{ a.createdAt | date: 'dd/MM/yyyy' }}</div></td></ng-container>
        <ng-container matColumnDef="actor"><th mat-header-cell *matHeaderCellDef>Người thực hiện</th>
          <td mat-cell *matCellDef="let a"><span class="who"><span class="av">{{ a.actor.charAt(0).toUpperCase() }}</span>
            <span><b>{{ a.actor }}</b>@if (a.actorRole) { <div class="small muted">{{ a.actorRole === 'ADMIN' ? 'Quản trị viên' : a.actorRole === 'STAFF' ? 'Nhân viên' : a.actorRole }}</div> }</span></span></td></ng-container>
        <ng-container matColumnDef="action"><th mat-header-cell *matHeaderCellDef>Thao tác</th>
          <td mat-cell *matCellDef="let a"><span class="chip" [class]="cls(a.action)">{{ actionLabel[a.action] || a.action }}</span>
            <div class="small muted">{{ entityLabel[a.entityType] || a.entityType }}@if (a.entityId) { · {{ a.entityId }} }</div></td></ng-container>
        <ng-container matColumnDef="summary"><th mat-header-cell *matHeaderCellDef>Nội dung</th>
          <td mat-cell *matCellDef="let a">{{ a.summary }}</td></ng-container>
        <ng-container matColumnDef="ip"><th mat-header-cell *matHeaderCellDef>IP</th>
          <td mat-cell *matCellDef="let a" class="small muted">{{ a.ip || '—' }}</td></ng-container>
        <tr mat-header-row *matHeaderRowDef="cols"></tr>
        <tr mat-row *matRowDef="let row; columns: cols"></tr>
      </table>
      @if (page()?.content?.length === 0) {
        <div class="empty-state"><mat-icon>history</mat-icon><b>Không có hoạt động phù hợp</b><span>Thử đổi bộ lọc hoặc khoảng thời gian.</span></div>
      }
      <mat-paginator [length]="page()?.totalElements ?? 0" [pageIndex]="page()?.page ?? 0" [pageSize]="30" [hidePageSize]="true" (page)="go($event)" />
    </div>
  `,
  styles: `
    .who { display: inline-flex; align-items: center; gap: 8px; }
    .av { width: 32px; height: 32px; border-radius: 50%; display: grid; place-items: center; background: var(--p-lilac); font-weight: 800; flex: none; }
    html.dark .av { background: var(--tl-violet-soft); }
  `,
})
export class AuditLogsComponent implements OnInit {
  private api = inject(AdminApi);
  cols = ['time', 'actor', 'action', 'summary', 'ip'];
  page = signal<Page<AuditLog> | null>(null);
  actionLabel = AUDIT_ACTION;
  entityLabel = AUDIT_ENTITY;
  actions = Object.entries(AUDIT_ACTION);
  entities = Object.entries(AUDIT_ENTITY);
  keyword = '';
  actor = '';
  entityType: string | null = null;
  action: string | null = null;
  from = '';
  to = '';

  ngOnInit(): void {
    this.load(0);
  }

  load(p: number): void {
    this.api.auditLogs({ keyword: this.keyword.trim(), actor: this.actor.trim(), entityType: this.entityType, action: this.action,
      from: this.from, to: this.to, page: p, size: 30 }).subscribe((r) => this.page.set(r));
  }

  go(e: PageEvent): void {
    this.load(e.pageIndex);
  }

  cls(action: string): string {
    return 'chip ' + (ACTION_CLASS[action] ?? 'grey');
  }
}
