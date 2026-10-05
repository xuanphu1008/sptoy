# ToyLand v2 – Hệ thống Quản lý Cửa hàng Đồ chơi trực tuyến

Đồ án học phần **Phát triển phần mềm hướng dịch vụ** – kiến trúc Client/Server (SOA):

| Thành phần | Công nghệ | Thư mục |
|---|---|---|
| CSDL | MySQL 8 | `database/toyland_v2.sql` |
| Backend (RESTful Web Service) | Spring Boot 3.5.5 · Spring Security 6 + JWT (access + refresh token) · JPA/Hibernate · WebSocket STOMP (thông báo realtime) · Apache POI (xuất Excel) · springdoc Swagger · Mail | `backend/toyland-api` |
| Frontend (Client) | Angular 19 (standalone, signals) · Angular Material 3 · Chart.js · @stomp/stompjs | `frontend/toyland-web` |
| Tích hợp bên thứ ba | Ví MoMo – môi trường thử nghiệm (thanh toán, hoàn tiền) · GHN (phí vận chuyển, địa chỉ 3 cấp) · Mailtrap/SMTP (email) | – |
| Kiểm thử | Postman/Newman (171 request có test tự động) · JUnit 5 | `postman/`, `backend/toyland-api/src/test` |
| Triển khai | Docker Compose (MySQL + API + Nginx) | `docker-compose.yml` |

> Bản v1 (`backend/toyland-backend`, `frontend/toyland-frontend`, `database/toyland_db.sql`, `postman/ToyLand.postman_collection.json`) không còn dùng — có thể xoá thủ công sau khi v2 chạy ổn.

Tài liệu thiết kế chi tiết: [`docs/THIET-KE-V2.md`](docs/THIET-KE-V2.md) · Đề xuất nâng cấp đã chốt: [`docs/DE-XUAT-NANG-CAP.md`](docs/DE-XUAT-NANG-CAP.md)

---

## 1. Tạo CSDL

1. Mở MySQL Workbench (hoặc phpMyAdmin/XAMPP).
2. Chạy toàn bộ file `database/toyland_v2.sql` → tạo mới schema **`toyland_v2`** (xoá nếu đã có) kèm dữ liệu mẫu:
   - 17 danh mục (2 cấp), 21 thương hiệu, 9 nhân vật/chủ đề, 101 sản phẩm / 176 biến thể (tối đa 2 thuộc tính)
   - 90 đơn hàng rải trong 75 ngày gần nhất (để dashboard có số liệu), đánh giá, yêu cầu trả hàng, phiếu nhập kho
   - Voucher: `TOYLAND10`, `GIAM50K`, `FREESHIP`, `CHAOBAN`, `TET2025` (đã hết hạn) · 2 chương trình khuyến mãi · 3 banner
   - Nhà cung cấp, từ khoá cấm, điểm thưởng khách hàng, thông báo, nhật ký hoạt động mẫu
3. **Đã có CSDL `toyland_v2` cũ và không muốn tạo lại?** Chạy lần lượt (mỗi file chạy lại nhiều lần cũng không bị nhân đôi):
   - `database/toyland_v2_them_san_pham.sql` – nạp thêm 62 sản phẩm / 122 biến thể (nếu CSDL mới có 39 sản phẩm).
   - `database/toyland_v2_nang_cap_chuc_nang.sql` – **bắt buộc cho bản nâng cấp chức năng**: thêm bảng/cột cho nhà cung cấp, báo cáo lợi nhuận (giá vốn trên dòng đơn), kiểm duyệt đánh giá, điểm thưởng, thông báo, nhật ký; tính lại điểm cho các đơn đã hoàn tất.
   - `database/toyland_v2_xac_nhan_email.sql` – **bắt buộc cho chức năng xác nhận email**: thêm cột `users.email_verified_at` + bảng `email_verification_tokens`; mọi tài khoản đang có được coi là đã xác nhận.
   - `database/toyland_v2_thanh_toan.sql` – **bắt buộc cho chức năng Thanh toán & đối soát**: thêm trạng thái *Chờ hoàn tiền*, biến `payment_transactions` thành sổ thu – chi (cột `kind`, `note`, `created_by`) và ghi bù bút toán COD / hoàn tiền cho dữ liệu cũ.
   - `database/toyland_v2_momo.sql` – **bắt buộc khi chuyển sang MoMo**: đổi đơn/giao dịch VNPay cũ sang MoMo và cập nhật ràng buộc `payment_method` (COD/MOMO).
   - `database/toyland_v2_bam_token.sql` – dọn refresh token / mã đặt lại mật khẩu cũ (lưu dạng rõ) sau khi backend chuyển sang **chỉ lưu mã băm SHA-256**. Không đổi cấu trúc bảng; mọi người dùng phải đăng nhập lại một lần.

