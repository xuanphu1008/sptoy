// Dai ly gia dinh "Cong dai ly": ung dung Node.js doc lap dung dich vu doi tac cua ToyLand.
//  - REST  : lay danh muc, dat don co Idempotency-Key, huy don (ky HMAC-SHA256)
//  - SOAP  : tra trang thai don, client sinh tu WSDL bang thu vien "soap"
//  - Webhook: nhan thong bao doi trang thai don, kiem tra chu ky HMAC va chong nhan trung
// Bien moi truong: TOYLAND_BASE_URL, PARTNER_API_KEY, PARTNER_SECRET, PORT
import crypto from 'node:crypto';
import http from 'node:http';
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import * as soap from 'soap';

const BASE = (process.env.TOYLAND_BASE_URL || 'http://localhost:8080').replace(/\/$/, '');
const API_KEY = process.env.PARTNER_API_KEY || '';
const SECRET = process.env.PARTNER_SECRET || '';
const PORT = Number(process.env.PORT || 3000);
const here = path.dirname(fileURLToPath(import.meta.url));

// ---------------------------------------------------------------- trang thai trong bo nho
const orders = [];               // don da dat qua ung dung nay
const webhookLog = [];           // cac webhook da nhan
const seenEvents = new Set();    // eventId da xu ly: nhan trung thi bo qua
const traces = [];               // nhat ky giao tiep (bang chung ky thuat) cho man hinh demo
let traceSeq = 0;
const mask = (v, keep = 8) => (v && v.length > keep ? v.slice(0, keep) + '…' : v || '');
function addTrace(t) {
  traces.unshift({ id: ++traceSeq, at: new Date().toISOString(), ...t });
  traces.length = Math.min(traces.length, 40);
}
const pretty = (raw) => { try { return JSON.stringify(JSON.parse(raw), null, 2); } catch { return raw; } };

// ---------------------------------------------------------------- goi REST co ky
function sha256Hex(s) { return crypto.createHash('sha256').update(s).digest('hex'); }

async function toyland(method, pathQuery, body, idemKey) {
  if (!API_KEY || !SECRET) throw httpError(500, 'Chua cau hinh PARTNER_API_KEY / PARTNER_SECRET');
  const raw = body === undefined ? '' : JSON.stringify(body);
  const ts = Math.floor(Date.now() / 1000);
  const canonical = `${ts}\n${method}\n${pathQuery}\n${sha256Hex(raw)}`;
  const headers = {
    'X-Api-Key': API_KEY, 'X-Timestamp': String(ts),
    'X-Signature': crypto.createHmac('sha256', SECRET).update(canonical).digest('hex'),
  };
  if (raw) headers['Content-Type'] = 'application/json';
  if (idemKey) headers['Idempotency-Key'] = idemKey;
  const t0 = Date.now();
  const shown = { 'X-Api-Key': mask(API_KEY), 'X-Timestamp': String(ts), 'X-Signature': mask(headers['X-Signature'], 16) };
  if (idemKey) shown['Idempotency-Key'] = idemKey;
  let res, text;
  try {
    res = await fetch(BASE + pathQuery, { method, headers, body: raw || undefined });
    text = await res.text();
  } catch (e) {
    addTrace({ kind: 'REST', title: `${method} ${pathQuery.split('?')[0]}`, status: 0, ms: Date.now() - t0, requestHeaders: shown, requestBody: raw ? pretty(raw) : '', responseBody: 'Khong ket noi duoc: ' + e.message });
    throw e;
  }
  addTrace({ kind: 'REST', title: `${method} ${pathQuery.split('?')[0]}`, status: res.status, ms: Date.now() - t0, requestHeaders: shown, requestBody: raw ? pretty(raw) : '', responseBody: pretty(text).slice(0, 4000) });
  let json; try { json = JSON.parse(text); } catch { json = { message: text }; }
  if (!res.ok) throw httpError(res.status, json.message || 'Loi tu ToyLand', json.code);
  return json;
}

// Dung hop dong: 409 IDEMPOTENCY_IN_PROGRESS nghia la yeu cau cung khoa dang chay, cho giay lat roi goi lai CUNG khoa.
async function toylandRetrying(method, pathQuery, body, idemKey, tries = 6) {
  for (let i = 1; ; i++) {
    try { return await toyland(method, pathQuery, body, idemKey); }
    catch (e) { if (e.code !== 'IDEMPOTENCY_IN_PROGRESS' || i >= tries) throw e; await new Promise((r) => setTimeout(r, 600 * i)); }
  }
}

