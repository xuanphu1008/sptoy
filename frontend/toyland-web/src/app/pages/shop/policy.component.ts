import { Component, computed, effect, inject, input } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { Title } from '@angular/platform-browser';
import { RouterLink, RouterLinkActive } from '@angular/router';

interface PolicyPage { title: string; icon: string; sections: { h: string; p: string[] }[]; }

/** Noi dung tinh cac trang chinh sach (tham khao cac shop do choi thuc te) */
const PAGES: Record<string, PolicyPage> = {
  shipping: {
    title: 'Chính sách giao hàng', icon: 'local_shipping',
    sections: [
      { h: 'Phạm vi & đơn vị vận chuyển', p: ['ToyLand giao hàng toàn quốc thông qua Giao Hàng Nhanh (GHN).', 'Phí vận chuyển được tính tự động theo địa chỉ nhận và khối lượng kiện hàng ngay ở bước thanh toán.'] },
      { h: 'Miễn phí vận chuyển', p: ['Miễn phí giao hàng cho đơn hàng có giá trị từ 500.000₫ (sau khi trừ khuyến mãi).', 'Có thể áp dụng thêm mã FREESHIP cho các đơn chưa đủ điều kiện.'] },
      { h: 'Thời gian giao hàng', p: ['Nội thành TP.HCM, Hà Nội: 1–2 ngày làm việc.', 'Các tỉnh thành khác: 2–5 ngày làm việc.', 'Đơn hàng được xác nhận và đóng gói trong vòng 24 giờ (trừ Chủ nhật, ngày lễ).'] },
      { h: 'Kiểm tra hàng', p: ['Quý khách được kiểm tra ngoại quan sản phẩm trước khi thanh toán cho đơn COD.'] },
    ],
  },
  return: {
    title: 'Chính sách đổi trả', icon: 'autorenew',
    sections: [
      { h: 'Thời hạn', p: ['Yêu cầu đổi trả trong vòng 7 ngày kể từ khi nhận hàng, thao tác ngay trong mục "Đơn mua" của tài khoản.'] },
      { h: 'Điều kiện', p: ['Sản phẩm lỗi do nhà sản xuất, giao sai mẫu, thiếu chi tiết hoặc hư hỏng khi vận chuyển.', 'Sản phẩm còn nguyên tem, hộp, phụ kiện và quà tặng kèm (nếu có). Vui lòng đính kèm ảnh chụp tình trạng sản phẩm.'] },
      { h: 'Hoàn tiền', p: ['Sau khi yêu cầu được duyệt, ToyLand hoàn tiền trong 3–7 ngày làm việc qua tài khoản ngân hàng hoặc ví MoMo.'] },
      { h: 'Không áp dụng', p: ['Sản phẩm đã qua sử dụng, bị bóc tem bảo hành, hư hỏng do sử dụng sai hướng dẫn.'] },
    ],
  },
  warranty: {
    title: 'Chính sách bảo hành', icon: 'build',
    sections: [
      { h: 'Sản phẩm được bảo hành', p: ['Đồ chơi điện tử, xe điều khiển, robot: bảo hành 3–6 tháng tuỳ thương hiệu.', 'Đồ chơi không dùng pin: đổi mới nếu lỗi sản xuất trong 7 ngày.'] },
      { h: 'Cách thức', p: ['Mang sản phẩm kèm mã đơn hàng đến cửa hàng hoặc liên hệ hotline 1900 1234 để được hướng dẫn gửi bảo hành.'] },
    ],
  },
  privacy: {
    title: 'Chính sách bảo mật', icon: 'privacy_tip',
    sections: [
      { h: 'Thông tin thu thập', p: ['Họ tên, email, số điện thoại, địa chỉ giao hàng và lịch sử mua hàng — chỉ dùng để xử lý đơn hàng và chăm sóc khách hàng.'] },
      { h: 'Bảo vệ dữ liệu', p: ['Mật khẩu được mã hoá một chiều bằng BCrypt; phiên đăng nhập dùng JWT có thời hạn ngắn và refresh token có thể thu hồi.', 'ToyLand không lưu thông tin thẻ thanh toán — giao dịch online được xử lý bởi MoMo.'] },
      { h: 'Quyền của bạn', p: ['Bạn có thể xem, chỉnh sửa thông tin cá nhân hoặc yêu cầu xoá tài khoản trong mục "Tài khoản của tôi" (theo Nghị định 13/2023/NĐ-CP về bảo vệ dữ liệu cá nhân).'] },
    ],
  },
  terms: {
    title: 'Điều khoản sử dụng', icon: 'gavel',
    sections: [
      { h: 'Tài khoản', p: ['Bạn chịu trách nhiệm bảo mật mật khẩu. Tài khoản sẽ bị tạm khoá 15 phút nếu đăng nhập sai 5 lần liên tiếp.'] },
      { h: 'Đặt hàng & giá', p: ['Giá hiển thị đã bao gồm VAT. ToyLand có quyền huỷ đơn nếu phát hiện sai sót về giá hoặc tồn kho và sẽ thông báo cho quý khách.', 'Đơn thanh toán MoMo không hoàn tất trong 15 phút sẽ tự động huỷ.'] },
      { h: 'An toàn sản phẩm', p: ['Phụ huynh vui lòng đọc kỹ độ tuổi khuyến nghị và cảnh báo an toàn trên mỗi sản phẩm trước khi cho bé sử dụng.'] },
    ],
  },
  payment: {
    title: 'Phương thức thanh toán', icon: 'payments',
    sections: [
      { h: 'Thanh toán khi nhận hàng (COD)', p: ['Thanh toán bằng tiền mặt cho nhân viên giao hàng.'] },
      { h: 'Thanh toán online qua ví MoMo', p: ['Quét mã QR bằng ứng dụng MoMo, hoặc dùng thẻ ATM nội địa / thẻ quốc tế Visa/Master/JCB trên trang thanh toán MoMo.', 'Đơn hàng cần được thanh toán trong 15 phút; bạn có thể thanh toán lại trong mục Đơn mua nếu lần đầu thất bại.'] },
    ],
  },
  'shopping-guide': {
    title: 'Hướng dẫn mua hàng', icon: 'help',
    sections: [
      { h: 'Bước 1 – Chọn sản phẩm', p: ['Tìm kiếm hoặc lọc theo danh mục, độ tuổi, thương hiệu, nhân vật. Chọn phân loại (màu sắc, kích thước…) và bấm "Thêm vào giỏ".'] },
      { h: 'Bước 2 – Kiểm tra giỏ hàng', p: ['Điều chỉnh số lượng, xem mã giảm giá khả dụng. Bạn có thể thêm hàng vào giỏ ngay cả khi chưa đăng nhập.'] },
      { h: 'Bước 3 – Thanh toán', p: ['Đăng nhập, chọn địa chỉ nhận hàng, áp mã giảm giá, chọn gói quà (nếu cần) và phương thức thanh toán.'] },
      { h: 'Bước 4 – Theo dõi đơn', p: ['Theo dõi trạng thái trong mục "Đơn mua" hoặc trang Tra cứu đơn hàng. Email thông báo sẽ được gửi khi đơn thay đổi trạng thái.'] },
    ],
  },
  faq: {
    title: 'Câu hỏi thường gặp', icon: 'quiz',
    sections: [
      { h: 'Tôi có thể huỷ đơn không?', p: ['Có — khi đơn còn ở trạng thái "Chờ thanh toán" hoặc "Chờ xác nhận", vào Đơn mua → Huỷ đơn.'] },
      { h: 'Làm sao biết đồ chơi phù hợp với bé?', p: ['Mỗi sản phẩm có nhãn độ tuổi và thông tin an toàn (chi tiết nhỏ, pin, chất liệu). Dùng bộ lọc "Độ tuổi" để tìm nhanh.'] },
      { h: 'Tôi quên mật khẩu?', p: ['Chọn "Quên mật khẩu" ở trang đăng nhập, hệ thống sẽ gửi liên kết đặt lại mật khẩu (hiệu lực 15 phút) tới email của bạn.'] },
    ],
  },
};

