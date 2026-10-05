import { Component } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'tl-not-found',
  imports: [RouterLink, MatButtonModule, MatIconModule],
  template: `
    <div class="container page">
      <div class="card box">
        <div class="code">404</div>
        <mat-icon>toys</mat-icon>
        <h2>Ối! Món đồ chơi này đã trốn đi đâu mất rồi</h2>
        <p class="muted">Trang bạn tìm không tồn tại hoặc đã được di chuyển.</p>
        <a mat-flat-button routerLink="/">Về trang chủ</a>
      </div>
    </div>
  `,
  styles: `
    .box { text-align: center; max-width: 560px; margin: 0 auto; padding: 40px; }
    .code { font-size: 96px; font-weight: 900; color: var(--tl-red); line-height: 1; }
    mat-icon { font-size: 48px; width: 48px; height: 48px; color: var(--tl-yellow); }
  `,
})
export class NotFoundComponent {}
