import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { RouterLink } from '@angular/router';
import { NotifyService } from '../core/notify.service';
import { LogoComponent } from '../shared/logo.component';

@Component({
  selector: 'tl-shop-footer',
  imports: [RouterLink, FormsModule, MatIconModule, LogoComponent],
  template: `
    <!-- Cam ket: 4 the clay pastel -->
    <section class="usp container">
      <div class="u" style="--c: var(--p-sky)"><span class="ic"><mat-icon>local_shipping</mat-icon></span><div><b>Giao hàng toàn quốc</b><span>Freeship đơn từ 500K</span></div></div>
      <div class="u" style="--c: var(--p-mint)"><span class="ic"><mat-icon>verified_user</mat-icon></span><div><b>Chính hãng, an toàn</b><span>Đạt chuẩn hợp quy CR</span></div></div>
      <div class="u" style="--c: var(--p-butter)"><span class="ic"><mat-icon>autorenew</mat-icon></span><div><b>Đổi trả 7 ngày</b><span>Nếu lỗi do nhà sản xuất</span></div></div>
      <div class="u" style="--c: var(--p-pink)"><span class="ic"><mat-icon>card_giftcard</mat-icon></span><div><b>Gói quà miễn phí thiệp</b><span>Kèm lời chúc cho bé</span></div></div>
    </section>

    <footer class="footer">
      <!-- duong luon song tach khoi noi dung -->
      <svg class="wave" viewBox="0 0 1440 60" preserveAspectRatio="none" aria-hidden="true">
        <path d="M0 30 C 120 0, 240 60, 360 30 S 600 0, 720 30 S 960 60, 1080 30 S 1320 0, 1440 30 V60 H0 Z" fill="currentColor" />
      </svg>
      <div class="inner">
        <div class="container cols">
          <div class="brand-col">
            <tl-logo variant="dark" [size]="40" />
            <p>Nơi mỗi món đồ chơi là một cuộc phiêu lưu nhỏ. Chọn lọc an toàn cho bé, dễ chịu cho ba mẹ.</p>
            <p class="contact"><mat-icon>place</mat-icon> 12 Nguyễn Văn Bảo, Gò Vấp, TP.HCM</p>
            <p class="contact"><mat-icon>call</mat-icon> 1900 1234 &nbsp;·&nbsp; <mat-icon>mail</mat-icon> cskh&#64;toyland.vn</p>
          </div>
          <div>
            <h4>Hỗ trợ</h4>
            <a routerLink="/track-order">Tra cứu đơn hàng</a>
            <a routerLink="/policy/shopping-guide">Hướng dẫn mua hàng</a>
            <a routerLink="/policy/payment">Thanh toán</a>
            <a routerLink="/policy/faq">Câu hỏi thường gặp</a>
          </div>
          <div>
            <h4>Chính sách</h4>
            <a routerLink="/policy/shipping">Giao hàng</a>
            <a routerLink="/policy/return">Đổi trả</a>
            <a routerLink="/policy/warranty">Bảo hành</a>
            <a routerLink="/policy/privacy">Bảo mật</a>
            <a routerLink="/policy/terms">Điều khoản</a>
          </div>
          <div class="news">
            <h4>Nhận quà & ưu đãi</h4>
            <p>Mã giảm giá mỗi tháng và gợi ý đồ chơi theo tuổi của bé.</p>
            <form class="nl" (ngSubmit)="subscribe()">
              <input type="email" name="email" [(ngModel)]="email" placeholder="Email của ba mẹ" aria-label="Email nhận tin" required />
              <button type="submit" [class.done]="done()">
                <mat-icon>{{ done() ? 'check' : 'send' }}</mat-icon>
              </button>
            </form>
            <div class="social">
              <a href="#" aria-label="Facebook" (click)="$event.preventDefault()"><mat-icon>thumb_up</mat-icon></a>
              <a href="#" aria-label="YouTube" (click)="$event.preventDefault()"><mat-icon>smart_display</mat-icon></a>
              <a href="#" aria-label="Instagram" (click)="$event.preventDefault()"><mat-icon>photo_camera</mat-icon></a>
              <a href="#" aria-label="Zalo" (click)="$event.preventDefault()"><mat-icon>chat_bubble</mat-icon></a>
            </div>
          </div>
        </div>
        <div class="container bottom">
          <span>© 2026 ToyLand · Đồ án học phần Phát triển phần mềm hướng dịch vụ</span>
          <span class="pay"><b>COD</b><b>MoMo</b><b>ATM</b><b>QR</b></span>
        </div>
      </div>
    </footer>
  `,
  styles: `
    .usp { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-top: 56px; margin-bottom: 24px; }
    .u { display: flex; gap: 14px; align-items: center; background: #fff; border-radius: 22px; padding: 16px; box-shadow: var(--clay-sm); }
    .ic { width: 52px; height: 52px; border-radius: 16px; background: var(--c); display: grid; place-items: center; flex: none;
      box-shadow: inset 0 -3px 0 rgb(0 0 0 / 6%), inset 0 2px 0 rgb(255 255 255 / 70%); transition: transform .3s var(--ease-bounce); }
    .u:hover .ic { transform: rotate(-10deg) scale(1.08); }
    .ic mat-icon { color: var(--ink); }
    .u b { display: block; font-family: var(--font-display); font-size: 16px; }
    .u span { font-size: 13px; color: var(--ink-2); }

    .footer { color: var(--ink); margin-top: 24px; }
    .wave { display: block; width: 100%; height: 50px; margin-bottom: -1px; }
    .inner { background: var(--ink); color: #cfc8ea; padding: 24px 0 0; font-size: 14px; }
    .cols { display: grid; grid-template-columns: 1.4fr 1fr 1fr 1.4fr; gap: 32px; }
    .brand-col p { line-height: 1.7; max-width: 340px; }
    .contact mat-icon { font-size: 16px; width: 16px; height: 16px; vertical-align: -3px; color: var(--tl-yellow); }
    h4 { color: #fff; font-size: 17px; }
    .cols a { display: block; padding: 5px 0; transition: color .15s, transform .15s; }
    .cols a:hover { color: #fff; transform: translateX(3px); }
    .news p { margin-top: 0; }
    .nl { display: flex; background: rgb(255 255 255 / 8%); border-radius: 999px; padding: 5px; border: 1px solid rgb(255 255 255 / 12%); }
    .nl:focus-within { border-color: var(--tl-yellow); }
    .nl input { flex: 1; min-width: 0; border: 0; background: transparent; outline: 0; color: #fff; font: inherit; padding: 0 14px; }
    .nl input::placeholder { color: #9d95bd; }
    .nl button { width: 42px; height: 42px; border: 0; border-radius: 50%; cursor: pointer; background: var(--grad-sun); color: var(--ink); display: grid; place-items: center; transition: transform .25s var(--ease-bounce), background .2s; }
    .nl button:hover { transform: rotate(-12deg) scale(1.06); }
    .nl button.done { background: #6fd3a0; }
    .social { display: flex; gap: 10px; margin-top: 16px; }
    .social a { width: 42px; height: 42px; border-radius: 14px; display: grid !important; place-items: center; padding: 0 !important; background: rgb(255 255 255 / 8%); transition: background .2s, transform .25s var(--ease-bounce) !important; }
    .social a:hover { background: var(--tl-red); transform: translateY(-3px) rotate(-6deg) !important; color: #fff; }
    .social mat-icon { font-size: 20px; width: 20px; height: 20px; }
    .bottom { display: flex; justify-content: space-between; align-items: center; gap: 12px; flex-wrap: wrap; border-top: 1px solid rgb(255 255 255 / 10%); margin-top: 32px; padding-top: 16px; padding-bottom: 20px; font-size: 13px; }
    .pay b { display: inline-block; background: #fff; color: var(--ink); font-size: 11px; padding: 4px 8px; border-radius: 8px; margin-left: 6px; }
    @media (max-width: 960px) { .usp, .cols { grid-template-columns: repeat(2, 1fr); } }
    @media (max-width: 600px) { .usp, .cols { grid-template-columns: 1fr; } .usp { gap: 10px; margin-top: 32px; } }
  `,
})
export class ShopFooterComponent {
  private notify = inject(NotifyService);
  email = '';
  done = signal(false);

  /** Demo giao dien: chua co API luu email nhan tin */
  subscribe(): void {
    if (!/^[^@\s]+@[^@\s]+\.[^@\s]+$/.test(this.email)) {
      this.notify.error('Email chưa đúng định dạng');
      return;
    }
    this.done.set(true);
    this.notify.success('Cảm ơn ba mẹ! ToyLand sẽ gửi ưu đãi sớm nhất 💌');
    this.email = '';
    setTimeout(() => this.done.set(false), 2500);
  }
}