@Component({
  selector: 'tl-policy',
  imports: [MatIconModule, RouterLink, RouterLinkActive],
  template: `
    <div class="container page layout">
      <aside class="card side">
        @for (k of keys; track k) {
          <a [routerLink]="['/policy', k]" routerLinkActive="on"><mat-icon>{{ pages[k].icon }}</mat-icon>{{ pages[k].title }}</a>
        }
      </aside>
      <article class="card prose">
        @if (page(); as pg) {
          <h1><mat-icon>{{ pg.icon }}</mat-icon>{{ pg.title }}</h1>
          @for (s of pg.sections; track s.h) {
            <h3>{{ s.h }}</h3>
            <ul>@for (x of s.p; track x) { <li>{{ x }}</li> }</ul>
          }
        } @else {
          <h2>Không tìm thấy trang</h2>
        }
      </article>
    </div>
  `,
  styles: `
    .layout { display: grid; grid-template-columns: 260px minmax(0, 1fr); gap: 20px; align-items: start; }
    .side { padding: 8px; }
    .side a { display: flex; align-items: center; gap: 10px; padding: 10px 12px; border-radius: 10px; font-weight: 600; font-size: 14px; }
    .side a.on, .side a:hover { background: var(--tl-red-soft); color: var(--tl-red); }
    h1 { display: flex; align-items: center; gap: 10px; } h1 mat-icon { color: var(--tl-red); }
    h3 { margin-top: 20px; }
    @media (max-width: 800px) { .layout { grid-template-columns: minmax(0, 1fr); } }
  `,
})
export class PolicyComponent {
  type = input.required<string>();
  pages = PAGES;
  keys = Object.keys(PAGES);
  page = computed(() => PAGES[this.type()] ?? null);
  private title = inject(Title);

  constructor() {
    effect(() => this.title.setTitle(`${this.page()?.title ?? 'Chính sách'} - ToyLand`));
  }
}
