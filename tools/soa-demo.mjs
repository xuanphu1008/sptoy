// Kich ban demo SOA cho ToyLand: dong vai ben thu ba goi dich vu cua cua hang.
// Chay:  node tools/soa-demo.mjs partner   (API doi tac ky HMAC)
//        node tools/soa-demo.mjs soap      (dich vu SOAP/WSDL cho doi tac)
//        node tools/soa-demo.mjs idem      (chong dat hang trung)
//        node tools/soa-demo.mjs order     (doi tac dat / xem / huy don qua API, co Idempotency-Key)
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

async function soap() {
  if (!process.env.API_KEY || !process.env.SECRET) { console.log(C.r('Thieu API_KEY / SECRET')); process.exit(1); }
  const call = async (label, op, sku, { secret = process.env.SECRET, ts = Math.floor(Date.now() / 1000), header = true } = {}) => {
    const sig = crypto.createHmac('sha256', secret).update(`${ts}\n${op}\n${sku}`).digest('hex');
    const hdr = header ? `<soapenv:Header><t:Auth><t:apiKey>${process.env.API_KEY}</t:apiKey><t:timestamp>${ts}</t:timestamp><t:signature>${sig}</t:signature></t:Auth></soapenv:Header>` : '<soapenv:Header/>';
    const env = `<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:t="http://toyland.vn/soap/partner">${hdr}<soapenv:Body><t:${op}><t:sku>${sku}</t:sku></t:${op}></soapenv:Body></soapenv:Envelope>`;
    const r = await fetch(BASE + '/soap/partner', { method: 'POST', headers: { 'Content-Type': 'text/xml; charset=utf-8' }, body: env });
    const x = await r.text();
    const fault = x.match(/<faultstring[^>]*>([^<]*)</);
    console.log(`${fault ? C.r('FAULT') : C.g(r.status)}  ${C.b(label)}`);
    console.log('    ' + (fault ? fault[1] : x.slice(x.indexOf('<ns2:Get'), x.indexOf('</SOAP-ENV:Body>')).slice(0, 260)));
  };
  console.log(C.y('\n[0] WSDL (hop dong dich vu): ' + BASE + '/soap/partner/partner.wsdl'));
  const w = await (await fetch(BASE + '/soap/partner/partner.wsdl')).text();
  console.log('    cac thao tac: ' + [...w.matchAll(/<wsdl:operation name="(\w+)"/g)].map(m => m[1]).filter((v, i, a) => a.indexOf(v) === i).join(', '));
  console.log(C.y('\n[1] GetStock / GetProduct hop le'));
  await call('GetStock LEG-0001', 'GetStockRequest', 'LEG-0001');
  await call('GetProduct LEG-0001', 'GetProductRequest', 'LEG-0001');
  console.log(C.y('\n[2] Cac truong hop loi tra ve SOAP Fault'));
  await call('chu ky sai', 'GetStockRequest', 'LEG-0001', { secret: 'sai' });
  await call('timestamp het han', 'GetStockRequest', 'LEG-0001', { ts: Math.floor(Date.now() / 1000) - 900 });
  await call('thieu header Auth', 'GetStockRequest', 'LEG-0001', { header: false });
  await call('SKU khong ton tai', 'GetStockRequest', 'KHONG-CO');
}

async function partnerPost(label, path, body, { idemKey, secret = process.env.SECRET, expect } = {}) {
  const raw = body === undefined ? '' : JSON.stringify(body);
  const ts = Math.floor(Date.now() / 1000);
  const headers = { 'X-Api-Key': process.env.API_KEY, 'X-Timestamp': String(ts), 'X-Signature': sign(secret, ts, 'POST', path, raw), 'Content-Type': 'application/json' };
  if (idemKey) headers['Idempotency-Key'] = idemKey;
  const res = await fetch(BASE + path, { method: 'POST', headers, body: raw || undefined });
  const t = await res.text(); let j; try { j = JSON.parse(t); } catch { j = t; }
  const good = expect ? res.status === expect : res.status < 300;
  console.log(`${good ? C.g(res.status) : C.r(res.status)}  ${C.b(label)}`);
  console.log('    ' + JSON.stringify(j).slice(0, 240));
  return { status: res.status, body: j };
}

