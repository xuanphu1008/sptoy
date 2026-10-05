import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { ShopApi } from '../../core/api.service';
import { PLACEHOLDER_IMG, RETURN_REASONS } from '../../core/labels';
import { Order, OrderItem } from '../../core/models';
import { NotifyService } from '../../core/notify.service';
import { ImageUploadComponent } from '../../shared/image-upload.component';

const RATING_TEXT = ['', 'Tệ', 'Không hài lòng', 'Bình thường', 'Hài lòng', 'Tuyệt vời'];

/** Danh gia 1 san pham trong don hang da giao */
@Component({
  selector: 'tl-review-dialog',
  imports: [FormsModule, MatDialogModule, MatButtonModule, MatIconModule, MatFormFieldModule, MatInputModule, ImageUploadComponent],
  template: `
    <h2 mat-dialog-title>Đánh giá sản phẩm</h2>
    <mat-dialog-content>
      <div class="prod">
        <img [src]="item.imageUrl || ph" alt="" />
        <div><b>{{ item.productName }}</b>@if (item.variantName) { <div class="muted small">{{ item.variantName }}</div> }</div>
      </div>
      <div class="stars-pick">
        @for (s of [1, 2, 3, 4, 5]; track s) {
          <mat-icon (click)="rating.set(s)" (mouseenter)="hover.set(s)" (mouseleave)="hover.set(0)">
            {{ (hover() || rating()) >= s ? 'star' : 'star_border' }}
          </mat-icon>
        }
        <span class="txt">{{ text[hover() || rating()] }}</span>
      </div>
      <mat-form-field class="full"><mat-label>Chia sẻ cảm nhận của bạn về sản phẩm</mat-label>
        <textarea matInput rows="4" maxlength="2000" [(ngModel)]="comment" placeholder="Chất lượng, độ an toàn, bé có thích không…"></textarea>
      </mat-form-field>
      <tl-image-upload [(urls)]="images" folder="reviews" [max]="5" />
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close>Huỷ</button>
      <button mat-flat-button [disabled]="saving()" (click)="submit()">Gửi đánh giá</button>
    </mat-dialog-actions>
  `,
  styles: `
    .prod { display: flex; gap: 12px; align-items: center; margin-bottom: 12px; }
    .prod img { width: 56px; height: 56px; border-radius: 8px; object-fit: cover; }
    .stars-pick { display: flex; align-items: center; gap: 4px; margin-bottom: 16px; }
    .stars-pick mat-icon { font-size: 36px; width: 36px; height: 36px; color: #ffb300; cursor: pointer; }
    .txt { margin-left: 8px; font-weight: 700; color: var(--tl-orange); }
  `,
})
export class ReviewDialogComponent {
  item = inject<OrderItem>(MAT_DIALOG_DATA);
  private api = inject(ShopApi);
  private notify = inject(NotifyService);
  private ref = inject(MatDialogRef<ReviewDialogComponent>);
  rating = signal(5);
  hover = signal(0);
  comment = '';
  images: string[] = [];
  saving = signal(false);
  text = RATING_TEXT;
  ph = PLACEHOLDER_IMG;

  submit(): void {
    this.saving.set(true);
    this.api.review({ orderItemId: this.item.id, rating: this.rating(), comment: this.comment.trim() || null, imageUrls: this.images }).subscribe({
      next: (r) => {
        if (r.pendingModeration) this.notify.info('Cảm ơn bạn! Đánh giá có nội dung cần kiểm duyệt nên sẽ hiển thị sau khi cửa hàng duyệt.');
        else this.notify.success('Cảm ơn bạn đã đánh giá sản phẩm!');
        this.ref.close(true);
      },
      error: (e) => {
        this.saving.set(false);
        this.notify.error(e);
      },
    });
  }
}

/** Yeu cau tra hang / hoan tien */
@Component({
  selector: 'tl-return-dialog',
  imports: [FormsModule, MatDialogModule, MatButtonModule, MatFormFieldModule, MatInputModule, MatSelectModule, ImageUploadComponent],
  template: `
    <h2 mat-dialog-title>Yêu cầu trả hàng / hoàn tiền</h2>
    <mat-dialog-content>
      <p class="muted small">Đơn {{ order.orderCode }} · Áp dụng trong 7 ngày kể từ khi nhận hàng. Vui lòng đính kèm ảnh tình trạng sản phẩm.</p>
      <mat-form-field class="full"><mat-label>Lý do</mat-label>
        <mat-select [(ngModel)]="reason">@for (r of reasons; track r) { <mat-option [value]="r">{{ r }}</mat-option> }</mat-select>
      </mat-form-field>
      <mat-form-field class="full"><mat-label>Mô tả chi tiết</mat-label>
        <textarea matInput rows="3" maxlength="1000" [(ngModel)]="description"></textarea>
      </mat-form-field>
      <tl-image-upload [(urls)]="images" folder="returns" [max]="5" />
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close>Huỷ</button>
      <button mat-flat-button [disabled]="saving() || !reason" (click)="submit()">Gửi yêu cầu</button>
    </mat-dialog-actions>
  `,
})
export class ReturnDialogComponent {
  order = inject<Order>(MAT_DIALOG_DATA);
  private api = inject(ShopApi);
  private notify = inject(NotifyService);
  private ref = inject(MatDialogRef<ReturnDialogComponent>);
  reasons = RETURN_REASONS;
  reason = RETURN_REASONS[0];
  description = '';
  images: string[] = [];
  saving = signal(false);

  submit(): void {
    this.saving.set(true);
    this.api.returnOrder(this.order.id, { reason: this.reason, description: this.description.trim() || null, imageUrls: this.images }).subscribe({
      next: (o) => {
        this.notify.success('Đã gửi yêu cầu trả hàng. ToyLand sẽ phản hồi trong 24 giờ.');
        this.ref.close(o);
      },
      error: (e) => {
        this.saving.set(false);
        this.notify.error(e);
      },
    });
  }
}
