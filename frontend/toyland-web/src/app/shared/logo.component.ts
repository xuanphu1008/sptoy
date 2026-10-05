import { Component, input } from '@angular/core';

/**
 * Logo ToyLand: "khoi do choi" dat nan (SVG tu ve, khong ban quyen) + chu Baloo 2.
 * variant: 'light' (nen sang) | 'dark' (nen toi: chu trang)
 */
let seq = 0;

@Component({
  selector: 'tl-logo',
  template: `
    <span class="logo" [class.dark]="variant() === 'dark'" [style.--s.px]="size()">
      <svg class="mark" viewBox="0 0 48 48" aria-hidden="true">
        <defs>
          <linearGradient [attr.id]="uid + 'a'" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stop-color="#ff8a7d"/><stop offset="1" stop-color="#e5433f"/></linearGradient>
          <linearGradient [attr.id]="uid + 'b'" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stop-color="#ffe08a"/><stop offset="1" stop-color="#ffb938"/></linearGradient>
        </defs>
        <!-- khoi lego do -->
        <rect x="4" y="14" width="40" height="30" rx="10" [attr.fill]="'url(#' + uid + 'a)'"/>
        <rect x="10" y="8" width="10" height="9" rx="4" fill="#ff8a7d"/>
        <rect x="28" y="8" width="10" height="9" rx="4" fill="#ff8a7d"/>
        <rect x="6" y="16" width="36" height="6" rx="3" fill="#fff" opacity=".28"/>
        <!-- ngoi sao vang -->
        <path d="M24 22l3 6 6.5.9-4.7 4.6 1.1 6.5L24 37l-5.9 3 1.1-6.5-4.7-4.6L21 28z" [attr.fill]="'url(#' + uid + 'b)'" stroke="#fff" stroke-width="1.5" stroke-linejoin="round"/>
      </svg>
      @if (showText()) { <span class="word">Toy<b>Land</b></span> }
    </span>
  `,
  styles: `
    .logo { display: inline-flex; align-items: center; gap: 8px; --s: 40px; }
    .mark { width: var(--s); height: var(--s); flex: none; filter: drop-shadow(0 4px 6px rgb(229 67 63 / 30%)); transition: transform .3s cubic-bezier(.34,1.56,.64,1); }
    .logo:hover .mark { transform: rotate(-8deg) scale(1.06); }
    .word { font-family: 'Baloo 2', Nunito, sans-serif; font-weight: 800; font-size: calc(var(--s) * .66); letter-spacing: -.02em; color: #2b2350; line-height: 1; }
    .word b { color: #f0524f; }
    .dark .word { color: #fff; }
    .dark .word b { color: #ffc83d; }
  `,
})
export class LogoComponent {
  /** id gradient rieng moi instance: tranh trung id khi 1 logo nam trong vung display:none */
  readonly uid = 'tlg' + ++seq;
  size = input(40);
  showText = input(true);
  variant = input<'light' | 'dark'>('light');
}
