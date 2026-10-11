# Danh mục dịch vụ của ToyLand

Tài liệu này trả lời câu hỏi "thành phần nào của hệ thống là một dịch vụ, nó nhận gì, trả gì, ai được gọi". Cách đọc: ToyLand gồm hai đơn vị triển khai độc lập (`toyland-api` và `toyland-notify`) và một số **miền dịch vụ** nằm trong `toyland-api`. Miền là ranh giới về mã nguồn và dữ liệu, không phải tiến trình riêng. Ranh giới được giữ bằng bài kiểm thử `ArchitectureTest` (ArchUnit): một miền chỉ gọi miền khác qua giao diện công bố (`UserApi`, `CatalogApi`, `OrderQueryApi`, `ReviewStatsApi`, `VoucherUsageApi`, `PartnerOrderApi`, `PartnerWebhookApi`), không chạm vào repository của miền đó.

## 1. Đơn vị triển khai

| Đơn vị | Vai trò | Dữ liệu | Giao thức vào | Giao thức ra |
|---|---|---|---|---|
| `toyland-api` | cung cấp dịch vụ bán hàng cho web, đối tác; tiêu thụ MoMo, GHN, Cloudinary, SMTP | schema `toyland_v2` | REST JSON `/api/v1`, REST đối tác `/api/partner/v1`, SOAP `/soap/partner`, IPN của MoMo | HTTP tới MoMo, GHN, Cloudinary; HTTP có ký tới `toyland-notify` |
| `toyland-notify` | gửi email và webhook thay cho `toyland-api`, thử lại khi lỗi | schema riêng `toyland_notify` | `POST /internal/events` (token nội bộ + HMAC) | SMTP, webhook HTTPS tới đối tác |
| `partner-demo` | đại lý mẫu viết bằng Node.js, chứng minh hợp đồng dùng được từ nơi khác | không có CSDL | trang web đại lý, `POST /webhooks/toyland` | REST + SOAP tới `toyland-api` |

## 2. Dịch vụ phía ToyLand cung cấp

Cột "Quyền" ghi ai gọi được. `Khách` là người dùng đã đăng nhập, `Admin` là vai trò ADMIN, `Đối tác` là khoá API + chữ ký.

| Dịch vụ (miền) | Trách nhiệm | Vào | Ra | Lỗi chính | Quyền | Bảng sở hữu | Bên tiêu thụ |
|---|---|---|---|---|---|---|---|
| Tài khoản (`auth`, `user`) | đăng ký, đăng nhập, làm mới token, quên mật khẩu, xác nhận email, hồ sơ | email, mật khẩu | cặp JWT, hồ sơ | 401 `BAD_CREDENTIALS`, 403 `EMAIL_NOT_VERIFIED` / `ACCOUNT_DISABLED`, 409 email trùng, 429 đăng nhập quá nhiều | công khai (đăng nhập), Khách, Admin | users, refresh_tokens, password_reset_tokens, email_verification_tokens, addresses | web |
| Danh mục (`catalog`) | tìm kiếm, chi tiết sản phẩm, biến thể, tồn bán được | bộ lọc, trang, sắp xếp | trang sản phẩm, chi tiết | 404, 400 | công khai; quản trị cần Admin; đối tác đọc qua cổng đối tác | categories, brands, franchises, products, product_variants, product_images, product_specs | web, đối tác |
| Giỏ hàng (`cart`, `wishlist`) | giữ hàng chọn mua, xem trước tính tiền | biến thể, số lượng | giỏ, bản tính thử | 400 vượt tồn, 404 | Khách (công khai riêng cho `cart/preview`) | cart_items, wishlist_items | web |
| Đơn hàng (`order`) | tính tiền, đặt đơn, đổi trạng thái, huỷ, trả hàng, tra cứu | giỏ hoặc danh sách SKU, địa chỉ | đơn, lịch sử trạng thái | 400, 409 `IDEMPOTENCY_*`, 409 `CONCURRENT_UPDATE`, 404 | Khách (chỉ đơn của mình), Admin, Đối tác (chỉ đơn của mình) | orders, order_items, order_status_history, return_requests | web, đối tác |
| Thanh toán (`payment`) | tạo giao dịch MoMo, nhận IPN, đối soát, hoàn tiền, sổ thu COD | đơn, kết quả MoMo | liên kết thanh toán, sổ giao dịch | 502 `MOMO_ERROR`, 400 sai chữ ký | Khách (trả tiền), Admin (sổ, hoàn), MoMo (IPN có chữ ký) | payment_transactions | web, MoMo |
| Kho (`inventory`, `supplier`) | trừ/hoàn kho, nhập kho, điều chỉnh, lịch sử biến động | biến thể, số lượng, lý do | tồn mới, phiếu | 400 `OUT_OF_STOCK`, 409 | Admin; nội bộ khi đặt/huỷ đơn | stock_receipts, stock_receipt_items, stock_movements, suppliers | nội bộ |
| Vận chuyển (`shipping`) | danh mục địa giới, tính phí GHN | tỉnh/huyện/xã, khối lượng | phí và nguồn (GHN hoặc cố định) | 503 `GHN_DISABLED`, 502 `GHN_ERROR` | công khai | không có (chỉ đọc từ GHN, có cache) | web |
| Khuyến mãi (`promotion`) | voucher, chiến dịch giảm giá, banner | mã voucher | mức giảm | 400, 409 | Khách, Admin | vouchers, voucher_usages, campaigns, banners | đơn hàng, web |
| Đánh giá (`review`) | đánh giá sau mua, kiểm duyệt từ cấm | sao, nội dung | đánh giá, thống kê | 403 `CANNOT_REVIEW`, 400 `BANNED_KEYWORD`, 409 | Khách, Admin | reviews, banned_keywords | web |
| Điểm thưởng (`loyalty`) | cộng, đổi, hoàn điểm | đơn | số dư, lịch sử | 400, 409 | Khách, Admin | point_transactions | đơn hàng |
| Thông báo trong ứng dụng (`notification`) | thông báo cho khách và nhân viên (WebSocket + REST) | sự kiện nội bộ | danh sách thông báo | 404 | Khách, Admin | notifications | web |
| Cổng đối tác REST (`partner`) | danh mục, tồn kho, đặt/xem/huỷ đơn cho bên thứ ba | `X-Api-Key`, `X-Timestamp`, `X-Signature`, thân JSON | JSON, `X-Request-Id` | 401 `PARTNER_AUTH_*`, 429, 409 `PARTNER_REF_DUPLICATE`, 422 `SKU_*` | Đối tác | partners, idempotency_keys | đại lý |
| Cổng đối tác SOAP (`partner.soap`) | `GetStock`, `GetProduct`, `GetOrderStatus` theo WSDL | phong bì SOAP có Header `Auth` | phong bì SOAP hoặc SOAP Fault | Fault `Client` (xác thực, XSD), `Server` | Đối tác | dùng chung với REST | đại lý doanh nghiệp |
| Phát sự kiện (`outbox`) | ghi sự kiện cùng giao dịch, chuyển sang `toyland-notify`, thử lại | sự kiện đơn hàng | trạng thái NEW, SENT, DEAD | 502 nội bộ ghi vào `last_error` | Admin (xem, gửi lại) | outbox_events | `toyland-notify` |
| Nhật ký và báo cáo (`audit`, `report`) | ghi thao tác quản trị, thống kê doanh thu | khoảng ngày | bảng, tệp Excel | 400 | Admin | audit_logs | Admin |

