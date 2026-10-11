# Cổng đại lý (partner-demo)

Ứng dụng Node.js độc lập, đóng vai một đại lý dùng dịch vụ đối tác của ToyLand. Không dùng chung mã với backend Java và Angular,
nên chứng minh hợp đồng dịch vụ (REST + SOAP + webhook) độc lập ngôn ngữ và độc lập giao diện.

| Việc | Cách làm |
|---|---|
| Lấy danh mục | REST `GET /api/partner/v1/products`, ký HMAC-SHA256 |
| Đặt hàng | REST `POST /api/partner/v1/orders`, `Idempotency-Key` sinh một lần cho mỗi lần mở biểu mẫu |
| Huỷ đơn | REST `POST /api/partner/v1/orders/{mã}/cancel` |
| Tra trạng thái | SOAP `GetOrderStatus`, client sinh từ WSDL bằng thư viện `soap`, xác thực bằng SOAP Header `Auth` |
| Nhận thông báo | Webhook `POST /webhooks/toyland`, kiểm tra chữ ký HMAC, timestamp, bỏ qua sự kiện trùng |

## Chạy trên máy

```
cd partner-demo
npm install
set TOYLAND_BASE_URL=https://sptoy-api.onrender.com
set PARTNER_API_KEY=<apiKey tạo ở Admin > Đối tác & API>
set PARTNER_SECRET=<secret>
node server.js
```

(PowerShell: `$env:TOYLAND_BASE_URL="..."`.) Mở `http://localhost:3000`.

## Nhận webhook

ToyLand phải gọi được vào ứng dụng này. Khi chạy trên máy thì dùng bản triển khai trên Render (xem dưới) hoặc một tunnel.
Dán địa chỉ webhook (hiện ở đầu trang, dạng `https://<tên>.onrender.com/webhooks/toyland`) vào ô Webhook của đối tác ở trang
Admin > Đối tác & API.

Mỗi webhook có 3 header: `X-Toyland-Event-Id`, `X-Toyland-Timestamp`, `X-Toyland-Signature: sha256=<hex>`.
Chữ ký = HMAC-SHA256(secret, `timestamp + "." + thân yêu cầu`). Hướng dẫn kiểm tra đầy đủ ở `docs/contracts/README.md`.

## Triển khai Render

- New > Web Service, chọn repo, **Root Directory** `partner-demo`, Runtime Node.
- Build Command `npm install`, Start Command `node server.js`.
- Environment: `TOYLAND_BASE_URL`, `PARTNER_API_KEY`, `PARTNER_SECRET`.