function httpError(status, message, code) { const e = new Error(message); e.status = status; e.code = code; return e; }

// ---------------------------------------------------------------- SOAP (client sinh tu WSDL)
let soapClient;
async function soapStatus(orderCode) {
  if (!soapClient) {
    soapClient = await soap.createClientAsync(`${BASE}/soap/partner/partner.wsdl`);
    soapClient.setEndpoint(`${BASE}/soap/partner`); // dung dia chi that, khong tin location trong WSDL
  }
  const ts = Math.floor(Date.now() / 1000);
  // Tieu de Auth: chu ky = HMAC(secret, timestamp \n tenThaoTac \n maDon)
  const sig = crypto.createHmac('sha256', SECRET).update(`${ts}\nGetOrderStatusRequest\n${orderCode}`).digest('hex');
  soapClient.clearSoapHeaders();
  soapClient.addSoapHeader(`<t:Auth xmlns:t="http://toyland.vn/soap/partner"><t:apiKey>${API_KEY}</t:apiKey><t:timestamp>${ts}</t:timestamp><t:signature>${sig}</t:signature></t:Auth>`);
  const t0 = Date.now();
  const hide = (xml) => String(xml || '').split(API_KEY).join(mask(API_KEY)).replace(/(<(?:\w+:)?signature>)([0-9a-f]{16})[0-9a-f]+/gi, '$1$2…');
  const record = (status) => addTrace({ kind: 'SOAP', title: 'SOAP GetOrderStatus', status, ms: Date.now() - t0, requestHeaders: null, requestBody: hide(soapClient.lastRequest), responseBody: String(soapClient.lastResponse || '').slice(0, 4000) });
  try {
    const [result] = await soapClient.GetOrderStatusAsync({ orderCode });
    record(200);
    return result;
  } catch (e) {
    record(e?.response?.status || 500);
    const fault = e?.root?.Envelope?.Body?.Fault?.faultstring || e.message;
    throw httpError(502, 'SOAP Fault: ' + (typeof fault === 'string' ? fault : JSON.stringify(fault)));
  }
}

// ---------------------------------------------------------------- webhook
function verifyWebhook(req, raw) {
  const ts = req.headers['x-toyland-timestamp'];
  const sig = String(req.headers['x-toyland-signature'] || '').replace(/^sha256=/, '');
  if (!ts || !sig) return 'Thieu header chu ky';
  if (Math.abs(Date.now() / 1000 - Number(ts)) > 300) return 'Timestamp lech qua 5 phut';
  const expected = crypto.createHmac('sha256', SECRET).update(`${ts}.${raw}`).digest('hex');
  const a = Buffer.from(expected), b = Buffer.from(sig);
  if (a.length !== b.length || !crypto.timingSafeEqual(a, b)) return 'Chu ky sai';
  return null;
}

function traceWebhook(req, raw, status, outcome) {
  addTrace({ kind: 'WEBHOOK', title: 'Webhook nhận từ ToyLand', status, ms: null,
    requestHeaders: { 'X-Toyland-Event-Id': req.headers['x-toyland-event-id'] || '', 'X-Toyland-Timestamp': req.headers['x-toyland-timestamp'] || '', 'X-Toyland-Signature': mask(String(req.headers['x-toyland-signature'] || ''), 22) },
    requestBody: pretty(raw), responseBody: outcome });
}

function onWebhook(event, outcome) {
  webhookLog.unshift({ at: new Date().toISOString(), eventId: event?.eventId, type: event?.type, orderCode: event?.data?.orderCode, status: event?.data?.status, outcome });
  webhookLog.length = Math.min(webhookLog.length, 50);
}

// ---------------------------------------------------------------- HTTP server
const readBody = (req) => new Promise((resolve, reject) => {
  let b = ''; req.on('data', (c) => { b += c; if (b.length > 1e6) reject(httpError(413, 'Than yeu cau qua lon')); });
  req.on('end', () => resolve(b)); req.on('error', reject);
});
const send = (res, status, obj) => { res.writeHead(status, { 'Content-Type': 'application/json; charset=utf-8' }); res.end(JSON.stringify(obj)); };

