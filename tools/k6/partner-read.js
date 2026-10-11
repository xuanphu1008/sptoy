// Do tai doc cho API doi tac: GET /api/partner/v1/products va /stock/{sku}, co ky HMAC.
// Chay:  k6 run -e BASE=https://sptoy-api.onrender.com -e KEY=<apiKey> -e SECRET=<secret> -e SKU=<sku> tools/k6/partner-read.js
// Luu y: han muc mac dinh cua doi tac la 60 yeu cau/phut; muon do tai that thi nang han muc o Admin > Doi tac.
import http from 'k6/http';
import crypto from 'k6/crypto';
import { check, sleep } from 'k6';

const BASE = __ENV.BASE || 'http://localhost:8080';
const KEY = __ENV.KEY;
const SECRET = __ENV.SECRET;
const SKU = __ENV.SKU || '';

export const options = {
  stages: [
    { duration: '30s', target: 5 },
    { duration: '1m', target: 20 },
    { duration: '30s', target: 0 },
  ],
  thresholds: {
    // 429 la ket qua dung khi vuot han muc, khong tinh la loi
    'http_req_failed{expected_response:true}': ['rate<0.01'],
    http_req_duration: ['p(95)<800'],
  },
};

function signed(method, pathWithQuery) {
  const ts = String(Math.floor(Date.now() / 1000));
  const bodyHash = crypto.sha256('', 'hex');
  const canonical = [ts, method, pathWithQuery, bodyHash].join('\n');
  const sig = crypto.hmac('sha256', SECRET, canonical, 'hex');
  return { headers: { 'X-Api-Key': KEY, 'X-Timestamp': ts, 'X-Signature': sig } };
}

export default function () {
  const list = '/api/partner/v1/products?page=0&size=20';
  const r1 = http.get(BASE + list, signed('GET', list));
  check(r1, { 'danh muc 200 hoac 429': (r) => r.status === 200 || r.status === 429 });
  if (SKU) {
    const p = '/api/partner/v1/stock/' + SKU;
    const r2 = http.get(BASE + p, signed('GET', p));
    check(r2, { 'ton kho 200 hoac 429': (r) => r.status === 200 || r.status === 429 });
  }
  sleep(1);
}
