import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatTooltipModule } from '@angular/material/tooltip';
import { AdminApi } from '../../core/admin-api.service';
import { PartnerCreated, PartnerRow } from '../../core/models';
import { NotifyService } from '../../core/notify.service';
import { openForm } from '../../shared/form-dialog.component';
import { environment } from '../../../environments/environment';

/** Hop thoai hien API key + secret ngay sau khi tao. Secret khong xem lai duoc. */
@Component({
  selector: 'tl-partner-secret-dialog',
  imports: [MatDialogModule, MatButtonModule, MatIconModule],
  template: `
    <h2 mat-dialog-title>Đã tạo đối tác "{{ data.name }}"</h2>
    <mat-dialog-content>
      <p class="warn"><mat-icon>warning</mat-icon>Secret chỉ hiện một lần. Hãy sao chép và gửi cho đối tác ngay, đóng hộp thoại này là không xem lại được.</p>
      <label>API key</label>
      <div class="box"><code>{{ data.apiKey }}</code><button mat-icon-button (click)="copy(data.apiKey)" aria-label="Sao chép API key"><mat-icon>content_copy</mat-icon></button></div>
      <label>Secret</label>
      <div class="box"><code>{{ data.secret }}</code><button mat-icon-button (click)="copy(data.secret)" aria-label="Sao chép secret"><mat-icon>content_copy</mat-icon></button></div>
    </mat-dialog-content>
    <mat-dialog-actions align="end"><button mat-flat-button mat-dialog-close>Đã lưu lại</button></mat-dialog-actions>
  `,
  styles: `
    .warn { display: flex; gap: 8px; align-items: flex-start; background: #fff4e5; color: #7a4a00; padding: 10px 12px; border-radius: 8px; font-size: 13px; }
    label { display: block; margin: 12px 0 4px; font-size: 12px; font-weight: 700; color: var(--tl-muted, #666); }
    .box { display: flex; align-items: center; justify-content: space-between; gap: 8px; border: 1px solid var(--tl-border, #ddd); border-radius: 8px; padding: 4px 4px 4px 12px; min-width: 420px; }
    code { word-break: break-all; font-size: 13px; }
    @media (max-width: 600px) { .box { min-width: 0; } }
  `,
})
export class PartnerSecretDialogComponent {
  data = inject<PartnerCreated>(MAT_DIALOG_DATA);
  private notify = inject(NotifyService);
  copy(v: string): void {
    navigator.clipboard?.writeText(v).then(() => this.notify.info('Đã sao chép'), () => this.notify.error('Không sao chép được, hãy bôi đen và chép tay'));
  }
}

@Component({
  selector: 'tl-partners',
  imports: [DatePipe, MatButtonModule, MatIconModule, MatSlideToggleModule, MatTooltipModule],
  template: `
    <div class="page-title"><h1>Đối tác & API</h1><span class="spacer"></span>
      <button mat-flat-button (click)="create()"><mat-icon>add</mat-icon>Thêm đối tác</button></div>
    <p class="muted">Bên thứ ba gọi dịch vụ của ToyLand bằng API key và chữ ký HMAC-SHA256. Hợp đồng REST nằm ở
      <a [href]="swagger" target="_blank" rel="noopener">Swagger UI</a>, hợp đồng SOAP ở <a [href]="wsdl" target="_blank" rel="noopener">WSDL</a>.</p>
    <div class="card">
      <table class="tbl">
        <thead><tr><th>Đối tác</th><th>API key</th><th>Hạn mức</th><th>Lần gọi gần nhất</th><th>Hoạt động</th></tr></thead>
        <tbody>
          @for (p of list(); track p.id) {
            <tr [class.off]="!p.active">
              <td><b>{{ p.name }}</b></td>
              <td><code>{{ p.apiKey }}</code></td>
              <td>{{ p.rateLimitPerMin }} yêu cầu/phút</td>
              <td>{{ p.lastUsedAt ? (p.lastUsedAt | date: 'dd/MM/yyyy HH:mm') : 'Chưa dùng' }}</td>
              <td><mat-slide-toggle [checked]="p.active" (change)="toggle(p, $event.checked)" [attr.aria-label]="'Bật tắt ' + p.name" /></td>
            </tr>
          } @empty {
            <tr><td colspan="5" class="muted center">Chưa có đối tác nào.</td></tr>
          }
        </tbody>
      </table>
    </div>
  `,
  styles: `
    .tbl { width: 100%; border-collapse: collapse; }
    .tbl th, .tbl td { text-align: left; padding: 10px 12px; border-bottom: 1px solid var(--tl-border, #eee); }
    .tbl th { font-size: 12px; text-transform: uppercase; color: var(--tl-muted, #666); }
    tr.off { opacity: .55; } code { font-size: 12px; } .center { text-align: center !important; }
  `,
})
export class PartnersComponent implements OnInit {
  private api = inject(AdminApi);
  private dialog = inject(MatDialog);
  private notify = inject(NotifyService);
  list = signal<PartnerRow[]>([]);
  private root = environment.apiUrl.replace(/\/api\/v1\/?$/, '');
  swagger = `${this.root}/swagger-ui.html`;
  wsdl = `${this.root}/soap/partner/partner.wsdl`;

  ngOnInit(): void { this.load(); }

  load(): void { this.api.partners().subscribe((l) => this.list.set(l)); }

  create(): void {
    openForm(this.dialog, {
      title: 'Thêm đối tác',
      value: { name: '', rateLimitPerMin: 60 },
      fields: [
        { key: 'name', label: 'Tên đối tác', required: true, wide: true },
        { key: 'rateLimitPerMin', label: 'Hạn mức (yêu cầu/phút)', type: 'number', required: true, hint: 'Từ 1 đến 6000' },
      ],
      save: (x) => this.api.createPartner(x),
    }).subscribe((r) => {
      if (r && typeof r === 'object') {
        this.dialog.open(PartnerSecretDialogComponent, { data: r, disableClose: true, maxWidth: '96vw' });
      }
      this.load();
    });
  }

  toggle(p: PartnerRow, active: boolean): void {
    this.api.setPartnerActive(p.id, active).subscribe({
      next: () => { this.notify.success(active ? 'Đã bật đối tác' : 'Đã tắt đối tác'); this.load(); },
      error: (e) => { this.notify.error(e); this.load(); },
    });
  }
}
