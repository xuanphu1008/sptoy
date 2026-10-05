# ToyLand – Hướng dẫn đóng gói & triển khai

Tài liệu này dành cho người chạy hệ thống ở máy khác hoặc lên máy chủ khi nghiệm thu. Có 3 cách, chọn **một**:

| Cách | Khi nào dùng | Cần cài |
|---|---|---|
| A. Chạy trực tiếp (dev) | Demo trên máy nhóm | XAMPP (MySQL/MariaDB), JDK 17, Eclipse, Node.js 20 |
| B. Đóng gói WAR + web tĩnh | Máy chủ Windows/Linux có Tomcat 10.1 và Nginx/Apache | JDK 17, Tomcat 10.1, Nginx hoặc Apache (XAMPP) |
| C. Docker Compose | Máy có Docker, muốn 1 lệnh chạy cả hệ thống | Docker Desktop |

Phiên bản đã kiểm tra: Spring Boot 3.5.5 (Java 17), Angular 19 (Node 20), MySQL 8 / MariaDB 10.4+.

---

## 0. Chuẩn bị chung

### 0.1 Cơ sở dữ liệu
- **Cài mới:** chạy toàn bộ `database/toyland_v2.sql` → tạo schema `toyland_v2` kèm dữ liệu mẫu (đã có MoMo, nhà cung cấp, điểm thưởng, nhật ký…).
- **Đã có CSDL cũ:** chạy thêm theo thứ tự (chạy lại nhiều lần vẫn an toàn):
  1. `toyland_v2_nang_cap_chuc_nang.sql`
  2. `toyland_v2_xac_nhan_email.sql`
  3. `toyland_v2_thanh_toan.sql`
  4. `toyland_v2_momo.sql` – đổi đơn/giao dịch VNPay cũ sang MoMo
  5. `toyland_v2_bam_token.sql` – mọi người đăng nhập lại 1 lần
- Kiểm tra nhanh: `SELECT payment_method, COUNT(*) FROM toyland_v2.orders GROUP BY payment_method;` chỉ còn `COD` và `MOMO`.

### 0.2 Cấu hình backend – không sửa file, dùng biến môi trường
Mọi khoá trong `application.properties` đều ghi đè được bằng biến môi trường: đổi `.` và `-` thành `_`, viết hoa.
Ví dụ `app.momo.secret-key` → `APP_MOMO_SECRET_KEY`, `spring.datasource.password` → `SPRING_DATASOURCE_PASSWORD`.

| Biến môi trường | Ý nghĩa | Mặc định |
|---|---|---|
| `SPRING_DATASOURCE_URL` | Chuỗi kết nối CSDL | `jdbc:mysql://localhost:3306/toyland_v2…` |
| `SPRING_DATASOURCE_USERNAME` / `_PASSWORD` | Tài khoản CSDL | `root` / (trống – XAMPP) |
| `APP_JWT_SECRET` | Khoá ký JWT (Base64, ≥ 64 byte). **Đổi khi triển khai thật** | giá trị dev |
| `APP_FRONTEND_URL` | Địa chỉ web (dùng trong link email, link quay về sau thanh toán) | `http://localhost:4200` |
| `APP_PUBLIC_BASE_URL` | Địa chỉ API công khai (link ảnh upload, IPN mặc định) | `http://localhost:8080` |
| `APP_CORS_ALLOWED_ORIGINS` | Tên miền web được gọi API (nhiều giá trị cách nhau dấu phẩy) | `http://localhost:4200` |
| `APP_UPLOAD_DIR` | Thư mục lưu ảnh upload | `uploads` |
| `APP_MOMO_PARTNER_CODE` / `_ACCESS_KEY` / `_SECRET_KEY` | Khoá MoMo Business | khoá thử nghiệm công khai của MoMo |
| `APP_MOMO_ENDPOINT` | Cổng MoMo | `https://test-payment.momo.vn` |
| `APP_MOMO_REQUEST_TYPE` | `payWithMethod` (QR/ATM/Visa) hoặc `captureWallet` (QR) | `payWithMethod` |
| `APP_MOMO_REDIRECT_URL` | Trang kết quả thanh toán | `{APP_FRONTEND_URL}/payment/momo-return` |
| `APP_MOMO_IPN_URL` | MoMo gọi server-to-server (phải là địa chỉ công khai) | `{APP_PUBLIC_BASE_URL}/api/v1/payments/momo/ipn` |
| `APP_GHN_TOKEN` / `APP_GHN_SHOP_ID` | GHN (để trống = phí ship cố định) | trống |
| `APP_MAIL_ENABLED`, `SPRING_MAIL_HOST`, `SPRING_MAIL_USERNAME`, `SPRING_MAIL_PASSWORD` | Gửi email thật | tắt (in link ra console) |
| `APP_RATE_LIMIT_ENABLED` | Giới hạn tần suất API công khai | `true` |

