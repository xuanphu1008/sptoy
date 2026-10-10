// Kich ban demo SOA cho ToyLand: dong vai ben thu ba goi dich vu cua cua hang.
// Chay:  node tools/soa-demo.mjs partner   (API doi tac ky HMAC)
//        node tools/soa-demo.mjs idem      (chong dat hang trung)
// Bien moi truong: BASE (mac dinh http://localhost:8080), API_KEY, SECRET (cho "partner"),
//                  USER, PASS (khach hang, cho "idem"; mac dinh khachhang / 123456)
import crypto from 'node:crypto';

const BASE = (process.env.BASE || 'http://localhost:8080').replace(/\/$/, '');
const C = { g: s => `\x1b[32m${s}\x1b[0m`, r: s => `\x1b[31m${s}\x1b[0m`, y: s => `\x1b[33m${s}\x1b[0m`, b: s => `\x1b[1m${s}\x1b[0m` };

function sign(secret, ts, method, pathQuery, body = '') {
  const canonical = `${ts}\n${method}\n${pathQuery}\n${crypto.createHash('sha256').update(body).digest('hex')}`;
  return crypto.createHmac('sha256', secret).update(canonical).digest('hex');
}

async function partnerCall(label, pathQuery, { secret = process.env.SECRET, ts = Math.floor(Date.now() / 1000), key = process.env.API_KEY } = {}) {
  const res = await fetch(BASE + pathQuery, {
    headers: { 'X-Api-Key': key, 'X-Timestamp': String(ts), 'X-Signature': sign(secret, ts, 'GET', pathQuery) },
  });
  const text = await res.text();
  let j; try { j = JSON.parse(text); } catch { j = text; }
  const ok = res.status < 300;
  console.log(`${ok ? C.g(res.status) : C.r(res.status)}  ${C.b(label)}`);
  console.log('    ' + JSON.stringify(j).slice(0, 220));
  return { status: res.status, body: j };
}

async function partner() {
  if (!process.env.API_KEY || !process.env.SECRET) { console.log(C.r('Thieu API_KEY / SECRET (tao o Admin > Doi tac, hoac POST /api/v1/admin/partners)')); process.exit(1); }
  console.log(C.y('\n[1] Danh sach hang dang ban (2 san pham dau)'));
  const list = await partnerCall('GET /products?page=0&size=2', '/api/partner/v1/products?page=0&size=2');
  const sku = list.body?.items?.[0]?.sku;
  console.log(C.y('\n[2] Ton kho theo SKU'));
  if (sku) await partnerCall(`GET /stock/${sku}`, `/api/partner/v1/stock/${sku}`);
  console.log(C.y('\n[3] Ky sai secret -> phai bi tu choi'));
  await partnerCall('chu ky sai', '/api/partner/v1/products', { secret: 'sai-secret' });
  console.log(C.y('\n[4] Timestamp cu 15 phut (tan cong phat lai) -> phai bi tu choi'));
  await partnerCall('timestamp het han', '/api/partner/v1/products', { ts: Math.floor(Date.now() / 1000) - 900 });
  console.log(C.y('\n[5] Ma hang khong ton tai -> 404'));
  await partnerCall('SKU khong co', '/api/partner/v1/stock/KHONG-CO');
}

async function idem() {
  const post = async (path, body, headers = {}) => {
    const r = await fetch(BASE + path, { method: 'POST', headers: { 'Content-Type': 'application/json', ...headers }, body: JSON.stringify(body) });
    const t = await r.text(); let j; try { j = JSON.parse(t); } catch { j = t; }
    return { status: r.status, body: j };
  };
  const get = async (path, headers) => (await fetch(BASE + path, { headers })).json();
  const lg = await post('/api/v1/auth/login', { username: process.env.USER_NAME || 'khachhang', password: process.env.PASS || '123456' });
  const H = { Authorization: 'Bearer ' + lg.body.accessToken };
  const addrs = await get('/api/v1/me/addresses', H);
  if (!addrs.length) { console.log(C.r('Khach chua co dia chi giao hang, hay them 1 dia chi truoc.')); return; }
  const order = { addressId: addrs[0].id, paymentMethod: 'COD', giftWrap: false, usePoints: false };
  const key = 'demo-' + crypto.randomUUID();

  const fill = () => post('/api/v1/cart', { variantId: Number(process.env.VARIANT_ID || 1), quantity: 1 }, H);
  await fill();
  console.log(C.y(`\n[1] Gui 6 yeu cau dat hang CUNG LUC, cung Idempotency-Key = ${key}`));
  const rs = await Promise.all(Array.from({ length: 6 }, () => post('/api/v1/orders', order, { ...H, 'Idempotency-Key': key })));
  rs.forEach((r, i) => console.log(`    #${i + 1}: ${r.status === 201 ? C.g(r.status) : C.y(r.status)}  ${r.body.orderCode || r.body.code}`));
  console.log(C.b(`    => Chi ${rs.filter(r => r.status === 201).length} don duoc tao.`));

  console.log(C.y('\n[2] Gui LAI sau do, cung khoa + cung noi dung -> nhan lai don cu, khong tao don moi'));
  const again = await post('/api/v1/orders', order, { ...H, 'Idempotency-Key': key });
  console.log(`    ${C.g(again.status)}  ${again.body.orderCode}`);

  console.log(C.y('\n[3] Cung khoa nhung NOI DUNG KHAC -> 409 IDEMPOTENCY_KEY_REUSED'));
  const diff = await post('/api/v1/orders', { ...order, note: 'noi dung khac' }, { ...H, 'Idempotency-Key': key });
  console.log(`    ${C.r(diff.status)}  ${diff.body.code}`);
}

const cmd = process.argv[2];
if (cmd === 'partner') await partner();
else if (cmd === 'idem') await idem();
else console.log('Dung: node tools/soa-demo.mjs partner|idem');
