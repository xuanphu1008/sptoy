# DÀN Ý & HỒ SƠ CHUẨN BỊ BÁO CÁO – TOYLAND

> Căn cứ: *Danh sách đề tài & yêu cầu chung* (mẫu 1) · *Đề cương mẫu* (mẫu 2) · *Báo cáo mẫu – website bán thiết bị năng lượng mặt trời* (mẫu 3).
> Nguyên tắc: **giữ đúng bố cục + văn phong của mẫu 3**, **bổ sung đủ các mục mẫu 1 yêu cầu mà mẫu 3 thiếu**, **không lặp lại lỗi của mẫu 3**.
> Trạng thái chức năng: đã chốt (xem `docs/TONG-KET-CHUC-NANG.md`). Bán hàng tại quầy (POS) → *Hướng phát triển*.

---

## 0. Thông tin bìa (cần nhóm xác nhận)

| Mục | Nội dung đề xuất |
|---|---|
| Trường / Khoa | TRƯỜNG ĐẠI HỌC TÀI NGUYÊN VÀ MÔI TRƯỜNG HÀ NỘI · KHOA CÔNG NGHỆ THÔNG TIN (logo lấy lại từ file mẫu) |
| Tên đề tài | **XÂY DỰNG VÀ TRIỂN KHAI ỨNG DỤNG QUẢN LÝ CỬA HÀNG ĐỒ CHƠI TRỰC TUYẾN TOYLAND SỬ DỤNG SPRING BOOT, ANGULAR VÀ MYSQL** (cụ thể hoá gợi ý số 3 "Quản lý bán hàng với Spring Boot, Angular và MySQL") |
| Nhóm | Nguyễn Xuân Phú · Hoàng Linh · Trần Thị Mai · Đoàn Thi Mai Duyên · Đinh Thị Thùy Linh |
| Lớp | ĐH13C3 |
| Giảng viên | ThS. Nguyễn Văn Hách |
| Nơi, thời gian | HÀ NỘI, …/2026 |

⚠️ **Cần hỏi giảng viên:** yêu cầu chung ghi "làm nhóm 2–3 người", nhóm đang có **5 người**.

## 1. Định dạng (lấy theo file mẫu 3)

- Khổ A4 · lề trái 3 cm, phải 2,25 cm, trên 1,87 cm, dưới 2,3 cm · Times New Roman · nội dung cỡ 14, giãn dòng 1,3–1,5.
- Heading 1 = CHƯƠNG, Heading 2 = 1.1, Heading 3 = 1.1.1 · chú thích hình/bảng dùng style *Caption* để sinh **Danh mục hình ảnh, bảng biểu** tự động.
- Dựng file Word **từ chính file mẫu** (giữ nguyên styles, bìa, khung), chỉ thay nội dung → hình thức giống mẫu 100%.

## 2. Lỗi của báo cáo mẫu cần tránh

| # | Lỗi trong mẫu 3 | Cách làm của ToyLand |
|---|---|---|
| 1 | Trùng số hình (*Hình 2.22* hai lần, *Hình 3.12* hai lần, "Hình 2. 2" chen giữa chương) | Đánh số tự động bằng Caption + field |
| 2 | Bảng use case ghi sai tác nhân (Quản lý đơn hàng / khuyến mãi / thống kê ghi "Khách vãng lai") | Tác nhân khớp đúng phân quyền thực tế (ADMIN / STAFF / CUSTOMER) |
| 3 | Mô tả công nghệ không có trong đề (TailwindCSS, Shadcn/UI, IntelliJ) | Chỉ ghi đúng thứ đã dùng: Angular Material 3, Eclipse, VS Code, Postman… |
| 4 | Trích dẫn trống ("…tới ." không có số), tài liệu tham khảo của đề tài *Quản lý sinh viên* không liên quan, nhiều nguồn khó kiểm chứng | Chỉ dùng nguồn **có thật, tra được** (tài liệu chính thức Spring/Angular/RFC 7519, Fielding 2000, báo cáo TMĐT Việt Nam của Bộ Công Thương…), trích dẫn đủ số |
| 5 | Bảng test case chỉ ghi "Pass/Fail", không có kết quả thật | Ghi kết quả chạy thật: Newman (số request, số assertion đạt, thời gian phản hồi TB), JUnit |
| 6 | Thiếu Tóm tắt, phân công, wireframe, code minh hoạ, JSON/XML; Phụ lục bỏ trống | Có đủ (mục 3 bên dưới) |
| 7 | Lỗi chính tả ("giỏ hang", "chức tìm kiếm", "khách hang") | Soát chính tả trước khi nộp |