### 0.3 Cấu hình frontend
Địa chỉ API nằm ở `frontend/toyland-web/src/environments/environment.ts` (`apiUrl`). Đổi **trước khi build** nếu API không ở `http://localhost:8080/api/v1`.

---

## A. Chạy trực tiếp (máy phát triển)

1. XAMPP → Start **MySQL** → phpMyAdmin → *Import* `database/toyland_v2.sql`.
2. Eclipse → *File → Import → Existing Maven Projects* → chọn `backend/toyland-api` → chờ tải thư viện → chuột phải `ToylandApiApplication` → *Run As → Java Application*. Thấy dòng `Started ToylandApiApplication` và `MoMo: bat (...)` là xong.
3. VS Code / terminal tại `frontend/toyland-web`:
   ```bash
   npm install
   npm start          # http://localhost:4200
   ```
4. Mở <http://localhost:8080/swagger-ui.html> để xem toàn bộ API.

---

## B. Đóng gói WAR + web tĩnh (máy chủ)

### B.1 Đóng gói backend
- **Eclipse:** chuột phải dự án `toyland-api` → *Run As → Maven build…* → Goals: `clean package` (thêm `-DskipTests` nếu muốn bỏ kiểm thử) → *Run*.
- **Dòng lệnh:** `cd backend/toyland-api` rồi `mvn clean package` (hoặc `mvnw clean package`).
- Kết quả: `backend/toyland-api/target/toyland-api-2.0.0.war`. File WAR chạy được theo 2 kiểu:

**Kiểu 1 – chạy độc lập (khuyên dùng, Tomcat nhúng sẵn):**
```bash
# Windows (cmd)
set SPRING_DATASOURCE_PASSWORD=matkhau
set APP_JWT_SECRET=<chuoi-base64-dai>
set APP_FRONTEND_URL=http://<ip-may-chu>
set APP_PUBLIC_BASE_URL=http://<ip-may-chu>:8080
set APP_CORS_ALLOWED_ORIGINS=http://<ip-may-chu>
java -jar toyland-api-2.0.0.war
```
Linux: thay `set` bằng `export`. Muốn chạy nền như dịch vụ: dùng `nohup java -jar ... &` hoặc tạo service systemd.

**Kiểu 2 – thả vào Tomcat 10.1 có sẵn** (cần Tomcat 10.1 vì Spring Boot 3 dùng Jakarta EE 10):
1. Đổi tên file thành `ROOT.war` để API nằm ở gốc (`http://<may>:8080/api/v1/...`). Nếu giữ tên `toyland-api-2.0.0.war` thì API thành `/toyland-api-2.0.0/api/v1/...` và phải sửa `apiUrl` của frontend tương ứng.
2. Chép vào `<TOMCAT>/webapps/`.
3. Đặt biến môi trường trong `<TOMCAT>/bin/setenv.bat` (Windows) hoặc `setenv.sh` (Linux):
   ```bat
   set "SPRING_DATASOURCE_PASSWORD=matkhau"
   set "APP_JWT_SECRET=<chuoi-base64-dai>"
   set "APP_UPLOAD_DIR=C:\toyland\uploads"
   ```
4. Chạy `bin/startup.bat` → xem `logs/catalina.*.log`.

### B.2 Đóng gói frontend
```bash
cd frontend/toyland-web
npm ci
npx ng build --configuration production
```
Kết quả: thư mục `dist/toyland-web/browser/` (HTML/JS/CSS tĩnh). Lưu ý: máy build cần Internet vì bước build nhúng sẵn font Google (Baloo 2, Nunito).

**Phục vụ bằng Nginx:** chép nội dung `browser/` vào `/usr/share/nginx/html` (Windows: `nginx/html`), dùng cấu hình mẫu `frontend/toyland-web/nginx.conf` (đã có dòng `try_files ... /index.html` để F5 ở trang con không bị 404).

**Phục vụ bằng Apache (XAMPP):** chép nội dung `browser/` vào `C:\xampp\htdocs\toyland\`, tạo file `.htaccess` cùng thư mục:
```apache
RewriteEngine On
RewriteBase /toyland/
RewriteCond %{REQUEST_FILENAME} -f [OR]
RewriteCond %{REQUEST_FILENAME} -d
RewriteRule ^ - [L]
RewriteRule ^ index.html [L]
```
và build với `npx ng build --configuration production --base-href /toyland/`. Khi đó `APP_FRONTEND_URL=http://<may>/toyland`, `APP_CORS_ALLOWED_ORIGINS=http://<may>`.

