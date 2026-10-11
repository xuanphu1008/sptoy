# Kịch bản demo 10 phút

Mục tiêu của buổi demo là cho giảng viên thấy ToyLand là một dịch vụ có nhiều bên tiêu thụ chứ không chỉ là một website. Thứ tự đi từ cái giảng viên quen nhìn (web) sang cái chứng minh tính dịch vụ (đối tác bên ngoài), rồi đến các điểm chịu lỗi.

## Chuẩn bị trước giờ demo

Làm xong trước khoảng 15 phút, vì các dịch vụ trên Render gói miễn phí ngủ sau 15 phút không có truy cập và lần đánh thức đầu mất khoảng 4 phút.

1. Mở lần lượt `https://sptoy-api.onrender.com/actuator/health`, `https://sptoy-notify.onrender.com/actuator/health` (địa chỉ theo tên service bạn đặt) và `https://sptoy-web.onrender.com` cho đến khi cả ba trả về bình thường.
2. Mở `https://sptoy-partner.onrender.com` (cổng đại lý, tên theo service bạn đặt). Trang phải hiện danh mục hàng.
3. Đăng nhập Admin, vào "Đối tác & API", kiểm tra đối tác của đại lý đang bật và ô Webhook đã trỏ về `https://sptoy-partner.onrender.com/webhooks/toyland`.
4. Chuẩn bị sẵn hai tab: web khách hàng và trang Admin "Đơn hàng". Chuẩn bị tab thứ ba là cổng đại lý.
5. Mở sẵn hộp thư email dùng để nhận thông báo.
6. Chuẩn bị sẵn SoapUI với project nạp từ `https://sptoy-api.onrender.com/soap/partner/partner.wsdl` (hoặc dùng nút "Tra qua SOAP" của cổng đại lý, xem bước 3).

## Diễn tiến

### 1. Web đặt hàng (1,5 phút)

Mở web khách hàng, đăng nhập, thêm một sản phẩm vào giỏ, chọn COD và đặt. Nói một câu: đây là một bên tiêu thụ, Angular chỉ biết hợp đồng REST, không biết CSDL. Chỉ cho giảng viên email xác nhận đơn vừa đến hộp thư, và nói rõ email không do `toyland-api` gửi trực tiếp mà đi qua hàng đợi sự kiện sang dịch vụ `toyland-notify`.

### 2. Đại lý đặt hàng qua REST, bấm hai lần (2 phút)

Sang tab cổng đại lý, chọn một sản phẩm ở "Danh mục hàng" (dữ liệu lấy qua REST có ký HMAC), bấm "Đặt cho khách", điền thông tin và bấm "Bấm hai lần liên tiếp". Kết quả mong đợi: hai yêu cầu cùng `Idempotency-Key`, chỉ có **một** đơn xuất hiện ở "Đơn đã đặt", và tồn kho chỉ giảm một lần. Giải thích: mỗi lần mở biểu mẫu đại lý sinh một khoá, gửi lại cùng khoá thì ToyLand trả lại đúng đơn cũ (bảng `idempotency_keys`).

Nếu giảng viên hỏi "còn nếu khoá khác nhưng cùng mã đơn bên đại lý": đổi khoá, giữ nguyên mã đơn đại lý thì nhận `409 PARTNER_REF_DUPLICATE`, đó là lớp chống trùng thứ hai.

### 3. Tra trạng thái đơn qua SOAP (1,5 phút)

Cách nhanh nhất: ở "Đơn đã đặt" bấm "Tra qua SOAP" cho đơn vừa tạo. Trang hiện trạng thái đơn lấy từ thao tác `GetOrderStatus`. Nếu muốn cho giảng viên thấy phong bì SOAP, mở SoapUI, chọn thao tác `GetOrderStatus`, điền `orderCode`, chỉ vào SOAP Header `Auth` (apiKey, timestamp, signature) và phần Body. Rồi gửi sai định dạng mã đơn (ví dụ `abc`) để thấy SOAP Fault do XSD chặn trước khi vào logic nghiệp vụ.

Câu nói gợi ý: REST là phong cách kiến trúc trên HTTP, SOAP là giao thức có phong bì, có hợp đồng WSDL/XSD chặt, đối tác kiểu doanh nghiệp thường dùng. ToyLand mở cả hai cổng, dùng chung một lõi nghiệp vụ.

### 4. Đổi trạng thái đơn, webhook về đại lý (2 phút)

