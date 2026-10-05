import { Component, computed, input } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';

/** Hien thi so sao (ho tro nua sao) */
@Component({
  selector: 'tl-stars',
  imports: [MatIconModule],
  template: `
    <span class="stars" [style.font-size.px]="size()">
      @for (s of icons(); track $index) {
        <mat-icon [style.font-size.px]="size()" [style.width.px]="size()" [style.height.px]="size()">{{ s }}</mat-icon>
      }
    </span>
  `,
})
export class StarsComponent {
  value = input(0);
  size = input(16);
  icons = computed(() => {
    const v = this.value() ?? 0;
    return [1, 2, 3, 4, 5].map((i) => (v >= i ? 'star' : v >= i - 0.5 ? 'star_half' : 'star_border'));
  });
}
