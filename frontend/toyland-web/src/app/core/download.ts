import { HttpResponse } from '@angular/common/http';

/** Luu file tai ve (lay ten file tu header Content-Disposition). Tra ve ten file. */
export function saveBlob(res: HttpResponse<Blob>, fallback: string): string {
  const cd = res.headers.get('Content-Disposition') ?? '';
  const m = /filename\*=UTF-8''([^;]+)|filename="?([^";]+)"?/i.exec(cd);
  const name = m ? decodeURIComponent(m[1] ?? m[2]) : fallback;
  const url = URL.createObjectURL(res.body as Blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = name;
  a.click();
  setTimeout(() => URL.revokeObjectURL(url), 1000);
  return name;
}
