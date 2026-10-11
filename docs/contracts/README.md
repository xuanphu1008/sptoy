# Hợp đồng dịch vụ ToyLand dành cho đối tác

Tài liệu này dành cho lập trình viên của bên thứ ba muốn tích hợp với ToyLand. ToyLand cung cấp hai cách gọi (REST và SOAP) và một chiều ngược lại (webhook). Các tệp trong thư mục này là hợp đồng chính thức, tài liệu này chỉ giải thích cách dùng.

| Tệp | Nội dung |
|---|---|
| `partner-v1.yaml` | Hợp đồng OpenAPI 3 của REST `/api/partner/v1` (danh mục, tồn kho, đặt đơn, huỷ đơn) |
| `public-v1.yaml` | Hợp đồng OpenAPI 3 của API công khai dùng cho giao diện web |
| `toyland-partner.wsdl`, `partner.xsd` | Hợp đồng SOAP (contract-first), địa chỉ chạy thật tại `/soap/partner/partner.wsdl` |

## 1. Cấp khoá và xác thực

Quản trị viên tạo đối tác ở trang Admin, mục "Đối tác & API". Hệ thống trả về `apiKey` và `secret` đúng một lần. Secret không lưu dạng đọc lại được ở giao diện, nên mất thì phải tạo đối tác mới.

Mọi yêu cầu REST gửi ba header:

| Header | Giá trị |
|---|---|
| `X-Api-Key` | khoá API của đối tác |
| `X-Timestamp` | thời gian hiện tại, số giây Unix |
| `X-Signature` | chữ ký HMAC-SHA256 dạng hex thường |

Chữ ký được tính trên chuỗi chuẩn gồm bốn dòng nối bằng ký tự xuống dòng `\n`:

```
<X-Timestamp>
<METHOD viết hoa>
<đường dẫn kèm query nếu có>
<SHA-256 hex của thân yêu cầu, thân rỗng thì băm chuỗi rỗng>
```

`X-Signature = hex(HMAC-SHA256(secret, chuỗi chuẩn))`. Máy chủ từ chối yêu cầu có timestamp lệch quá 5 phút, nên đồng hồ máy đối tác cần chạy NTP. Mỗi đối tác có hạn mức số yêu cầu mỗi phút, vượt hạn mức nhận `429` kèm header `Retry-After: 60`.

Ví dụ Node.js, đây cũng là đoạn đang chạy trong `partner-demo/server.js`:

```js
const crypto = require('crypto');
function sign(secret, method, pathWithQuery, bodyString, ts) {
  const bodyHash = crypto.createHash('sha256').update(bodyString || '').digest('hex');
  const canonical = [ts, method.toUpperCase(), pathWithQuery, bodyHash].join('\n');
  return crypto.createHmac('sha256', secret).update(canonical).digest('hex');
}
```

SOAP dùng cùng khoá nhưng đặt trong SOAP Header `Auth` (các phần tử `apiKey`, `timestamp`, `signature`). Chữ ký SOAP là `HMAC-SHA256(secret, timestamp + "\n" + tênThaoTác + "\n" + (sku hoặc orderCode))`. Tên thao tác là `GetStock`, `GetProduct` hoặc `GetOrderStatus`.

## 2. Đặt đơn an toàn khi gọi lại: Idempotency-Key

`POST /api/partner/v1/orders` bắt buộc có header `Idempotency-Key` (chữ, số và các ký tự `. _ : -`, tối đa 80 ký tự). Đối tác sinh khoá một lần cho mỗi ý định đặt hàng và dùng lại đúng khoá đó khi phải gọi lại do mất mạng hay hết thời gian chờ.

- Cùng khoá, cùng thân yêu cầu: máy chủ trả lại nguyên kết quả của lần đầu, không tạo đơn thứ hai.
- Cùng khoá nhưng thân khác: `409 IDEMPOTENCY_KEY_REUSED`.
- Yêu cầu đầu tiên chưa xử lý xong mà yêu cầu thứ hai đã tới: `409 IDEMPOTENCY_IN_PROGRESS`, đối tác chờ vài giây rồi gọi lại.