async function order() {
  if (!process.env.API_KEY || !process.env.SECRET) { console.log(C.r('Thieu API_KEY / SECRET')); process.exit(1); }
  const list = await partnerCall('GET /products?size=5', '/api/partner/v1/products?page=0&size=5');
  const item = (list.body?.items || []).find(i => i.stock >= 3);
  if (!item) { console.log(C.r('Khong co SKU nao con >= 3 ton de demo')); return; }
  const stockOf = async () => (await partnerCall('ton kho hien tai', `/api/partner/v1/stock/${item.sku}`)).body.available;
  const ref = 'PO-' + Date.now();
  const key = 'po-' + crypto.randomUUID();
  const body = { externalRef: ref, receiverName: 'Nguyen Van Doi Tac', phone: '0900000001', province: 'Ha Noi', district: 'Cau Giay', ward: 'Dich Vong', addressDetail: '1 Pho Demo', note: 'Don thu nghiem tu doi tac', items: [{ sku: item.sku, quantity: 2 }] };

  console.log(C.y(`\n[1] Ton kho truoc khi dat (${item.sku})`));
  const before = await stockOf();
  console.log(C.y('\n[2] Dat don (2 cai) kem Idempotency-Key -> 201'));
  const created = await partnerPost('POST /orders', '/api/partner/v1/orders', body, { idemKey: key, expect: 201 });
  const code = created.body?.orderCode;
  console.log(C.y('\n[3] Gui lai DUNG yeu cau do (mat mang, gui lai) -> nhan lai cung don, khong tao don thu hai'));
  const again = await partnerPost('POST /orders (gui lai)', '/api/partner/v1/orders', body, { idemKey: key, expect: 201 });
  console.log('    ' + (again.body?.orderCode === code ? C.g('Cung ma don ' + code) : C.r('KHAC ma don')));
  console.log(C.y('\n[4] Cung externalRef nhung khoa moi -> 409 PARTNER_REF_DUPLICATE'));
  await partnerPost('trung externalRef', '/api/partner/v1/orders', body, { idemKey: 'po-' + crypto.randomUUID(), expect: 409 });
  console.log(C.y('\n[5] Cung khoa, noi dung khac -> 409 IDEMPOTENCY_KEY_REUSED'));
  await partnerPost('khoa dung lai', '/api/partner/v1/orders', { ...body, note: 'doi noi dung' }, { idemKey: key, expect: 409 });
  console.log(C.y('\n[6] Thieu Idempotency-Key -> 400'));
  await partnerPost('khong co khoa', '/api/partner/v1/orders', { ...body, externalRef: ref + '-b' }, { expect: 400 });
  console.log(C.y('\n[7] Dat vuot ton kho -> 400 OUT_OF_STOCK, khong tru kho'));
  await partnerPost('het hang', '/api/partner/v1/orders', { ...body, externalRef: ref + '-c', items: [{ sku: item.sku, quantity: 99 }] }, { idemKey: 'po-' + crypto.randomUUID(), expect: 400 });
  console.log(C.y('\n[8] Ton kho sau khi dat: phai giam dung 2'));
  const mid = await stockOf();
  console.log('    ' + (before - mid === 2 ? C.g(`${before} -> ${mid}`) : C.r(`${before} -> ${mid}`)));
  console.log(C.y('\n[9] Xem don'));
  await partnerCall('GET /orders/' + code, '/api/partner/v1/orders/' + code);
  console.log(C.y('\n[10] Huy don -> hoan kho'));
  await partnerPost('POST /orders/' + code + '/cancel', `/api/partner/v1/orders/${code}/cancel`, { reason: 'Doi tac doi y' }, { expect: 200 });
  const after = await stockOf();
  console.log('    ' + (after === before ? C.g(`Kho tro ve ${after}`) : C.r(`Kho ${after}, ky vong ${before}`)));
}

const cmd = process.argv[2];
if (cmd === 'partner') await partner();
else if (cmd === 'soap') await soap();
else if (cmd === 'idem') await idem();
else if (cmd === 'order') await order();
else console.log('Dung: node tools/soa-demo.mjs partner|soap|idem|order');