**Tài khoản mẫu**

| Vai trò | Username | Mật khẩu |
|---|---|---|
| Quản trị viên (ADMIN) | `admin` | `admin123` |
| Nhân viên (STAFF) | `staff` | `staff123` |
| Khách hàng | `khachhang` (và `tranthimai`, `lequanghuy`, `phamthuha`…) | `123456` |
| Khách **chưa xác nhận email** (demo bị chặn đặt hàng) | `chuaxacnhan` | `123456` |

## 2. Chạy Backend (Eclipse)

1. `File → Import → Maven → Existing Maven Projects` → chọn `backend/toyland-api`.
2. Sửa `src/main/resources/application.properties`:
   - `spring.datasource.password=` mật khẩu MySQL của bạn.
   - (Tuỳ chọn) GHN, MoMo, Mailtrap – xem mục 4. **Để trống vẫn chạy được**: phí ship cố định 30.000đ, MoMo dùng khoá thử nghiệm có sẵn, email chỉ ghi ra log.
3. Chuột phải `ToylandApiApplication.java` → `Run As → Spring Boot App` (hoặc `Java Application`).
4. Kiểm tra: <http://localhost:8080/swagger-ui.html> (bấm **Authorize**, dán `accessToken` lấy từ `/api/v1/auth/login`).

> Dự án đóng gói `war` + `ServletInitializer` như các lab, vẫn chạy trực tiếp bằng Tomcat nhúng. Yêu cầu JDK 17+ (đã thử với JDK 21).

## 3. Chạy Frontend (VS Code)

```bash
cd frontend/toyland-web
npm install        # chạy lại sau khi cập nhật: có thêm thư viện @stomp/stompjs
npm start          # = ng serve → http://localhost:4200
```

- Cửa hàng: <http://localhost:4200> · Trang quản trị: <http://localhost:4200/admin> (đăng nhập `admin` hoặc `staff`).
- Địa chỉ API cấu hình ở `src/environments/environment.ts` (`http://localhost:8080/api/v1`).

## 4. Cấu hình tích hợp (tuỳ chọn)

### 4.1 GHN – phí vận chuyển thật + chọn địa chỉ Tỉnh/Quận/Phường
1. Đăng ký tài khoản môi trường thử nghiệm tại <https://5sao.ghn.dev>, tạo cửa hàng.
2. Lấy **Token** (Chủ cửa hàng → Xem thông tin → Token API) và **ShopId**.
3. Điền `app.ghn.token=` và `app.ghn.shop-id=` → khởi động lại backend. Hộp thoại địa chỉ sẽ chuyển sang chọn 3 cấp từ GHN; phí ship tính theo cân nặng/kích thước hộp.