## 3. Đối chiếu yêu cầu bàn giao (mẫu 1) → vị trí trong báo cáo

| Yêu cầu | Vị trí |
|---|---|
| Trang bìa | Bìa |
| **Tóm tắt nội dung (1 trang)** – mẫu 3 thiếu | Sau bìa |
| Giới thiệu đề tài & lý do chọn | Lời mở đầu + 1.1 |
| Cơ sở lý thuyết: dịch vụ web, REST, **JSON/XML**, Spring, Angular | 1.2 – 1.5 |
| Phân tích: yêu cầu, sơ đồ kiến trúc, ERD, use case | 2.1 – 2.4 |
| Thiết kế: mô hình dữ liệu, API endpoints, **UI/UX (wireframe)** | 2.3, 2.5, **2.6** |
| Triển khai: công nghệ, **đoạn code minh hoạ**, ảnh chụp màn hình | 3.1 – 3.3, 3.4.1 |
| Kiểm thử & đánh giá: Postman, giao diện | 3.4.2 – 3.4.3 |
| Kết luận & hướng phát triển | Kết luận |
| **Phân công công việc** – mẫu 3 thiếu | Mục riêng trước Tài liệu tham khảo |
| Tài liệu tham khảo chuẩn học thuật | Tài liệu tham khảo |

---

## 4. Dàn ý chi tiết

### Bìa · Tóm tắt · Mục lục · Danh mục hình ảnh, bảng biểu · Danh mục từ viết tắt
- **Tóm tắt (1 trang):** bài toán cửa hàng đồ chơi → giải pháp SOA (Spring Boot REST + Angular + MySQL) → kết quả định lượng: 33 bảng, 149 API, 41 màn hình, 4 vai trò, tích hợp 3 dịch vụ ngoài (VNPay, GHN, SMTP) + WebSocket; 170 request Postman + 14 unit test → từ khoá.
- **Từ viết tắt:** SOA, REST, API, JSON, XML, JWT, JPA, ORM, DTO, SPA, CRUD, CORS, STOMP, COD, IPN, HMAC, SKU, UC, ERD.

### LỜI MỞ ĐẦU (văn phong như mẫu 3: bối cảnh → hạn chế → SOA → Spring Boot + Angular → lý do chọn đề tài)
- Bối cảnh TMĐT Việt Nam và thị trường đồ chơi trẻ em (số liệu **tra nguồn thật** khi viết).
- Hạn chế của cửa hàng đồ chơi vừa và nhỏ: quản lý tồn kho theo mẫu mã/màu/size thủ công, không đối soát được tiền COD/VNPay, thiếu thông tin an toàn đồ chơi theo độ tuổi (QCVN 3:2019/BKHCN).
- Vì sao SOA + Spring Boot + Angular; lý do chọn đề tài ToyLand.

### CHƯƠNG 1. CƠ SỞ LÝ LUẬN VÀ TỔNG QUAN CÔNG NGHỆ
| Mục | Nội dung | Hình / bảng |
|---|---|---|
| 1.1 Đặt vấn đề & tính cấp thiết | Thực trạng, mục tiêu tổng quát và cụ thể (như đề cương mẫu mục 2) | – |
| 1.2 SOA và RESTful API | SOA, web service (SOAP vs REST), 6 ràng buộc REST, HTTP method & mã trạng thái, **định dạng JSON và XML (so sánh, ví dụ cùng 1 sản phẩm)** | H1.1 Mô hình SOA · H1.2 Mô hình REST · B1.1 HTTP method · B1.2 JSON vs XML |
| 1.3 Spring Boot | Auto-configuration, starter, Spring MVC, Spring Data JPA, Spring Security, validation, WebSocket | H1.3 Kiến trúc Spring Boot |
| 1.4 Angular | Component, standalone, signals, DI, Router, HttpClient/interceptor, RxJS, Angular Material 3 | H1.4 Kiến trúc Angular |
| 1.5 Công nghệ bổ trợ | MySQL/MariaDB, Hibernate, JWT (RFC 7519), BCrypt, Maven, Node.js/npm, WebSocket-STOMP, Apache POI, VNPay, GHN, SMTP, Docker | H1.5 Cấu trúc JWT · B1.3 Tổng hợp công nghệ + phiên bản |
| 1.6 Kết luận chương 1 | | |

