// Do tai luong duyet cua khach: trang chu, danh sach san pham, tim kiem. Khong can dang nhap.
// Chay:  k6 run -e BASE=https://sptoy-api.onrender.com tools/k6/public-browse.js
import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE = __ENV.BASE || 'http://localhost:8080';

export const options = {
  stages: [
    { duration: '30s', target: 10 },
    { duration: '1m', target: 50 },
    { duration: '30s', target: 0 },
  ],
  thresholds: { http_req_failed: ['rate<0.01'], http_req_duration: ['p(95)<1000'] },
};

export default function () {
  const pages = ['/api/v1/products?page=0&size=24', '/api/v1/products?page=0&size=24&sort=bestseller', '/api/v1/categories'];
  for (const p of pages) {
    const r = http.get(BASE + p);
    check(r, { 'status 200': (x) => x.status === 200 });
  }
  sleep(1);
}
