import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatRadioModule } from '@angular/material/radio';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { Router, RouterLink } from '@angular/router';
import { ShopApi } from '../../core/api.service';
import { AuthService } from '../../core/auth.service';
import { CartStore } from '../../core/cart.store';
import { PLACEHOLDER_IMG } from '../../core/labels';
import { Address, PaymentMethod, Quote, Voucher } from '../../core/models';
import { NotifyService } from '../../core/notify.service';
import { VndPipe } from '../../core/vnd.pipe';
import { AddressDialogComponent } from '../../shared/address-dialog.component';

@Component({
  selector: 'tl-checkout',
  imports: [RouterLink, FormsModule, MatButtonModule, MatIconModule, MatRadioModule, MatCheckboxModule, MatFormFieldModule, MatInputModule, MatSlideToggleModule, VndPipe],
  templateUrl: './checkout.component.html',
  styleUrl: './checkout.component.scss',
})
export class CheckoutComponent implements OnInit {
  private api = inject(ShopApi);
  auth = inject(AuthService);
  cart = inject(CartStore);
  private notify = inject(NotifyService);
  private router = inject(Router);
  private dialog = inject(MatDialog);

  addresses = signal<Address[]>([]);
  addressId = signal<number | null>(null);
  showAllAddresses = signal(false);
  quote = signal<Quote | null>(null);
  vouchers = signal<Voucher[]>([]);
  voucherInput = '';
  appliedVoucher = signal<string | null>(null);
  giftWrap = signal(false);
  usePoints = signal(false);
  giftMessage = '';
  note = '';
  payment = signal<PaymentMethod>('COD');
  placing = signal(false);
  /** Khóa chống đặt trùng: giữ nguyên khi mạng lỗi (retry an toàn), đổi khi server đã trả lời lỗi rõ ràng */
  private idemKey = crypto.randomUUID();
  ph = PLACEHOLDER_IMG;

  ngOnInit(): void {
    this.api.addresses().subscribe((list) => {
      this.addresses.set(list);
      const def = list.find((a) => a.isDefault) ?? list[0];
      this.addressId.set(def?.id ?? null);
      this.refresh();
      if (!list.length) this.addAddress();
    });
    this.api.availableVouchers().subscribe({ next: (v) => this.vouchers.set(v), error: () => {} });
  }

  selectedAddress(): Address | undefined {
    return this.addresses().find((a) => a.id === this.addressId());
  }

  refresh(): void {
    this.api.quote({ addressId: this.addressId(), voucherCode: this.appliedVoucher(), giftWrap: this.giftWrap(), usePoints: this.usePoints() }).subscribe({
      next: (q) => {
        this.quote.set(q);
        if (q.voucherError && this.appliedVoucher()) {
          this.notify.error(q.voucherError);
          this.appliedVoucher.set(null);
        }
        if (!q.momoEnabled && this.payment() === 'MOMO') this.payment.set('COD');
      },
      error: (e) => this.notify.error(e),
    });
  }

  chooseAddress(id: number): void {
    this.addressId.set(id);
    this.showAllAddresses.set(false);
    this.refresh();
  }

  addAddress(edit?: Address): void {
    this.dialog.open(AddressDialogComponent, { data: edit ?? null, width: '600px', maxWidth: '96vw' }).afterClosed().subscribe((a?: Address) => {
      if (!a) return;
      this.api.addresses().subscribe((list) => {
        this.addresses.set(list);
        this.chooseAddress(a.id);
      });
    });
  }

  applyVoucher(code?: string): void {
    const c = (code ?? this.voucherInput).trim().toUpperCase();
    if (!c) return;
    this.voucherInput = c;
    this.appliedVoucher.set(c);
    this.api.quote({ addressId: this.addressId(), voucherCode: c, giftWrap: this.giftWrap(), usePoints: this.usePoints() }).subscribe({
      next: (q) => {
        this.quote.set(q);
        if (q.voucherError) {
          this.notify.error(q.voucherError);
          this.appliedVoucher.set(null);
        } else this.notify.success(`Đã áp dụng mã ${c}`);
      },
      error: (e) => this.notify.error(e),
    });
  }

  removeVoucher(): void {
    this.appliedVoucher.set(null);
    this.voucherInput = '';
    this.refresh();
  }

  togglePoints(on: boolean): void {
    this.usePoints.set(on);
    this.refresh();
  }

  toggleGift(on: boolean): void {
    this.giftWrap.set(on);
    this.refresh();
  }

  place(): void {
    const q = this.quote();
    const addressId = this.addressId();
    if (!addressId) {
      this.notify.error('Vui lòng thêm địa chỉ giao hàng');
      return;
    }
    if (!q?.canPlaceOrder) {
      this.notify.error(q?.issues?.join(' • ') || 'Không thể đặt hàng');
      return;
    }
    this.placing.set(true);
    this.api.placeOrder({
      addressId, voucherCode: this.appliedVoucher(), paymentMethod: this.payment(), note: this.note || null,
      giftWrap: this.giftWrap(), giftMessage: this.giftWrap() ? this.giftMessage || null : null, usePoints: this.usePoints(),
    }, this.idemKey).subscribe({
      next: (r) => {
        this.cart.reload();
        if (r.paymentUrl) {
          this.notify.info('Đang chuyển sang trang thanh toán MoMo…');
          window.location.href = r.paymentUrl;
        } else {
          this.router.navigate(['/order-success', r.orderId]);
        }
      },
      error: (e) => {
        this.placing.set(false);
        if (e?.status >= 400 && e?.status < 500 && e?.status !== 409) this.idemKey = crypto.randomUUID();
        this.notify.error(e);
        this.refresh();
      },
    });
  }
}