Ngoài ra mỗi đơn mang `externalRef`, mã đơn trong hệ thống của đối tác (tối đa 64 ký tự). Cùng một đối tác không đặt hai đơn trùng `externalRef` (`409 PARTNER_REF_DUPLICATE`), đây là lớp chống trùng thứ hai, độc lập với khoá idempotency.

## 3. Bảng mã lỗi

Mọi lỗi REST có dạng JSON `{ "status", "code", "message", "path" }`. Đối tác xử lý theo `code`, không theo nội dung `message`.

| HTTP | `code` | Ý nghĩa | Đối tác nên làm |
|---|---|---|---|
| 401 | `PARTNER_AUTH_MISSING` | thiếu một trong ba header xác thực | bổ sung header |
| 401 | `PARTNER_TIMESTAMP_INVALID` | `X-Timestamp` không phải số giây Unix | sửa định dạng |
| 401 | `PARTNER_TIMESTAMP_EXPIRED` | timestamp lệch quá 5 phút | đồng bộ đồng hồ rồi gọi lại |
| 401 | `PARTNER_AUTH_FAILED` | khoá sai, đối tác bị tắt hoặc chữ ký sai | kiểm tra khoá và cách ký, không tự lặp lại |
| 429 | `PARTNER_RATE_LIMITED` | vượt hạn mức mỗi phút | đợi theo `Retry-After` |
| 400 | `IDEMPOTENCY_KEY_REQUIRED` / `IDEMPOTENCY_KEY_INVALID` | thiếu hoặc sai định dạng khoá | sinh khoá đúng định dạng |
| 409 | `IDEMPOTENCY_KEY_REUSED` | khoá đã dùng với thân khác | dùng khoá mới cho đơn mới |
| 409 | `IDEMPOTENCY_IN_PROGRESS` | yêu cầu cùng khoá đang chạy | chờ rồi gọi lại cùng khoá |
| 409 | `PARTNER_REF_DUPLICATE` | `externalRef` đã có đơn | lấy đơn cũ bằng `GET` |
| 400 | `PARTNER_REF_INVALID` | `externalRef` rỗng hoặc dài quá 64 ký tự | sửa dữ liệu |
| 400 | `PARTNER_ITEMS_INVALID` | danh sách hàng rỗng hoặc số lượng không hợp lệ | sửa dữ liệu |
| 422 | `SKU_NOT_SELLABLE` | SKU không tồn tại hoặc đã ngừng bán | kiểm tra lại danh mục |
| 422 | `SKU_OUT_OF_STOCK` | không đủ tồn kho cho số lượng đặt | hỏi lại tồn kho, giảm số lượng hoặc đổi SKU |
| 409 | `CONCURRENT_UPDATE` | hai yêu cầu cùng tranh một dòng tồn kho | gọi lại cùng `Idempotency-Key` sau ít giây |
| 409 | `ORDER_NOT_CANCELLABLE` | đơn đã qua bước cho phép huỷ | không huỷ được, liên hệ cửa hàng |
| 5xx | (nhiều mã) | lỗi phía ToyLand | gọi lại cùng `Idempotency-Key` sau vài giây, tăng dần khoảng chờ |

Quy tắc chung: lỗi 4xx là lỗi do yêu cầu, gọi lại nguyên văn sẽ lỗi lại, riêng `409 IDEMPOTENCY_IN_PROGRESS` và `429` là ngoại lệ. Lỗi 5xx và hết thời gian chờ thì gọi lại được, và chỉ an toàn với `POST /orders` vì đã có `Idempotency-Key`.

Mỗi phản hồi mang header `X-Request-Id`. Đối tác có thể gửi sẵn header này (chỉ chữ, số, `.`, `_`, `-`, tối đa 64 ký tự) để dễ đối chiếu log hai phía, nếu không gửi ToyLand tự sinh. Khi báo lỗi cho ToyLand, hãy kèm mã này.

SOAP trả lỗi bằng SOAP Fault. Dữ liệu sai lược đồ XSD (SKU sai định dạng, mã đơn sai mẫu) bị chặn ngay ở bước kiểm tra lược đồ, trước khi vào logic nghiệp vụ.

## 4. Webhook ToyLand gọi sang đối tác

Đối tác đăng ký địa chỉ nhận ở trang Admin (ô Webhook, phải là `https://`). Mỗi lần trạng thái đơn của đối tác đổi, ToyLand gửi `POST` JSON tới địa chỉ đó. Loại sự kiện hiện có: `order.placed` và `order.status_changed`.