const server = http.createServer(async (req, res) => {
  const url = new URL(req.url, 'http://x');
  try {
    if (req.method === 'GET' && (url.pathname === '/' || url.pathname === '/index.html')) {
      res.writeHead(200, { 'Content-Type': 'text/html; charset=utf-8' });
      return res.end(fs.readFileSync(path.join(here, 'public', 'index.html')));
    }
    if (req.method === 'GET' && url.pathname === '/health') return send(res, 200, { status: 'UP' });
    if (req.method === 'GET' && url.pathname === '/api/config') {
      const proto = req.headers['x-forwarded-proto'] || 'http';
      return send(res, 200, { toylandBase: BASE, configured: Boolean(API_KEY && SECRET), webhookUrl: `${proto}://${req.headers.host}/webhooks/toyland` });
    }
    if (req.method === 'GET' && url.pathname === '/api/catalog') {
      return send(res, 200, await toyland('GET', '/api/partner/v1/products?page=0&size=12'));
    }
    if (req.method === 'GET' && url.pathname === '/api/orders') return send(res, 200, orders);
    if (req.method === 'GET' && url.pathname === '/api/webhooks') return send(res, 200, webhookLog);
    if (req.method === 'GET' && url.pathname === '/api/traces') return send(res, 200, traces);

    if (req.method === 'POST' && url.pathname === '/api/orders') {
      const r = JSON.parse((await readBody(req)) || '{}');
      // Idempotency-Key do trinh duyet tao MOT LAN cho moi lan mo bieu mau; bam hai lan van cung khoa -> chi mot don
      const key = String(r.clientToken || '');
      if (!key) throw httpError(400, 'Thieu clientToken');
      const order = await toylandRetrying('POST', '/api/partner/v1/orders', {
        externalRef: 'DL-' + key.slice(0, 20), receiverName: r.receiverName, phone: r.phone, province: r.province,
        district: r.district, ward: r.ward, addressDetail: r.addressDetail, note: r.note || null,
        items: [{ sku: r.sku, quantity: Number(r.quantity) || 1 }],
      }, 'dl-' + key);
      if (!orders.find((o) => o.orderCode === order.orderCode)) orders.unshift(order);
      return send(res, 201, order);
    }
    let m;
    if (req.method === 'POST' && (m = url.pathname.match(/^\/api\/orders\/(TL\d+)\/cancel$/))) {
      const o = await toyland('POST', `/api/partner/v1/orders/${m[1]}/cancel`, { reason: 'Khach dai ly doi y' });
      const i = orders.findIndex((x) => x.orderCode === o.orderCode); if (i >= 0) orders[i] = o;
      return send(res, 200, o);
    }
    if (req.method === 'GET' && (m = url.pathname.match(/^\/api\/orders\/(TL\d+)\/soap-status$/))) {
      return send(res, 200, await soapStatus(m[1]));
    }
    if (req.method === 'POST' && url.pathname === '/webhooks/toyland') {
      const raw = await readBody(req);
      const bad = verifyWebhook(req, raw);
      if (bad) { onWebhook(null, 'TU CHOI: ' + bad); traceWebhook(req, raw, 401, 'Từ chối: ' + bad); return send(res, 401, { error: bad }); }
      const event = JSON.parse(raw);
      if (seenEvents.has(event.eventId)) { onWebhook(event, 'BO QUA: nhan trung'); traceWebhook(req, raw, 200, 'Bỏ qua: sự kiện trùng (đã xử lý trước đó)'); return send(res, 200, { status: 'duplicate' }); }
      seenEvents.add(event.eventId);
      const o = orders.find((x) => x.orderCode === event.data?.orderCode);
      if (o) { o.status = event.data.status; o.statusLabel = event.data.statusLabel; o.paymentStatus = event.data.paymentStatus ?? o.paymentStatus; }
      onWebhook(event, 'DA XU LY');
      traceWebhook(req, raw, 200, 'Chữ ký hợp lệ, đã cập nhật trạng thái đơn');
      return send(res, 200, { status: 'ok' });
    }
    send(res, 404, { message: 'Khong tim thay' });
  } catch (e) {
    send(res, e.status || 500, { message: e.message, code: e.code });
  }
});

server.listen(PORT, () => console.log(`Cong dai ly chay tai http://localhost:${PORT}  (ToyLand: ${BASE})`));