### 4.2 Ví MoMo – thanh toán online (môi trường thử nghiệm)
- **Không cần cấu hình**: backend dùng sẵn bộ khoá thử nghiệm công khai của MoMo (`partnerCode=MOMO`) và gọi `https://test-payment.momo.vn`. Không cần sửa `application.properties`.
- Có tài khoản MoMo Business riêng thì ghi đè bằng biến môi trường: `APP_MOMO_PARTNER_CODE`, `APP_MOMO_ACCESS_KEY`, `APP_MOMO_SECRET_KEY` (tắt hẳn MoMo: `APP_MOMO_ENABLED=false`).
- Luồng: đặt hàng chọn **MoMo** → backend gọi API tạo giao dịch (ký HMAC-SHA256) → chuyển sang trang MoMo → thanh toán → MoMo đưa về `/payment/momo-return` → frontend gửi tham số cho backend kiểm tra chữ ký → đơn chuyển **Chờ xác nhận / Đã thanh toán**. Quá 15 phút chưa trả → tự huỷ, hoàn kho. Lỗi/huỷ → vào *Đơn mua* bấm **Thanh toán MoMo** để trả lại.
- Phương thức trên trang MoMo (`app.momo.request-type`, mặc định `payWithMethod` – khách tự chọn QR / ATM / Visa). Nếu trang MoMo báo lỗi loại giao dịch, đặt biến môi trường `APP_MOMO_REQUEST_TYPE=captureWallet` (chỉ quét QR).
- **Thẻ ATM thử nghiệm:** `9704 0000 0000 0018` · Tên `NGUYEN VAN A` · Ngày phát hành `03/07` · OTP `OTP`. (Thẻ `...0026` = thẻ bị khoá, `...0034` = không đủ tiền – để demo trường hợp lỗi.)
- **Thẻ Visa/Master thử nghiệm:** `5200 0000 0000 1096` · `NGUYEN VAN A` · `05/26` · CVC `111` · OTP `OTP`.
- **Quét QR:** cài *MoMo Test App* theo hướng dẫn tại <https://developers.momo.vn/v3/docs/payment/onboarding/test-instructions/>.
- **IPN (MoMo gọi thẳng về server):** chạy localhost thì MoMo không gọi tới được – bước kiểm tra ở trang `/payment/momo-return` đã đủ để xác nhận đơn. Muốn demo IPN: chạy `ngrok http 8080` rồi đặt `APP_MOMO_IPN_URL=https://<mã>.ngrok-free.app/api/v1/payments/momo/ipn`.
- **Hoàn tiền:** ở *Thanh toán & đối soát → Chờ hoàn tiền*, đơn trả bằng MoMo có lựa chọn **Hoàn về ví MoMo (tự động)**: hệ thống gọi API hoàn tiền của MoMo và ghi mã giao dịch hoàn vào sổ thu – chi.
- Đang dùng CSDL cũ có đơn VNPay → chạy `database/toyland_v2_momo.sql` (mục 1).

### 4.3 Thông báo realtime (WebSocket)
- Không cần cấu hình. Backend mở endpoint `ws://localhost:8080/ws` (STOMP), client gửi JWT trong khung `CONNECT`.
- Kênh riêng từng người: `/user/queue/notifications`. Nhân viên/quản trị nhận: đơn mới, khách huỷ đơn, yêu cầu trả hàng, đánh giá vi phạm, sắp hết hàng. Khách nhận: đơn đổi trạng thái, được cộng/hoàn điểm.
- Mở 2 trình duyệt (một khách đặt hàng, một admin) để thấy chuông thông báo nhảy số ngay lập tức.

### 4.4 Email

**Xác nhận email khi đăng ký (giống `MustVerifyEmail` của Laravel):**