### CHƯƠNG 2. PHÂN TÍCH VÀ THIẾT KẾ HỆ THỐNG
**2.1 Phân tích yêu cầu nghiệp vụ** – theo 4 tác nhân + yêu cầu phi chức năng (bảo mật, hiệu năng, mở rộng, pháp lý: Luật BVDLCN, QCVN 3:2019).
- B2.1 Danh sách yêu cầu chức năng theo tác nhân · B2.2 Yêu cầu phi chức năng.

**2.2 Phân tích và thiết kế Use Case**
- 2.2.1 Sơ đồ use case: H2.1 tổng quát (Khách vãng lai, Khách hàng, Nhân viên, Quản trị viên + hệ thống ngoài VNPay, GHN, Email) và các UC chi tiết, **mỗi UC 1 hình + 1 bảng mô tả** (tên, tác nhân, mô tả, tiền điều kiện, luồng chính, luồng thay thế, hậu điều kiện):

| UC | Tên | Tác nhân |
|---|---|---|
| UC01 | Đăng ký & xác nhận email (extend: gửi lại link) | Khách vãng lai |
| UC02 | Đăng nhập / đăng xuất (extend: quên mật khẩu; khoá 5 lần sai) | Mọi tài khoản |
| UC03 | Tìm kiếm & lọc sản phẩm | Khách vãng lai, Khách hàng |
| UC04 | Quản lý giỏ hàng | Khách vãng lai, Khách hàng |
| UC05 | Đặt hàng & thanh toán (include: áp voucher, dùng điểm; extend: VNPay) | Khách hàng, VNPay, GHN |
| UC06 | Quản lý đơn của tôi (huỷ, trả hàng, mua lại) | Khách hàng |
| UC07 | Đánh giá sản phẩm | Khách hàng |
| UC08 | Quản lý sản phẩm & biến thể | Nhân viên, Quản trị viên |
| UC09 | Xử lý đơn hàng & trả hàng | Nhân viên, Quản trị viên |
| UC10 | Quản lý kho, phiếu nhập & nhà cung cấp | Nhân viên, Quản trị viên |
| UC11 | Thanh toán & đối soát (ghi nhận thu, hoàn tiền) | Nhân viên, Quản trị viên |
| UC12 | Quản lý khuyến mãi (voucher, chương trình, banner) | Quản trị viên |
| UC13 | Kiểm duyệt đánh giá & từ khoá cấm | Nhân viên (duyệt), Quản trị viên |
| UC14 | Báo cáo thống kê & xuất Excel | Quản trị viên |
| UC15 | Quản lý người dùng & điểm thưởng | Quản trị viên |

- 2.2.2 Biểu đồ tuần tự (H2.x): đăng nhập JWT + refresh token · đăng ký + xác nhận email · tìm kiếm sản phẩm · thêm vào giỏ · đặt hàng COD · thanh toán VNPay (return + IPN, kiểm chữ ký HMAC-SHA512) · chuyển trạng thái đơn + thông báo realtime WebSocket · ghi nhận hoàn tiền.
- 2.2.3 Biểu đồ hoạt động (H2.x): đăng nhập (khoá tạm) · đặt hàng · duyệt trả hàng · thêm sản phẩm & sinh biến thể · kiểm duyệt đánh giá · nhập kho.
- H2.x **Sơ đồ trạng thái đơn hàng** (10 trạng thái) và **trạng thái thanh toán** (UNPAID → PAID → REFUND_PENDING → REFUNDED).

**2.3 Thiết kế cơ sở dữ liệu**
- 2.3.1 ERD: H2.x tổng thể + tách theo phân hệ (Tài khoản · Danh mục–Sản phẩm · Đơn hàng–Thanh toán · Khuyến mãi–Hậu mãi · Kho).
- 2.3.2 Thiết kế bảng: B2.x mô tả các bảng chính (tên cột, kiểu, ràng buộc, ý nghĩa): users, products, product_variants, orders, order_items, payment_transactions, vouchers, stock_movements, reviews, point_transactions… + bảng tổng hợp 33 bảng.
- Điểm thiết kế: biến thể 2 thuộc tính, snapshot đơn hàng, optimistic lock `@Version`, sổ kho, sổ thu – chi, điểm thưởng nguyên tử, token lưu SHA-256.

