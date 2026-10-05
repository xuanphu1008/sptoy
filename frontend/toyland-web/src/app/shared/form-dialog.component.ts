import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialog, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { Observable } from 'rxjs';
import { ShopApi } from '../core/api.service';
import { NotifyService } from '../core/notify.service';

export interface FieldDef {
  key: string;
  label: string;
  type?: 'text' | 'textarea' | 'number' | 'select' | 'toggle' | 'datetime' | 'color' | 'image';
  options?: { value: unknown; label: string }[];
  required?: boolean;
  hint?: string;
  /** kiem tra dinh dang (vd EMAIL_PATTERN) + thong bao khi sai */
  pattern?: RegExp;
  patternError?: string;
  /** chiem ca 2 cot */
  wide?: boolean;
  /** thu muc upload cho type=image */
  folder?: string;
  /** an field khi ham tra ve false */
  showIf?: (v: Record<string, unknown>) => boolean;
}

export interface FormDialogData {
  title: string;
  fields: FieldDef[];
  value: Record<string, unknown>;
  /** Goi API luu; dong dialog khi thanh cong */
  save: (v: Record<string, unknown>) => Observable<unknown>;
}

/** Hop thoai form dung chung cho cac man hinh CRUD don gian cua admin */
@Component({
  selector: 'tl-form-dialog',
  imports: [FormsModule, MatDialogModule, MatButtonModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatSlideToggleModule, MatIconModule],
  template: `
    <h2 mat-dialog-title>{{ data.title }}</h2>
    <mat-dialog-content>
      <div class="form">
        @for (f of data.fields; track f.key) {
          @if (!f.showIf || f.showIf(v)) {
            @switch (f.type || 'text') {
              @case ('toggle') {
                <mat-slide-toggle class="tg" [(ngModel)]="v[f.key]">{{ f.label }}</mat-slide-toggle>
              }
              @case ('select') {
                <mat-form-field [class.wide]="f.wide"><mat-label>{{ f.label }}</mat-label>
                  <mat-select [(ngModel)]="v[f.key]">@for (o of f.options; track $index) { <mat-option [value]="o.value">{{ o.label }}</mat-option> }</mat-select>
                  @if (f.hint) { <mat-hint>{{ f.hint }}</mat-hint> }
                </mat-form-field>
              }
              @case ('textarea') {
                <mat-form-field class="wide"><mat-label>{{ f.label }}</mat-label><textarea matInput rows="3" [(ngModel)]="v[f.key]"></textarea>
                  @if (f.hint) { <mat-hint>{{ f.hint }}</mat-hint> }</mat-form-field>
              }
              @case ('image') {
                <div class="wide img-field">
                  @if (v[f.key]) { <img [src]="$any(v[f.key])" alt="" /> }
                  <mat-form-field class="grow"><mat-label>{{ f.label }}</mat-label><input matInput [(ngModel)]="v[f.key]" placeholder="https://… hoặc tải lên" /></mat-form-field>
                  <label class="up"><mat-icon>upload</mat-icon>{{ uploading() ? '…' : 'Tải lên' }}
                    <input type="file" accept="image/*" hidden (change)="upload($event, f)" /></label>
                </div>
              }
              @default {
                <mat-form-field [class.wide]="f.wide"><mat-label>{{ f.label }}</mat-label>
                  <input matInput [type]="f.type === 'datetime' ? 'datetime-local' : f.type === 'number' ? 'number' : f.type === 'color' ? 'color' : 'text'"
                         [(ngModel)]="v[f.key]" [required]="!!f.required" />
                  @if (f.hint) { <mat-hint>{{ f.hint }}</mat-hint> }
                </mat-form-field>
              }
            }
          }
        }
      </div>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close>Huỷ</button>
      <button mat-flat-button (click)="submit()" [disabled]="saving()">Lưu</button>
    </mat-dialog-actions>
  `,
  styles: `
    .form { display: grid; grid-template-columns: 1fr 1fr; gap: 4px 16px; padding-top: 6px; width: 640px; max-width: 100%; }
    .wide { grid-column: 1 / -1; }
    .tg { margin: 8px 0 16px; }
    .img-field { display: flex; gap: 12px; align-items: center; }
    .img-field img { width: 56px; height: 56px; object-fit: cover; border-radius: 8px; margin-bottom: 20px; }
    .up { margin-bottom: 20px; cursor: pointer; display: inline-flex; align-items: center; gap: 4px; border: 1px solid var(--tl-border); border-radius: 20px; padding: 8px 14px; font-weight: 600; white-space: nowrap; }
    .up:hover { border-color: var(--tl-red); color: var(--tl-red); }
    @media (max-width: 600px) { .form { grid-template-columns: 1fr; } }
  `,
})
export class FormDialogComponent {
  data = inject<FormDialogData>(MAT_DIALOG_DATA);
  private ref = inject(MatDialogRef<FormDialogComponent>);
  private notify = inject(NotifyService);
  private shop = inject(ShopApi);
  v: Record<string, unknown> = { ...this.data.value };
  saving = signal(false);
  uploading = signal(false);

  constructor() {
    // LocalDateTime tu backend "2026-09-01T10:00:00" -> input datetime-local can "2026-09-01T10:00"
    for (const f of this.data.fields) {
      if (f.type === 'datetime' && typeof this.v[f.key] === 'string') this.v[f.key] = (this.v[f.key] as string).slice(0, 16);
    }
  }

  upload(ev: Event, f: FieldDef): void {
    const el = ev.target as HTMLInputElement;
    const file = el.files?.[0];
    el.value = '';
    if (!file) return;
    this.uploading.set(true);
    this.shop.upload(file, f.folder || 'products').subscribe({
      next: (r) => (this.v[f.key] = r.url),
      error: (e) => this.notify.error(e),
    }).add(() => this.uploading.set(false));
  }

  submit(): void {
    const missing = this.data.fields.find((f) => f.required && (!f.showIf || f.showIf(this.v)) && (this.v[f.key] === null || this.v[f.key] === undefined || this.v[f.key] === ''));
    if (missing) return this.notify.error(`Vui lòng nhập "${missing.label}"`);
    const wrong = this.data.fields.find((f) => f.pattern && (!f.showIf || f.showIf(this.v)) && typeof this.v[f.key] === 'string'
      && (this.v[f.key] as string).trim() !== '' && !f.pattern.test((this.v[f.key] as string).trim()));
    if (wrong) return this.notify.error(wrong.patternError || `"${wrong.label}" không đúng định dạng`);
    const out: Record<string, unknown> = { ...this.v };
    for (const f of this.data.fields) {
      if (out[f.key] === '') out[f.key] = null;
      if (f.type === 'number' && out[f.key] !== null && out[f.key] !== undefined) out[f.key] = Number(out[f.key]);
    }
    this.saving.set(true);
    this.data.save(out).subscribe({
      next: (r) => {
        this.notify.success('Đã lưu');
        this.ref.close(r ?? true);
      },
      error: (e) => {
        this.saving.set(false);
        this.notify.error(e);
      },
    });
  }
}

export function openForm(dialog: MatDialog, data: FormDialogData): Observable<unknown> {
  return dialog.open(FormDialogComponent, { data, maxWidth: '96vw', autoFocus: false }).afterClosed();
}
