import { HttpResponse } from '@angular/common/http';

/**
 * Lay ten file tu header Content-Disposition.
 * Spring gui ca 2 dang: filename="=?UTF-8?Q?...?=" (RFC 2047) va filename*=UTF-8''... (RFC 5987)
 * -> uu tien filename*, neu chi co filename dang =?UTF-8?Q?...?= thi giai ma; loi thi dung ten mac dinh.
 */
export function fileNameFrom(cd: string | null, fallback: string): string {
  const h = cd ?? '';
  const star = /filename\*\s*=\s*UTF-8''([^;]+)/i.exec(h);
  let name = '';
  if (star) {
    try { name = decodeURIComponent(star[1].trim().replace(/^"|"$/g, '')); } catch { name = ''; }
  }
  if (!name) {
    const plain = /filename\s*=\s*"?([^";]+)"?/i.exec(h);
    name = plain ? plain[1].trim() : '';
    const q = /^=\?UTF-8\?Q\?(.*)\?=$/i.exec(name);
    if (q) {
      try {
        const hex = q[1].replace(/_/g, ' ').replace(/=([0-9A-F]{2})/gi, '%$1');
        name = decodeURIComponent(hex);
      } catch { name = ''; }
    }
  }
  // ten loi / khong co duoi file -> dung ten mac dinh (de Windows mo duoc bang Excel)
  if (!name || /[?=]/.test(name) || !/\.[a-z0-9]{2,5}$/i.test(name)) return fallback;
  return name;
}

/** Luu file tai ve (lay ten file tu header Content-Disposition). Tra ve ten file. */
export function saveBlob(res: HttpResponse<Blob>, fallback: string): string {
  const name = fileNameFrom(res.headers.get('Content-Disposition'), fallback);
  const url = URL.createObjectURL(res.body as Blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = name;
  a.click();
  setTimeout(() => URL.revokeObjectURL(url), 1000);
  return name;
}