## 3. Dịch vụ `toyland-notify`

| Việc | Vào | Ra | Lỗi chính | Quyền |
|---|---|---|---|---|
| Nhận sự kiện | `POST /internal/events`, thân JSON `eventId`, `type`, `email`, `webhook`; tiêu đề `X-Toyland-Timestamp`, `X-Toyland-Signature` | 202 và trạng thái "đã nhận" hoặc "trùng" | 401 chữ ký sai hoặc lệch giờ, 400 | token nội bộ dùng chung với `toyland-api` |
| Gửi email | địa chỉ, tiêu đề, HTML trong sự kiện | SENT hoặc thử lại | lỗi SMTP được ghi lại, thử lại giãn dần | nội bộ |
| Gửi webhook | địa chỉ + secret đối tác trong sự kiện | SENT hoặc thử lại, DEAD sau 6 lần | HTTP không phải 2xx, quá 10 giây | nội bộ |
| Xem và gửi lại | `GET /internal/deliveries`, `POST /internal/deliveries/{id}/retry` | danh sách lượt gửi | 401 | token nội bộ |

Tính idempotent: bảng `processed_events` có khoá chính `event_id`. Sự kiện đến lần hai bị nhận biết và không tạo thêm lượt gửi.

## 4. Dịch vụ bên ngoài mà ToyLand tiêu thụ

| Dịch vụ ngoài | Dùng để | Cách bảo vệ | Khi dịch vụ ngoài lỗi |
|---|---|---|---|
| MoMo | tạo giao dịch, nhận IPN, hoàn tiền, truy vấn trạng thái | ký HMAC-SHA256 hai chiều, kiểm số tiền, khoá dòng đơn khi xử lý kết quả | truy vấn thử lại tối đa 2 lần, cầu dao `momo-query`; job đối soát 5 phút/lần; trả tiền sau khi đơn đã huỷ thì chuyển sang chờ hoàn |
| GHN | địa giới, phí vận chuyển | timeout 3 giây kết nối và 5 giây đọc; thử lại tối đa 3 lần, cầu dao `ghn` | rơi về phí cố định, vẫn đặt hàng được |
| Cloudinary | lưu ảnh | chỉ nhận ảnh hợp lệ, giới hạn dung lượng | báo lỗi tải ảnh, không ảnh hưởng đặt hàng |
| SMTP (Brevo) | gửi email | qua `toyland-notify`, cổng 2525 | thử lại, sau đó DEAD, đơn không bị ảnh hưởng |

## 5. Quy tắc sở hữu dữ liệu

Mỗi bảng thuộc đúng một miền ở cột "Bảng sở hữu" trên. Miền khác muốn dữ liệu đó phải gọi giao diện công bố của miền chủ, ví dụ báo cáo hỏi đơn qua `OrderQueryApi`, đơn hàng hỏi giá và tồn qua `CatalogApi`. Hiện các miền còn dùng chung một schema MySQL để triển khai đơn giản trên gói miễn phí, nhưng vì không miền nào truy vấn bảng của miền khác nên có thể tách schema về sau mà không đổi hợp đồng. `toyland-notify` đã làm đúng điều đó: schema riêng, không bảng nào dùng chung.