**2.4 Thiết kế kiến trúc tổng thể**
- 2.4.1 Kiến trúc Backend: H2.x biểu đồ khối (Controller → Service → Repository → Entity, DTO, Security filter chain, WebSocket broker, tích hợp ngoài), 18 package theo miền nghiệp vụ.
- 2.4.2 Kiến trúc Frontend: H2.x (core / shared / layout / pages, lazy routes, guards, interceptor refresh token, signals store).
- 2.4.3 Cơ chế giao tiếp: REST JSON + JWT Bearer, refresh token rotation, WebSocket STOMP, CORS, mã lỗi thống nhất `{status, code, message, details}`.

**2.5 Thiết kế các dịch vụ (API Endpoints)** – nhóm như mẫu 3 nhưng đầy đủ: Xác thực · Cửa hàng (public) · Tài khoản · Giỏ hàng · Đặt hàng · Thanh toán VNPay · Điểm thưởng & thông báo · Quản trị (sản phẩm, đơn, trả hàng, kho, nhà cung cấp, **thanh toán & đối soát**, khuyến mãi, đánh giá & từ khoá cấm, báo cáo, người dùng, nhật ký). B2.x mỗi nhóm; ví dụ request/response JSON cho 2–3 API tiêu biểu. Danh sách đầy đủ 149 API → Phụ lục.

**2.6 Thiết kế giao diện (wireframe)** – *mẫu 3 thiếu, mẫu 1 yêu cầu*: wireframe khung trang chủ, danh sách sản phẩm, chi tiết, thanh toán, dashboard quản trị; bảng màu, font (Baloo 2 + Nunito), nguyên tắc Material Design 3, responsive, chế độ tối.

**2.7 Kết luận chương 2**

### CHƯƠNG 3. CÀI ĐẶT, THỬ NGHIỆM VÀ ĐÁNH GIÁ
**3.1 Môi trường và công cụ** – B3.1: JDK 17/21, Spring Boot 3.5.5, Eclipse (Spring Tools), Angular 19 + Node 20, VS Code, MySQL 8 / MariaDB (XAMPP), Postman/Newman, Chrome, Git, Docker.

**3.2 Triển khai Backend** – cấu trúc thư mục + **code minh hoạ** (ngắn, có chú thích):
1. `SecurityConfig` – phân quyền theo vai trò · 2. `JwtAuthFilter` · 3. `CheckoutService.place` – trừ kho, khoá lạc quan, voucher, điểm · 4. `VnPayService.handleCallback` – kiểm chữ ký, idempotent · 5. `OrderWorkflow.transition` – máy trạng thái · 6. `KeywordModeration` – lọc từ cấm không dấu · 7. `NotificationService` – đẩy WebSocket sau commit · 8. `AdminPaymentService.refund` – đối soát hoàn tiền.

**3.3 Triển khai Frontend** – cấu trúc thư mục + code minh hoạ: `authInterceptor` (tự refresh token), `CartStore` (signals, gộp giỏ khách), `NotificationStore` (STOMP), trang chi tiết sản phẩm chọn biến thể.

**3.4 Kết quả thực nghiệm và đánh giá**
- 3.4.1 Giao diện người dùng (ảnh chụp): *khách hàng* – trang chủ, danh sách & bộ lọc, chi tiết, giỏ hàng trượt, thanh toán, VNPay sandbox, đặt hàng thành công, đơn của tôi (timeline), điểm thưởng, đăng ký & xác nhận email (kèm ảnh email nhận được); *quản trị* – dashboard, đơn hàng, chi tiết đơn, sản phẩm (trình soạn nhiều tab), kho & phiếu nhập, nhà cung cấp, thanh toán & đối soát, báo cáo + file Excel, kiểm duyệt đánh giá, nhật ký, thông báo realtime; chế độ tối, mobile.
- 3.4.2 Kiểm thử chức năng và API: mục đích → phương pháp (như mẫu 3) → **B3.x Test case giao diện (có kết quả thực tế)** → **B3.x Test case API** trích từ Postman → **kết quả Newman** (tổng request, assertion đạt/lỗi, thời gian phản hồi trung bình) → **JUnit 14 test** → kiểm thử bảo mật (401/403, khoá tài khoản, token giả, chữ ký VNPay giả).
- 3.4.3 Đánh giá ưu điểm, nhược điểm, hướng phát triển.

**3.5 Kết luận chương 3**

### KẾT LUẬN VÀ HƯỚNG PHÁT TRIỂN
Kết quả đạt được (đối chiếu mục tiêu) · hạn chế (chưa deploy cloud, chưa đo tải chuyên sâu, VNPay mới ở sandbox) · hướng phát triển: bán tại quầy (POS) + hoá đơn điện tử, ví MoMo/ZaloPay, triển khai cloud, gợi ý sản phẩm theo độ tuổi, ứng dụng di động, tách microservice.