1. Đăng ký xong, khách vẫn được đăng nhập và chuyển tới trang *Kiểm tra hộp thư*.
2. Hệ thống gửi link `http://localhost:4200/verify-email?token=...`, hiệu lực 24 giờ (`app.auth.email-verify-hours`). Mỗi link chỉ dùng 1 lần; CSDL chỉ lưu mã băm SHA-256 của token.
3. Chưa xác nhận: vẫn xem hàng, thêm giỏ được nhưng **không đặt hàng, không viết đánh giá** (API trả `403 EMAIL_NOT_VERIFIED`); có dải nhắc dưới header và nút *Gửi lại email* (tối đa 1 lần/60 giây).
4. Đổi email ở trang Hồ sơ → phải xác nhận lại email mới. Tài khoản do quản trị tạo được coi là đã xác nhận.
5. **Chưa cấu hình SMTP (`app.mail.enabled=false`) vẫn demo được:** link xác nhận (và link đặt lại mật khẩu) được in ra console của backend, dòng `[MAIL tắt] Link xác nhận email cho ...` → copy dán vào trình duyệt.

**Cấu hình gửi mail:**
- **Đang phát triển – Mailtrap:** tạo inbox tại <https://mailtrap.io> (Email Testing → Inboxes → SMTP Settings), điền `spring.mail.username/password`, đặt `app.mail.enabled=true`. Email xác nhận đơn, đổi trạng thái, quên mật khẩu sẽ vào hộp thư Mailtrap.
- **Khi nghiệm thu – gửi thật (ví dụ Gmail):**
  ```properties
  app.mail.enabled=true
  app.mail.from=ToyLand <email-cua-ban@gmail.com>
  spring.mail.host=smtp.gmail.com
  spring.mail.port=587
  spring.mail.username=email-cua-ban@gmail.com
  spring.mail.password=<App Password 16 ký tự>   # Google Account → Security → 2-Step Verification → App passwords
  ```

### 4.5 Giới hạn tần suất (chống dò mật khẩu, spam)
- Không cần cấu hình. Các API công khai dễ bị lạm dụng bị giới hạn **theo địa chỉ IP**; vượt ngưỡng → `429 TOO_MANY_REQUESTS` kèm header `Retry-After` và thông báo tiếng Việt.

| API | Ngưỡng mặc định |
|---|---|
| Đăng nhập | 20 lần / 1 phút |
| Đăng ký | 5 lần / 10 phút |
| Quên mật khẩu | 5 lần / 15 phút |
| Đặt lại mật khẩu | 10 lần / 15 phút |
| Xác nhận email | 20 lần / 10 phút |
| Làm mới token | 30 lần / 1 phút |
| Tra cứu đơn không đăng nhập | 20 lần / 1 phút |

- Đổi ngưỡng bằng biến môi trường (không cần sửa `application.properties`), ví dụ `APP_RATE_LIMIT_LOGIN_MAX=50`, `APP_RATE_LIMIT_LOGIN_WINDOW_SECONDS=60`; tắt hẳn: `APP_RATE_LIMIT_ENABLED=false`.
- Bổ sung cho cơ chế khoá **tài khoản** 15 phút sau 5 lần sai mật khẩu: khoá tài khoản chặn đoán mật khẩu một người, giới hạn IP chặn một máy thử trên nhiều tài khoản.

## 5. Chức năng chính

**Khách hàng:** trang chủ (banner, danh mục, mua theo độ tuổi, đang giảm giá có đếm ngược, nổi bật, bán chạy, nhân vật, thương hiệu, đã xem gần đây) · tìm kiếm gợi ý tức thì (không dấu) · lọc nhiều chiều có đếm số lượng (danh mục, độ tuổi, giá, thương hiệu, nhân vật, giới tính, còn hàng, giảm giá) + sắp xếp · chi tiết sản phẩm (thư viện ảnh, chọn biến thể 2 thuộc tính, tồn kho theo biến thể, thông tin an toàn/cảnh báo chi tiết nhỏ, thông số, đánh giá có ảnh + lọc theo sao) · yêu thích · giỏ hàng cho cả khách vãng lai (tự gộp khi đăng nhập) · thanh toán (sổ địa chỉ GHN, voucher, gói quà + thiệp, COD/ví MoMo, phí ship GHN) · đơn mua theo trạng thái, huỷ đơn có lý do, thanh toán lại, trả hàng kèm ảnh, đánh giá sản phẩm đã mua, mua lại · tra cứu đơn không cần đăng nhập · xác nhận email bằng link (chặn đặt hàng/đánh giá khi chưa xác nhận) · quên/đặt lại mật khẩu · đổi mật khẩu · xoá tài khoản (NĐ 13/2023).