Thân yêu cầu không chứa email hay thông tin cá nhân của khách:

```json
{
  "eventId": "c6c074e0-6ec4-49a7-afe6-932dc6c780ed",
  "type": "order.status_changed",
  "occurredAt": "2026-10-11T07:25:03Z",
  "data": { "orderCode": "TL261011197033", "externalRef": "DL-531b71b0-365b-46cb-a", "previousStatus": "PENDING", "status": "CONFIRMED", "statusLabel": "Đã xác nhận", "paymentStatus": "UNPAID", "total": 929000, "note": "Da xac nhan" }
}
```

Ba header đi kèm: `X-Toyland-Event-Id`, `X-Toyland-Timestamp` (số giây Unix), `X-Toyland-Signature: sha256=<hex>`. Chữ ký là `HMAC-SHA256(secret, timestamp + "." + thân yêu cầu nguyên văn)`, dùng chính secret đã cấp khi tạo đối tác.

Phía đối tác cần làm bốn việc:

1. Tính lại chữ ký trên thân yêu cầu thô (trước khi parse JSON) và so sánh bằng hàm so sánh thời gian không đổi.
2. Từ chối nếu timestamp lệch quá 5 phút.
3. Lưu `eventId` đã xử lý và bỏ qua sự kiện trùng, vì ToyLand gửi theo nguyên tắc "ít nhất một lần".
4. Trả `2xx` trong vòng 10 giây khi đã nhận. Mọi mã khác, hay không trả lời, đều coi là thất bại.

Khi thất bại, ToyLand gửi lại với khoảng chờ tăng dần (10 giây, 20 giây, 40 giây... tối đa 10 phút, có thêm độ trễ ngẫu nhiên nhỏ), tổng cộng 6 lần. Sau đó lượt gửi chuyển sang trạng thái lỗi hẳn (`DEAD`) ở dịch vụ thông báo, người vận hành xem được qua `GET /internal/deliveries` và đưa vào gửi lại bằng `POST /internal/deliveries/{id}/retry` (hai đường này cần token nội bộ, không mở cho đối tác). Thứ tự giữa các sự kiện không được đảm bảo tuyệt đối khi có gửi lại, nên đối tác so `occurredAt` hoặc gọi `GET /orders/{mã}` để lấy trạng thái mới nhất.

Đoạn kiểm tra mẫu:

```js
function verify(secret, ts, rawBody, header) {
  if (Math.abs(Date.now() / 1000 - Number(ts)) > 300) return false;
  const expected = 'sha256=' + crypto.createHmac('sha256', secret).update(ts + '.' + rawBody).digest('hex');
  const a = Buffer.from(expected), b = Buffer.from(header || '');
  return a.length === b.length && crypto.timingSafeEqual(a, b);
}
```

## 5. Phiên bản và thay đổi hợp đồng

Phiên bản nằm trong đường dẫn (`/api/partner/v1`). Trong cùng một phiên bản, ToyLand chỉ thêm, không bớt hay đổi nghĩa:

- Được phép thêm trường mới vào phản hồi, thêm thao tác mới, thêm loại sự kiện webhook mới. Đối tác phải bỏ qua trường và loại sự kiện chưa biết.
- Không được xoá trường, đổi kiểu dữ liệu, đổi nghĩa mã lỗi hiện có hay siết điều kiện đầu vào. Những thay đổi này chỉ ra mắt ở `v2`.
- Khi `v2` ra đời, `v1` được giữ ít nhất 6 tháng song song và thông báo trước cho đối tác qua email.

Hợp đồng SOAP đổi theo cách tương tự: thao tác mới đi kèm phần tử mới trong XSD, thay đổi phá vỡ thì ra WSDL mới với `targetNamespace` mới.

## 6. Thử nhanh

Trong thư mục `tools` có `soa-demo.mjs` gọi lần lượt các thao tác REST và SOAP, và thư mục `partner-demo` là một cổng đại lý hoàn chỉnh (đặt đơn, huỷ đơn, tra trạng thái bằng SOAP, nhận webhook). Cách chạy nằm ở `partner-demo/README.md`.
