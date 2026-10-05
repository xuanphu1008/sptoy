import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { ShopApi } from '../core/api.service';
import { Address, AddressRequest, Option } from '../core/models';
import { NotifyService } from '../core/notify.service';

/**
 * Them / sua dia chi giao hang.
 * Neu GHN da cau hinh: chon Tinh -> Quan -> Phuong tu API GHN (co ma de tinh phi ship that).
 * Neu chua: nhap tay ten Tinh/Quan/Phuong (phi ship co dinh).
 */
@Component({
  selector: 'tl-address-dialog',
  imports: [ReactiveFormsModule, MatDialogModule, MatButtonModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatCheckboxModule],
  template: `
    <h2 mat-dialog-title>{{ data?.id ? 'Sửa địa chỉ' : 'Thêm địa chỉ mới' }}</h2>
    <mat-dialog-content>
      <form [formGroup]="f" class="form">
        <div class="grid-2">
          <mat-form-field><mat-label>Họ tên người nhận</mat-label><input matInput formControlName="receiverName" /></mat-form-field>
          <mat-form-field><mat-label>Số điện thoại</mat-label><input matInput formControlName="phone" placeholder="0912345678" />
            @if (f.controls.phone.hasError('pattern')) { <mat-error>SĐT 10-11 số, bắt đầu bằng 0</mat-error> }
          </mat-form-field>
        </div>
        @if (ghn()) {
          <mat-form-field class="full"><mat-label>Tỉnh / Thành phố</mat-label>
            <mat-select formControlName="provinceId" (selectionChange)="onProvince($event.value)">
              @for (o of provinces(); track o.id) { <mat-option [value]="+o.id">{{ o.name }}</mat-option> }
            </mat-select>
          </mat-form-field>
          <div class="grid-2">
            <mat-form-field><mat-label>Quận / Huyện</mat-label>
              <mat-select formControlName="districtId" (selectionChange)="onDistrict($event.value)">
                @for (o of districts(); track o.id) { <mat-option [value]="+o.id">{{ o.name }}</mat-option> }
              </mat-select>
            </mat-form-field>
            <mat-form-field><mat-label>Phường / Xã</mat-label>
              <mat-select formControlName="wardCode" (selectionChange)="onWard($event.value)">
                @for (o of wards(); track o.id) { <mat-option [value]="o.id">{{ o.name }}</mat-option> }
              </mat-select>
            </mat-form-field>
          </div>
        } @else {
          <div class="note">GHN chưa được cấu hình → nhập tay địa chỉ, phí ship tính theo mức cố định.</div>
          <mat-form-field class="full"><mat-label>Tỉnh / Thành phố</mat-label><input matInput formControlName="provinceName" /></mat-form-field>
          <div class="grid-2">
            <mat-form-field><mat-label>Quận / Huyện</mat-label><input matInput formControlName="districtName" /></mat-form-field>
            <mat-form-field><mat-label>Phường / Xã</mat-label><input matInput formControlName="wardName" /></mat-form-field>
          </div>
        }
        <mat-form-field class="full"><mat-label>Số nhà, tên đường</mat-label><input matInput formControlName="detail" /></mat-form-field>
        <mat-checkbox formControlName="isDefault">Đặt làm địa chỉ mặc định</mat-checkbox>
      </form>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close>Huỷ</button>
      <button mat-flat-button (click)="save()" [disabled]="saving()">Lưu địa chỉ</button>
    </mat-dialog-actions>
  `,
  styles: `.form { padding-top: 6px; max-width: 560px; } .note { background: var(--tl-yellow-soft); padding: 8px 12px; border-radius: 8px; font-size: 13px; margin-bottom: 12px; }`,
})
export class AddressDialogComponent implements OnInit {
  data = inject<Address | null>(MAT_DIALOG_DATA);
  private api = inject(ShopApi);
  private ref = inject(MatDialogRef<AddressDialogComponent>);
  private notify = inject(NotifyService);
  private fb = inject(FormBuilder);

  ghn = signal(false);
  saving = signal(false);
  provinces = signal<Option[]>([]);
  districts = signal<Option[]>([]);
  wards = signal<Option[]>([]);

  f = this.fb.group({
    receiverName: [this.data?.receiverName ?? '', Validators.required],
    phone: [this.data?.phone ?? '', [Validators.required, Validators.pattern(/^0\d{9,10}$/)]],
    provinceId: [this.data?.provinceId ?? (null as number | null)],
    provinceName: [this.data?.provinceName ?? '', Validators.required],
    districtId: [this.data?.districtId ?? (null as number | null)],
    districtName: [this.data?.districtName ?? '', Validators.required],
    wardCode: [this.data?.wardCode ?? (null as string | null)],
    wardName: [this.data?.wardName ?? '', Validators.required],
    detail: [this.data?.detail ?? '', Validators.required],
    isDefault: [this.data?.isDefault ?? false],
  });

  ngOnInit(): void {
    this.api.shippingConfig().subscribe((c) => {
      if (!c.ghnEnabled) return;
      this.api.provinces().subscribe({
        next: (p) => {
          this.ghn.set(true);
          this.provinces.set(p);
          const v = this.f.value;
          if (v.provinceId) this.api.districts(v.provinceId).subscribe((d) => this.districts.set(d));
          if (v.districtId) this.api.wards(v.districtId).subscribe((w) => this.wards.set(w));
        },
        error: () => this.ghn.set(false),
      });
    });
  }

  onProvince(id: number): void {
    this.f.patchValue({ provinceName: this.nameOf(this.provinces(), id), districtId: null, districtName: '', wardCode: null, wardName: '' });
    this.districts.set([]);
    this.wards.set([]);
    this.api.districts(id).subscribe((d) => this.districts.set(d));
  }

  onDistrict(id: number): void {
    this.f.patchValue({ districtName: this.nameOf(this.districts(), id), wardCode: null, wardName: '' });
    this.wards.set([]);
    this.api.wards(id).subscribe((w) => this.wards.set(w));
  }

  onWard(code: string): void {
    this.f.patchValue({ wardName: this.nameOf(this.wards(), code) });
  }

  private nameOf(list: Option[], id: number | string): string {
    return list.find((o) => String(o.id) === String(id))?.name ?? '';
  }

  save(): void {
    if (this.f.invalid) {
      this.f.markAllAsTouched();
      this.notify.error('Vui lòng nhập đầy đủ thông tin địa chỉ');
      return;
    }
    const body = this.f.getRawValue() as AddressRequest;
    if (!this.ghn()) {
      // Nhap tay: xoa ma GHN neu ten tinh/quan/phuong bi doi (tranh tinh phi ship sai quan)
      const d = this.data;
      if (!d?.id || d.provinceName !== body.provinceName || d.districtName !== body.districtName || d.wardName !== body.wardName) {
        body.provinceId = null; body.districtId = null; body.wardCode = null;
      }
    }
    this.saving.set(true);
    const req = this.data?.id ? this.api.updateAddress(this.data.id, body) : this.api.addAddress(body);
    req.subscribe({
      next: (a) => {
        this.notify.success('Đã lưu địa chỉ');
        this.ref.close(a);
      },
      error: (e) => {
        this.saving.set(false);
        this.notify.error(e);
      },
    });
  }
}