**Điểm thưởng (khách hàng thân thiết):** mỗi 10.000đ tiền hàng = 1 điểm, cộng khi đơn **hoàn tất** · hạng Thành viên → Bạc (500) → Vàng (1.500) → Kim cương (4.000) với hệ số x1.0 / x1.1 / x1.25 / x1.5 · dùng điểm ở bước thanh toán (1 điểm = 100đ, tối đa 50% giá trị hàng) · huỷ/trả hàng tự hoàn điểm · trang *Điểm thưởng* xem số dư, hạng, lịch sử. Tham số ở `app.loyalty.*`.

**Quản trị (ADMIN/STAFF):** dashboard doanh thu theo ngày, trạng thái đơn, doanh thu theo danh mục, phương thức thanh toán, top sản phẩm, cảnh báo tồn thấp · quản lý đơn (lọc, xử lý theo quy trình, cập nhật hàng loạt, ghi chú nội bộ, in phiếu) · duyệt trả hàng/hoàn tiền · sản phẩm (trình soạn nhiều tab, sinh tổ hợp biến thể tự động, ảnh, SEO) · kho (tồn theo biến thể, điều chỉnh kiểm kê, phiếu nhập, lịch sử xuất/nhập) · **thanh toán & đối soát** (sổ thu – chi MoMo/COD/tiền mặt/chuyển khoản, hoàn tiền tự động qua API MoMo, biểu đồ dòng tiền, hàng đợi *Chờ hoàn tiền* và *COD chưa thu*, ghi nhận đã thu / đã hoàn tiền nhiều đợt, xuất Excel) · **nhà cung cấp** (CRUD, gắn vào phiếu nhập, thống kê số phiếu/tổng tiền nhập) · đánh giá (ẩn/hiện, phản hồi, **tự gắn cờ đánh giá chứa từ khoá cấm** – không dấu, khớp nguyên từ – chờ duyệt) · chuông thông báo realtime · **chỉ ADMIN:** danh mục, thương hiệu & nhân vật, voucher, chương trình khuyến mãi, banner, người dùng & **điều chỉnh điểm thưởng**, danh sách từ khoá cấm, **báo cáo & xuất Excel** (doanh thu – giá vốn – lợi nhuận theo ngày/tháng, theo sản phẩm, tồn kho & giá trị tồn, khách hàng mua nhiều), **nhật ký hoạt động** (ai – làm gì – lúc nào – IP).

## 6. Kiểm thử

**Postman / Newman** – collection `postman/ToyLand-v2.postman_collection.json` (11 thư mục, 171 request). Mỗi request có sẵn test: mã trạng thái mong đợi, định dạng JSON, thông báo lỗi tiếng Việt, và test nghiệp vụ (tổng tiền báo giá, điểm không vượt số dư, phân quyền 401/403, file Excel trả về đúng định dạng…). Các request tự lưu biến (`customerToken`, `adminToken`, `staffToken`, `orderId`, `addressId`, `supplierId`…) cho request sau.

- Trong Postman: chuột phải collection → **Run collection** (chạy theo thứ tự thư mục).
- Dòng lệnh (xuất báo cáo HTML để minh chứng):
  ```bash
  npm install -g newman newman-reporter-htmlextra
  newman run postman/ToyLand-v2.postman_collection.json -r cli,htmlextra --reporter-htmlextra-export postman/ket-qua-kiem-thu.html
  ```
  Chạy trên CSDL vừa tạo từ `toyland_v2.sql`; một số request chấp nhận nhiều kết quả (vd đăng ký lần 2 → 409) để chạy lại được nhiều lần.

