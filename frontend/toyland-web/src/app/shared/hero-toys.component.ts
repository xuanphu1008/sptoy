import { Component } from '@angular/core';

/**
 * Minh hoa hero "do choi dat nan" (SVG tu ve, khong dung anh ban quyen):
 * 3 khoi chu T-O-Y, qua bong soc, ngoi sao, con quay, dam may. Moi nhom troi nhe voi nhip khac nhau.
 */
@Component({
  selector: 'tl-hero-toys',
  template: `
    <svg viewBox="0 0 520 440" role="img" aria-label="Minh hoạ khối đồ chơi TOY, quả bóng và ngôi sao">
      <defs>
        <radialGradient id="ht-red" cx="35%" cy="25%" r="85%"><stop offset="0" stop-color="#ffa196"/><stop offset=".55" stop-color="#f45a52"/><stop offset="1" stop-color="#d43d3a"/></radialGradient>
        <radialGradient id="ht-blue" cx="35%" cy="25%" r="85%"><stop offset="0" stop-color="#b9dcff"/><stop offset=".55" stop-color="#6aa9f5"/><stop offset="1" stop-color="#3f7fd6"/></radialGradient>
        <radialGradient id="ht-yellow" cx="35%" cy="25%" r="85%"><stop offset="0" stop-color="#fff0b0"/><stop offset=".55" stop-color="#ffcf4d"/><stop offset="1" stop-color="#f0a92a"/></radialGradient>
        <radialGradient id="ht-mint" cx="35%" cy="30%" r="80%"><stop offset="0" stop-color="#d9fff0"/><stop offset=".6" stop-color="#7fe0b7"/><stop offset="1" stop-color="#3fb488"/></radialGradient>
        <radialGradient id="ht-lilac" cx="35%" cy="30%" r="80%"><stop offset="0" stop-color="#f1ebff"/><stop offset=".6" stop-color="#b9a4ff"/><stop offset="1" stop-color="#8a6fe8"/></radialGradient>
        <radialGradient id="ht-shadow" cx="50%" cy="50%" r="50%"><stop offset="0" stop-color="#2b2350" stop-opacity=".22"/><stop offset="1" stop-color="#2b2350" stop-opacity="0"/></radialGradient>
        <clipPath id="ht-ball"><circle cx="0" cy="0" r="52"/></clipPath>
      </defs>

      <!-- nen: vong tron pastel + cham bi -->
      <circle cx="270" cy="220" r="190" fill="#fff" opacity=".55"/>
      <g fill="#2b2350" opacity=".08">
        <circle cx="90" cy="80" r="5"/><circle cx="120" cy="60" r="3"/><circle cx="470" cy="330" r="5"/><circle cx="440" cy="360" r="3"/><circle cx="60" cy="300" r="4"/>
      </g>

      <!-- bong do duoi chan -->
      <ellipse cx="250" cy="392" rx="190" ry="22" fill="url(#ht-shadow)"/>

      <!-- Khoi T (do) -->
      <g class="fl f1">
        <g transform="translate(118 250) rotate(-6)">
          <rect x="0" y="0" width="128" height="128" rx="30" fill="url(#ht-red)"/>
          <rect x="12" y="10" width="104" height="22" rx="11" fill="#fff" opacity=".35"/>
          <text x="64" y="98" text-anchor="middle" font-family="'Baloo 2', Nunito, sans-serif" font-weight="800" font-size="86" fill="#fff">T</text>
        </g>
      </g>
      <!-- Khoi O (xanh) -->
      <g class="fl f2">
        <g transform="translate(262 262) rotate(5)">
          <rect x="0" y="0" width="120" height="120" rx="30" fill="url(#ht-blue)"/>
          <rect x="12" y="10" width="96" height="20" rx="10" fill="#fff" opacity=".35"/>
          <text x="60" y="92" text-anchor="middle" font-family="'Baloo 2', Nunito, sans-serif" font-weight="800" font-size="82" fill="#fff">O</text>
        </g>
      </g>
      <!-- Khoi Y (vang) nam tren -->
      <g class="fl f3">
        <g transform="translate(196 132) rotate(-3)">
          <rect x="0" y="0" width="118" height="118" rx="30" fill="url(#ht-yellow)"/>
          <rect x="12" y="10" width="94" height="20" rx="10" fill="#fff" opacity=".45"/>
          <text x="59" y="90" text-anchor="middle" font-family="'Baloo 2', Nunito, sans-serif" font-weight="800" font-size="80" fill="#fff" stroke="#e9a126" stroke-width="2">Y</text>
        </g>
      </g>

      <!-- Qua bong soc -->
      <g class="fl f4">
        <g transform="translate(436 318)">
          <circle r="52" fill="url(#ht-mint)"/>
          <g clip-path="url(#ht-ball)" opacity=".9">
            <rect x="-60" y="-14" width="120" height="16" fill="#fff" opacity=".85" transform="rotate(-25)"/>
            <rect x="-60" y="14" width="120" height="12" fill="#ff8fab" transform="rotate(-25)"/>
          </g>
          <ellipse cx="-18" cy="-24" rx="14" ry="8" fill="#fff" opacity=".7" transform="rotate(-30)"/>
        </g>
      </g>

      <!-- Ngoi sao -->
      <g class="fl f5">
        <g transform="translate(420 96) rotate(12)">
          <path d="M0 -46 L13 -15 L46 -13 L20 8 L29 42 L0 23 L-29 42 L-20 8 L-46 -13 L-13 -15 Z" fill="url(#ht-yellow)" stroke="#fff" stroke-width="5" stroke-linejoin="round"/>
          <ellipse cx="-8" cy="-14" rx="8" ry="5" fill="#fff" opacity=".7"/>
        </g>
      </g>

      <!-- Con quay tim -->
      <g class="fl f6">
        <g transform="translate(86 190)">
          <path d="M0 -40 C 30 -40 44 -12 44 0 C 44 16 22 30 0 52 C -22 30 -44 16 -44 0 C -44 -12 -30 -40 0 -40 Z" fill="url(#ht-lilac)"/>
          <rect x="-5" y="-58" width="10" height="22" rx="5" fill="#8a6fe8"/>
          <ellipse cx="-14" cy="-18" rx="10" ry="6" fill="#fff" opacity=".6"/>
        </g>
      </g>

      <!-- Dam may -->
      <g class="fl f7">
        <g transform="translate(300 64)" fill="#fff">
          <ellipse cx="0" cy="0" rx="46" ry="22"/><circle cx="-18" cy="-12" r="20"/><circle cx="14" cy="-16" r="24"/>
        </g>
      </g>
    </svg>
  `,
  styles: `
    :host { display: block; }
    svg { width: 100%; height: auto; overflow: visible; }
    .fl { transform-box: fill-box; transform-origin: center; animation: tl-float 5s ease-in-out infinite; }
    .f1 { animation-delay: -.4s; } .f2 { animation-delay: -1.8s; animation-duration: 5.6s; } .f3 { animation-delay: -1s; animation-duration: 4.6s; }
    .f4 { animation-delay: -2.5s; animation-duration: 4.2s; } .f5 { animation-delay: -.8s; animation-duration: 3.8s; }
    .f6 { animation-delay: -3s; animation-duration: 6s; } .f7 { animation-delay: -1.5s; animation-duration: 7s; }
  `,
})
export class HeroToysComponent {}
