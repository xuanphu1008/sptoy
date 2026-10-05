import { Component, OnInit, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { ShopApi } from '../../core/api.service';
import { Address } from '../../core/models';
import { NotifyService } from '../../core/notify.service';
import { AddressDialogComponent } from '../../shared/address-dialog.component';
import { confirmDialog } from '../../shared/confirm-dialog.component';

@Component({
  selector: 'tl-addresses',
  imports: [MatButtonModule, MatIconModule],
  template: `
    <div class="card">
      <div class="row between wrap">
        <h2 class="mb-0">Sổ địa chỉ</h2>
        <button mat-flat-button (click)="edit()"><mat-icon>add</mat-icon>Thêm địa chỉ mới</button>
      </div>
      @for (a of list(); track a.id) {
        <div class="addr">
          <div class="grow">
            <div><b>{{ a.receiverName }}</b> <span class="muted">| {{ a.phone }}</span>
              @if (a.isDefault) { <span class="chip red">Mặc định</span> }
              @if (!a.wardCode) { <span class="chip grey">Nhập tay</span> }
            </div>
            <div class="muted">{{ a.fullAddress }}</div>
          </div>
          <div class="acts">
            <button mat-button (click)="edit(a)">Sửa</button>
            @if (!a.isDefault) {
              <button mat-button (click)="remove(a)">Xoá</button>
              <button mat-stroked-button (click)="setDefault(a)">Đặt mặc định</button>
            }
          </div>
        </div>
      } @empty {
        <div class="empty"><mat-icon>home_pin</mat-icon><p>Bạn chưa có địa chỉ nào.</p></div>
      }
    </div>
  `,
  styles: `
    .addr { display: flex; gap: 12px; align-items: center; padding: 16px 0; border-bottom: 1px solid #f0f0f0; flex-wrap: wrap; }
    .acts { display: flex; gap: 4px; align-items: center; }
    .chip { margin-left: 6px; }
  `,
})
export class AddressesComponent implements OnInit {
  private api = inject(ShopApi);
  private dialog = inject(MatDialog);
  private notify = inject(NotifyService);
  list = signal<Address[]>([]);

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.api.addresses().subscribe((l) => this.list.set(l));
  }

  edit(a?: Address): void {
    this.dialog.open(AddressDialogComponent, { data: a ?? null, width: '600px', maxWidth: '96vw' }).afterClosed().subscribe((r) => r && this.load());
  }

  setDefault(a: Address): void {
    this.api.setDefaultAddress(a.id).subscribe({ next: () => this.load(), error: (e) => this.notify.error(e) });
  }

  remove(a: Address): void {
    confirmDialog(this.dialog, { title: 'Xoá địa chỉ này?', message: a.fullAddress, confirmText: 'Xoá', danger: true }).subscribe((ok) => {
      if (ok) this.api.deleteAddress(a.id).subscribe({ next: () => this.load(), error: (e) => this.notify.error(e) });
    });
  }
}
