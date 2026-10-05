import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialog, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { Observable } from 'rxjs';

export interface ConfirmData {
  title: string;
  message?: string;
  confirmText?: string;
  danger?: boolean;
  /** Neu co: hien o nhap ly do / ghi chu, ket qua tra ve la chuoi */
  input?: { label: string; required?: boolean; options?: string[]; value?: string };
}

/** Hop thoai xac nhan (co the kem o nhap ly do). Tra ve true/chuoi hoac undefined neu huy. */
@Component({
  selector: 'tl-confirm-dialog',
  imports: [MatDialogModule, MatButtonModule, FormsModule, MatFormFieldModule, MatInputModule, MatSelectModule],
  template: `
    <h2 mat-dialog-title>{{ data.title }}</h2>
    <mat-dialog-content>
      @if (data.message) { <p style="white-space: pre-line">{{ data.message }}</p> }
      @if (data.input; as inp) {
        @if (inp.options?.length) {
          <mat-form-field class="full">
            <mat-label>{{ inp.label }}</mat-label>
            <mat-select [(ngModel)]="choice">
              @for (o of inp.options; track o) { <mat-option [value]="o">{{ o }}</mat-option> }
            </mat-select>
          </mat-form-field>
          @if (choice === 'Lý do khác') {
            <mat-form-field class="full">
              <mat-label>Nhập lý do</mat-label>
              <textarea matInput rows="2" [(ngModel)]="text"></textarea>
            </mat-form-field>
          }
        } @else {
          <mat-form-field class="full">
            <mat-label>{{ inp.label }}</mat-label>
            <textarea matInput rows="3" [(ngModel)]="text"></textarea>
          </mat-form-field>
        }
      }
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close>Huỷ</button>
      <button mat-flat-button [class.danger]="data.danger" [disabled]="!valid()" (click)="ok()">{{ data.confirmText || 'Xác nhận' }}</button>
    </mat-dialog-actions>
  `,
  styles: `.danger { --mdc-filled-button-container-color: #c62828; } mat-form-field { min-width: 360px; } @media (max-width: 600px) { mat-form-field { min-width: 0; } }`,
})
export class ConfirmDialogComponent {
  data = inject<ConfirmData>(MAT_DIALOG_DATA);
  private ref = inject(MatDialogRef<ConfirmDialogComponent>);
  choice = this.data.input?.options?.[0] ?? '';
  text = this.data.input?.value ?? '';

  value(): string {
    const inp = this.data.input;
    if (!inp) return '';
    if (inp.options?.length) return this.choice === 'Lý do khác' ? this.text.trim() || this.choice : this.choice;
    return this.text.trim();
  }

  valid(): boolean {
    return !this.data.input?.required || this.value().length > 0;
  }

  ok(): void {
    this.ref.close(this.data.input ? this.value() : true);
  }
}

/** Tien ich mo hop thoai xac nhan */
export function confirmDialog<T = boolean | string>(dialog: MatDialog, data: ConfirmData): Observable<T | undefined> {
  return dialog.open(ConfirmDialogComponent, { data, autoFocus: false }).afterClosed();
}
