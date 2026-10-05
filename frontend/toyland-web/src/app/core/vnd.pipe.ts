import { Pipe, PipeTransform } from '@angular/core';

const fmt = new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND', maximumFractionDigits: 0 });

/** 1299000 -> "1.299.000 ₫" */
@Pipe({ name: 'vnd' })
export class VndPipe implements PipeTransform {
  transform(value: number | null | undefined): string {
    return value === null || value === undefined ? '' : fmt.format(value);
  }
}