Sang Admin, mở đơn của đại lý (lọc "Đối tác"), đổi trạng thái từ Chờ xác nhận sang Đã xác nhận. Quay lại cổng đại lý, mục "Webhook đã nhận" hiện sự kiện `order.status_changed` với kết quả "DA XU LY" sau vài giây, và trạng thái đơn ở bảng "Đơn đã đặt" đổi theo. Chỉ vào chữ ký: webhook mang `X-Toyland-Signature`, đại lý kiểm bằng secret và bỏ qua sự kiện trùng.

Mở thêm "Hàng đợi sự kiện" ở trang Admin "Đối tác & API": hai sự kiện `order.placed` và `order.status_changed` ở trạng thái Đã gửi.

### 5. Khi đối tác ngừng hoạt động (1,5 phút, nếu còn thời gian)

Tắt service cổng đại lý (hoặc đổi ô Webhook sang địa chỉ sai), đổi tiếp trạng thái đơn sang Đang đóng gói. Dịch vụ thông báo thử lại với khoảng chờ tăng dần (10, 20, 40 giây...), vẫn ở trạng thái chờ, đơn hàng của khách không bị ảnh hưởng. Bật lại đại lý, sự kiện tự được giao bù. Câu chốt: đây là "ít nhất một lần" cộng với xử lý idempotent ở phía nhận.

Lưu ý: bước này cần chờ nhiều phút nếu service Render ngủ, chỉ nên làm khi đã chuẩn bị trước, hoặc kể lại kết quả đã chạy thử và mở nhật ký.

### 6. Bảo mật: tài khoản khách gọi API quản trị (1 phút)

Dùng Postman (bộ sưu tập trong thư mục `postman`), đăng nhập bằng tài khoản khách rồi gọi `GET /api/v1/admin/orders` hoặc `GET /api/v1/admin/outbox`: nhận `403`. Gọi `GET /api/v1/orders/{id}` của đơn người khác: nhận `404`, không lộ việc đơn tồn tại. Gọi cổng đối tác với chữ ký sai: nhận `401 PARTNER_AUTH_FAILED`.

### 7. Chịu lỗi dịch vụ ngoài (tuỳ chọn, 0,5 phút)

ToyLand tiêu thụ GHN và MoMo. Mở `GhnClient.java` chỉ vào hai chú thích `@Retry` và `@CircuitBreaker`, và cấu hình trong `soa-defaults.properties`. Khi GHN lỗi, phí ship rơi về phí cố định (đoạn `catch` trong `ShippingService.fee`) và khách vẫn đặt hàng được. Với MoMo, nêu job đối soát 5 phút/lần hỏi lại giao dịch đang chờ. Phần này kể bằng mã nguồn, không cần thao tác trực tiếp.

## Các kết quả chạy thử đi kèm để mở khi bị hỏi

- `mvn test` của `toyland-api`: 41 kiểm thử, gồm 9 ca tích hợp (đặt đơn, gửi lại cùng khoá, trùng mã đơn đại lý, đại lý B không xem được đơn của A, sáu yêu cầu tranh nhau sản phẩm cuối chỉ một đơn thành công, sự kiện outbox ghi cùng giao dịch) và kiểm tra ranh giới miền bằng ArchUnit.
- `mvn test` của `toyland-notify`: 7 kiểm thử về backoff, chữ ký và luồng gửi.
- `tools/soa-demo.mjs` (các lệnh `partner`, `soap`, `idem`, `order`) và `tools/k6/*.js` cho tải.

## Nếu có sự cố trong buổi demo

| Hiện tượng | Nguyên nhân thường gặp | Cách xử lý nhanh |
|---|---|---|
| Trang không tải, chờ lâu | service Render đang ngủ | chờ đến 4 phút, trong lúc đó trình bày sơ đồ kiến trúc |
| Webhook không đến | ô Webhook sai, hoặc `toyland-notify` ngủ | kiểm tra ô Webhook ở Admin, mở `/actuator/health` của notify, xem "Hàng đợi sự kiện" |
| Email không đến | hộp thư chậm, hoặc SMTP chưa cấu hình ở notify | xem email trong thư rác, xem nhật ký của notify |
| `401 PARTNER_TIMESTAMP_EXPIRED` | đồng hồ máy lệch quá 5 phút | đồng bộ giờ máy |
| `429` | vượt hạn mức đối tác | đợi 60 giây hoặc nâng hạn mức ở Admin |