---

## C. Docker Compose (1 lệnh)

```bash
docker compose up -d --build      # lần đầu mất vài phút
docker compose logs -f api        # xem log backend
docker compose down               # dừng (giữ dữ liệu)
docker compose down -v            # dừng và xoá CSDL + ảnh upload để tạo lại từ đầu
```

| Dịch vụ | Địa chỉ |
|---|---|
| Web (Nginx) | <http://localhost:4200> |
| API + Swagger | <http://localhost:8080/swagger-ui.html> |
| MySQL | `localhost:3307` – `root` / `toyland123` |

- Lần đầu, MySQL tự chạy `database/toyland_v2.sql`.
- Đổi cấu hình: sửa khối `environment:` của dịch vụ `api` trong `docker-compose.yml` rồi `docker compose up -d`.
- Ảnh upload nằm trong volume `uploads`, không mất khi build lại.

---

## D. Thanh toán MoMo khi demo

- Mặc định dùng khoá thử nghiệm công khai của MoMo → không cần đăng ký vẫn demo được. Thẻ ATM thử nghiệm: `9704 0000 0000 0018` · `NGUYEN VAN A` · `03/07` · OTP `OTP`.
- Chạy localhost: MoMo **không gọi IPN** về máy được; trang `/payment/momo-return` gửi kết quả cho backend kiểm tra chữ ký nên đơn vẫn cập nhật đúng.
- Muốn demo IPN (server-to-server):
  1. Tải ngrok, chạy `ngrok http 8080` → nhận địa chỉ dạng `https://abcd.ngrok-free.app`.
  2. Khởi động backend với `APP_MOMO_IPN_URL=https://abcd.ngrok-free.app/api/v1/payments/momo/ipn`.
  3. Thanh toán thử → trong cửa sổ ngrok thấy dòng `POST /api/v1/payments/momo/ipn 204`.
- Máy chủ chặn Internet ra ngoài → MoMo không tạo được giao dịch, khách nhận thông báo "Không kết nối được cổng MoMo… chọn COD".

---

## E. Checklist trước buổi nghiệm thu

- [ ] CSDL đúng phiên bản (mục 0.1), đăng nhập được `admin/admin123`, `staff/staff123`, `khachhang/123456`.
- [ ] Backend log có `MoMo: bat`, Swagger mở được.
- [ ] Đặt 1 đơn COD và 1 đơn MoMo (thẻ ATM thử nghiệm) → admin nhận chuông thông báo realtime.
- [ ] Duyệt trả hàng đơn MoMo → *Thanh toán & đối soát → Chờ hoàn tiền* → *Hoàn về ví MoMo*.
- [ ] Báo cáo → xuất Excel mở được.
- [ ] Chạy Newman, lưu `postman/ket-qua-kiem-thu.html`; chạy JUnit (14 test) chụp màn hình kết quả.
- [ ] Email thật: bật `APP_MAIL_ENABLED=true` + SMTP Gmail (README mục 4.4) nếu giảng viên muốn xem email.
- [ ] Đổi `APP_JWT_SECRET` nếu triển khai ra ngoài máy cá nhân.

## F. Lỗi thường gặp

| Hiện tượng | Nguyên nhân / cách xử lý |
|---|---|
| Backend báo `Communications link failure` | MySQL chưa chạy hoặc sai cổng/mật khẩu (`SPRING_DATASOURCE_*`). |
| Web báo lỗi CORS | `APP_CORS_ALLOWED_ORIGINS` chưa có đúng địa chỉ web (kể cả cổng). |
| F5 ở trang con bị 404 | Thiếu cấu hình `try_files` (Nginx) hoặc `.htaccess` (Apache) ở mục B.2. |
| Đặt hàng MoMo báo lỗi 502 | Máy không ra được Internet tới `test-payment.momo.vn`, hoặc MoMo từ chối (xem log backend dòng `MoMo tu choi`). Thử `APP_MOMO_REQUEST_TYPE=captureWallet`. |
| Lỗi `CONSTRAINT chk_ord_pm failed` | CSDL cũ chưa chạy `toyland_v2_momo.sql`. |
| Đăng nhập liên tục báo 429 | Vượt giới hạn tần suất; chờ thời gian ghi trong thông báo hoặc `APP_RATE_LIMIT_ENABLED=false` khi kiểm thử tải. |