**JUnit 5** – `src/test/java/com/toyland`: `PureLogicTest` (chuẩn hoá tiếng Việt, làm tròn tiền, quy trình trạng thái đơn, chữ ký MoMo HMAC-SHA256) và `NewFeaturesLogicTest` (hạng thành viên, tích/dùng điểm, lọc từ khoá cấm, định dạng tiền, token xác nhận email, mẫu email hợp lệ, kỳ đối soát). Chạy: Eclipse → chuột phải thư mục `src/test/java` → *Run As → JUnit Test*, hoặc `mvn test`.

## 7. Chạy bằng Docker (triển khai)

Yêu cầu Docker Desktop. Tại thư mục gốc dự án:

```bash
docker compose up -d --build     # lần đầu mất vài phút (tải Maven/npm)
```

| Dịch vụ | Địa chỉ |
|---|---|
| Web cửa hàng + quản trị (Nginx) | <http://localhost:4200> |
| REST API + Swagger | <http://localhost:8080/swagger-ui.html> |
| MySQL | `localhost:3307` – `root` / `toyland123` (tránh trùng MySQL cài sẵn cổng 3306) |

- Lần đầu MySQL tự chạy `database/toyland_v2.sql`. Tạo lại CSDL từ đầu: `docker compose down -v` rồi `up` lại.
- Cấu hình MoMo/GHN/Email bằng biến môi trường trong `docker-compose.yml` (vd `APP_MOMO_SECRET_KEY`, `APP_MAIL_ENABLED=true`) – Spring Boot tự ánh xạ sang `app.momo.secret-key`…
- Hướng dẫn đóng gói & triển khai đầy đủ (WAR/Tomcat, Nginx/Apache, Docker, ngrok, checklist nghiệm thu): [`docs/TRIEN-KHAI.md`](docs/TRIEN-KHAI.md).
- Ảnh upload lưu trong volume `uploads` nên không mất khi build lại.

## 8. Cấu trúc mã nguồn

```
backend/toyland-api/src/main/java/com/toyland
├── auth          JWT, refresh token, đăng ký/đăng nhập, quên mật khẩu, SecurityConfig
├── user          hồ sơ, sổ địa chỉ, quản lý người dùng
├── catalog       danh mục, thương hiệu, nhân vật, sản phẩm, biến thể, tìm kiếm & bộ lọc
├── wishlist      yêu thích
├── cart          giỏ hàng (tài khoản + khách vãng lai)
├── promotion     voucher, chương trình KM, banner, tính giá
├── order         checkout, đơn hàng, quy trình trạng thái, trả hàng, job tự động
├── payment       ví MoMo (tạo giao dịch, redirect, IPN, hoàn tiền), sổ thu – chi, đối soát
├── shipping      GHN
├── inventory     tồn kho, phiếu nhập, lịch sử kho
├── review        đánh giá, kiểm duyệt từ khoá cấm
├── supplier      nhà cung cấp
├── loyalty       điểm thưởng, hạng thành viên
├── report        dashboard, báo cáo doanh thu/lợi nhuận, xuất Excel (Apache POI)
├── notification  email + thông báo realtime (WebSocket STOMP)
├── audit         nhật ký hoạt động
├── media         upload ảnh
├── common        exception, DTO dùng chung, tiện ích
└── config        Swagger, static /uploads, WebSocket

frontend/toyland-web/src/app
├── core      models, API service, auth + interceptor refresh token, guards, stores (giỏ hàng, yêu thích, thông báo realtime)
├── shared    thẻ sản phẩm, hộp thoại địa chỉ/xác nhận/form, upload ảnh, timeline đơn, đếm ngược
├── layout    layout cửa hàng (header, mega menu, giỏ hàng trượt, footer) & layout quản trị
└── pages     shop · auth · account · admin
```
