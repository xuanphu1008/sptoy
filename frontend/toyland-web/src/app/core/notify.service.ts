import { HttpErrorResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';

/** Lay thong bao loi than thien tu ErrorResponse cua Spring Boot */
export function errorMessage(err: unknown): string {
  if (err instanceof HttpErrorResponse) {
    if (err.status === 0) return 'Không kết nối được máy chủ. Kiểm tra backend Spring Boot đang chạy ở cổng 8080.';
    const body = err.error;
    if (body?.details && Object.keys(body.details).length) return Object.values(body.details).join(' • ');
    return body?.message || err.message;
  }
  return 'Có lỗi xảy ra, vui lòng thử lại';
}

@Injectable({ providedIn: 'root' })
export class NotifyService {
  private snack = inject(MatSnackBar);

  success(message: string): void {
    this.snack.open(message, 'Đóng', { duration: 3000, panelClass: 'snack-success' });
  }

  error(err: unknown): void {
    this.snack.open(typeof err === 'string' ? err : errorMessage(err), 'Đóng', { duration: 5000, panelClass: 'snack-error' });
  }

  info(message: string): void {
    this.snack.open(message, 'Đóng', { duration: 3000 });
  }
}
