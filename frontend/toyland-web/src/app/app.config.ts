import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { ApplicationConfig, LOCALE_ID, inject, provideAppInitializer, provideZoneChangeDetection } from '@angular/core';
import { registerLocaleData } from '@angular/common';
import localeVi from '@angular/common/locales/vi';
import { MAT_DATE_LOCALE, provideNativeDateAdapter } from '@angular/material/core';
import { MAT_FORM_FIELD_DEFAULT_OPTIONS } from '@angular/material/form-field';
import { MatIconRegistry } from '@angular/material/icon';
import { MatPaginatorIntl } from '@angular/material/paginator';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { provideRouter, withComponentInputBinding, withInMemoryScrolling } from '@angular/router';
import { provideCharts, withDefaultRegisterables } from 'ng2-charts';

import { routes } from './app.routes';
import { authInterceptor, loadingInterceptor } from './core/interceptors';
import { VietnamesePaginatorIntl } from './core/paginator-intl';

registerLocaleData(localeVi);

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    // Mot bo icon duy nhat cho toan he thong: Material Icons Round
    provideAppInitializer(() => { inject(MatIconRegistry).setDefaultFontSetClass('material-icons-round', 'mat-ligature-font'); }),
    provideRouter(routes, withComponentInputBinding(), withInMemoryScrolling({ scrollPositionRestoration: 'top', anchorScrolling: 'enabled' })),
    provideHttpClient(withInterceptors([loadingInterceptor, authInterceptor])),
    provideAnimationsAsync(),
    provideCharts(withDefaultRegisterables()),
    provideNativeDateAdapter(),
    { provide: LOCALE_ID, useValue: 'vi' },
    { provide: MAT_DATE_LOCALE, useValue: 'vi-VN' },
    { provide: MatPaginatorIntl, useClass: VietnamesePaginatorIntl },
    { provide: MAT_FORM_FIELD_DEFAULT_OPTIONS, useValue: { appearance: 'outline' } },
  ],
};
