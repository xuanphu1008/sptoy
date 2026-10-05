import { Injectable } from '@angular/core';
import { MatPaginatorIntl } from '@angular/material/paginator';

@Injectable()
export class VietnamesePaginatorIntl extends MatPaginatorIntl {
  override itemsPerPageLabel = 'Số dòng:';
  override nextPageLabel = 'Trang sau';
  override previousPageLabel = 'Trang trước';
  override firstPageLabel = 'Trang đầu';
  override lastPageLabel = 'Trang cuối';
  override getRangeLabel = (page: number, size: number, length: number): string => {
    if (length === 0) return '0 / 0';
    const start = page * size;
    return `${start + 1} – ${Math.min(start + size, length)} / ${length}`;
  };
}
