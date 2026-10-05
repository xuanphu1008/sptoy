import { Component, inject } from '@angular/core';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { RouterOutlet } from '@angular/router';
import { LoadingService } from './core/loading.service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, MatProgressBarModule],
  template: `
    @if (loading.active()) {
      <mat-progress-bar class="top-loader" mode="indeterminate" />
    }
    <router-outlet />
  `,
  styles: `.top-loader { position: fixed; top: 0; left: 0; right: 0; z-index: 2000; height: 3px; }`,
})
export class AppComponent {
  loading = inject(LoadingService);
}
