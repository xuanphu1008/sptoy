# ToyLand – Tổng kết chức năng & đối chiếu 3 mẫu (28/09/2026)

Căn cứ: (1) *Danh sách đề tài & yêu cầu chung* · (2) *Đề cương mẫu* (Quản lý sinh viên) · (3) *Báo cáo mẫu* (Website bán thiết bị năng lượng mặt trời).

## 1. Quy mô hiện tại

| Hạng mục | Số lượng |
|---|---|
| Bảng CSDL (MySQL/MariaDB) | 33 |
| REST endpoint (`/api/v1`) | 149 |
| Lớp Java backend | 170 |
| Màn hình Angular | 41 trang + 12 thành phần dùng chung |
| Request Postman có test tự động | 170 (11 thư mục) |
| Unit test JUnit 5 | 14 |
| Dữ liệu mẫu | 101 sản phẩm / 176 biến thể, 90 đơn, 11 tài khoản |

## 2. Đối chiếu yêu cầu chung (mẫu 1)

| Yêu cầu | ToyLand |
|---|---|
| RESTful Web Service bằng Spring Boot | ✅ Spring Boot 3.5.5, `/api/v1`, DTO record, mã lỗi thống nhất, Swagger |
| CSDL quan hệ MySQL | ✅ 33 bảng, khoá ngoại, CHECK, script tạo mới + script nâng cấp |
| Client Angular gọi API | ✅ Angular 19 standalone + signals, HttpClient, interceptor |
| Nguyên lý REST + Material Design | ✅ Angular Material 3, theme Clay Pastel, sáng/tối, responsive |
| Tối thiểu 1 tính năng bảo mật | ✅ vượt: JWT access + refresh (rotation), BCrypt, khoá 5 lần sai, phân quyền 3 vai trò, xác nhận email, quên mật khẩu, audit log |
| Bàn giao: code server & client | ✅ + Docker Compose, README |

## 3. Đối chiếu chức năng với báo cáo mẫu (mẫu 3)

| Nhóm chức năng trong báo cáo mẫu | Mẫu | ToyLand |
|---|---|---|
| Đăng ký / đăng nhập / đăng xuất / quên mật khẩu | có | ✅ + xác nhận email bằng link, refresh token |
| Quản lý thông tin cá nhân | có | ✅ hồ sơ, sổ địa chỉ, đổi mật khẩu, xoá tài khoản |
| Xem & tìm kiếm sản phẩm | có | ✅ tìm không dấu, gợi ý tức thì, lọc 8 chiều có đếm |
| Giỏ hàng | có | ✅ cả khách vãng lai, tự gộp khi đăng nhập |
| Đặt hàng, theo dõi đơn | có | ✅ timeline trạng thái, huỷ, trả hàng, mua lại, tra cứu không cần đăng nhập |
| Thanh toán | có | ✅ COD + VNPay (chữ ký HMAC-SHA512, IPN, tự huỷ quá 15 phút) |
| Đánh giá sản phẩm | có | ✅ chỉ khách đã mua, ảnh, phân bố sao, admin phản hồi |
| Quản lý sản phẩm | có | ✅ biến thể 2 thuộc tính, ảnh, thông số, SEO |
| Quản lý đơn hàng (nhân viên) | có | ✅ quy trình 10 trạng thái, cập nhật hàng loạt, in phiếu |
| Quản lý nhà cung cấp (nhân viên) | có | ✅ CRUD + gắn phiếu nhập |
| Xử lý thanh toán (nhân viên) | có | ✅ màn hình *Thanh toán & đối soát*: sổ thu – chi, dòng tiền theo ngày, chờ hoàn tiền, COD chưa thu, ghi nhận thu/hoàn, xuất Excel |
| Quản lý khuyến mãi | có | ✅ voucher + chương trình giảm giá theo thời gian + banner |
| Kiểm duyệt đánh giá, từ khoá cấm (admin) | có | ✅ tự gắn cờ, duyệt, quét lại |
| Quản lý doanh thu, thống kê báo cáo | có | ✅ dashboard + báo cáo lợi nhuận/tồn kho/khách hàng, xuất Excel |
| Chức năng vượt mẫu | — | kho & phiếu nhập, điểm thưởng + hạng, thông báo realtime WebSocket, nhật ký hoạt động, GHN phí ship thật, email HTML |

## 4. Đối chiếu mục tiêu đề cương (mẫu 2)

| Mục tiêu | ToyLand |
|---|---|
| Entity – Repository – Service – Controller | ✅ tổ chức theo miền nghiệp vụ (18 package) |
| JWT, phân quyền | ✅ |
| Giao diện Material responsive, routing | ✅ |
| Tài liệu hướng dẫn & API documentation | ✅ README, Swagger UI, Postman · ⚠️ thiếu *hướng dẫn sử dụng* dạng người dùng cuối (đưa vào phụ lục báo cáo) |
| Kiểm thử | ✅ Postman 156 test + JUnit 13 · ⚠️ **chưa chạy Newman trên backend thật để lấy kết quả** |
| Đánh giá hiệu năng | ❌ **chưa có số liệu** |
| Triển khai lên cloud (Heroku/Vercel/AWS) | ⚠️ đã có Docker Compose, **chưa deploy cloud** |

## 5. Việc còn lại trước khi viết báo cáo

### Chức năng
| Mã | Việc | Vì sao |
|---|---|---|
| ~~G1~~ ✅ đã làm (28/09) | **Quản lý thanh toán & đối soát** cho nhân viên: danh sách giao dịch VNPay, lịch sử giao dịch từng đơn, xác nhận đã thu tiền COD, ghi nhận hoàn tiền | Báo cáo mẫu có tác nhân *Nhân viên – Xử lý thanh toán* và nhóm API *Payment Management* |
| G2 | **Deploy cloud**: frontend Vercel/Netlify, backend + MySQL trên Railway/Render/Aiven (cần tài khoản của nhóm) | Đề cương ghi rõ mục tiêu triển khai lên cloud |
| G3 | **Đo hiệu năng** bằng k6 hoặc JMeter: thời gian phản hồi, số request/giây của các API chính | Đề cương: "kiểm thử, đánh giá hiệu năng" |
| G4 | (tuỳ chọn) Trả về XML khi `Accept: application/xml` | Minh hoạ phần lý thuyết JSON/XML |
| — | POS bán tại quầy + hoá đơn | Đã tạm hoãn → ghi vào *Hướng phát triển* |

### Kiểm chứng trên máy nhóm (lấy minh chứng cho chương 3)
1. Chạy Newman cả collection → báo cáo HTML (gửi lại để sửa test lệch nếu có).
2. Run JUnit trong Eclipse → ảnh 13 test pass.
3. Chụp màn hình các trang chính trên dữ liệu thật.

### Nội dung báo cáo sẽ bổ sung (không phải code)
Tóm tắt 1 trang · Use case tổng quát + chi tiết (bảng mô tả) · biểu đồ tuần tự & hoạt động · ERD · wireframe · bảng test case frontend/API · đánh giá ưu/nhược điểm · phân công 5 thành viên · tài liệu tham khảo chuẩn học thuật · phụ lục hướng dẫn sử dụng.