### PHÂN CÔNG CÔNG VIỆC (đề xuất theo module – nhóm xác nhận lại)
| Thành viên | Phụ trách chính | Phần báo cáo |
|---|---|---|
| Nguyễn Xuân Phú (nhóm trưởng) | Kiến trúc, CSDL, bảo mật JWT/xác nhận email, tích hợp VNPay – GHN – email, WebSocket, Docker | Ch.2 (2.3, 2.4), tổng hợp |
| Hoàng Linh | Sản phẩm – biến thể, danh mục/thương hiệu/nhân vật, kho, nhà cung cấp | Ch.2 (2.5), 3.2 |
| Trần Thị Mai | Giỏ hàng, đặt hàng, thanh toán & đối soát, trả hàng, điểm thưởng | Ch.2 (2.2 use case, tuần tự) |
| Đoàn Thi Mai Duyên | Giao diện Angular cửa hàng + quản trị, Material Design, responsive | Ch.1, 2.6, 3.3, 3.4.1 |
| Đinh Thị Thùy Linh | Khuyến mãi, đánh giá & kiểm duyệt, báo cáo Excel, nhật ký; kiểm thử Postman/JUnit | 3.4.2, 3.4.3, Kết luận |

### TÀI LIỆU THAM KHẢO (chỉ nguồn có thật – hoàn thiện khi viết)
Fielding (2000) luận án REST · Papazoglou & van den Heuvel (2007) SOA · RFC 7519 (JWT) · RFC 8259 (JSON) · W3C XML 1.0 · Spring Boot / Spring Security / Spring Data JPA Reference · Angular & Angular Material docs · Hibernate ORM 6.6 docs · VNPay sandbox API docs · GHN API docs · STOMP 1.2 spec · Apache POI docs · Luật BVDLCN 2025 · QCVN 3:2019/BKHCN · Báo cáo TMĐT Việt Nam (Bộ Công Thương / iDEA) · sách: Walls – *Spring in Action*; Fowler – *PoEAA*; Newman – *Building Microservices*.

### PHỤ LỤC
A. Hướng dẫn cài đặt & chạy (Eclipse, VS Code, Docker) · B. Hướng dẫn sử dụng cho khách hàng / nhân viên / quản trị · C. Danh sách đầy đủ API · D. Tài khoản & dữ liệu mẫu · E. Trích kết quả Newman.

---

## 5. Danh mục hình & bảng – nguồn và ai làm

| Nhóm | Số lượng (ước tính) | Ai làm |
|---|---|---|
| Hình lý thuyết ch.1 | 5 | Claude vẽ lại (không lấy ảnh mạng có bản quyền) |
| Use case (1 tổng quát + 15 chi tiết) + 15 bảng mô tả | 31 | Claude |
| Biểu đồ tuần tự 8 + hoạt động 6 + trạng thái 2 | 16 | Claude |
| ERD tổng + 5 phân hệ, kiến trúc BE/FE/giao tiếp | 9 | Claude |
| Wireframe | 5 | Claude |
| Ảnh giao diện | ~25 | Claude chụp bằng dữ liệu mẫu; **nhóm chụp các ảnh "bằng chứng thật"** ở mục 6 |
| Bảng test case, kết quả Newman/JUnit | 4–6 | Claude soạn, **số liệu lấy từ lần chạy của nhóm** |

## 6. Nhóm cần cung cấp trước khi viết bản hoàn chỉnh

1. Xác nhận: tên đề tài, tháng/năm ghi trên bìa, số thành viên (2–3 hay 5), bảng phân công ở trên.
2. Chạy trên máy nhóm và gửi lại:
   - `newman run postman/ToyLand-v2.postman_collection.json -r cli,htmlextra --reporter-htmlextra-export postman/ket-qua-kiem-thu.html` → gửi file HTML (hoặc ảnh chụp phần tổng kết).
   - Eclipse → chuột phải `src/test/java` → *Run As → JUnit Test* → ảnh cửa sổ kết quả.
   - Ảnh: trang thanh toán VNPay sandbox, hộp thư nhận email xác nhận đăng ký + email xác nhận đơn, Swagger UI.
3. (Tuỳ chọn) MSSV từng người nếu khoa yêu cầu ghi trên bìa.
