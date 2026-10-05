import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, finalize, switchMap, throwError } from 'rxjs';
import { AuthService } from './auth.service';
import { LoadingService } from './loading.service';

/**
 * Gan "Authorization: Bearer <access token>".
 * Neu nhan 401 (access token 15 phut het han) -> tu dong goi /auth/refresh roi gui lai request.
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const isAuthCall = req.url.includes('/auth/');
  const token = auth.accessToken;
  const withToken = token && !isAuthCall ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : req;

  return next(withToken).pipe(
    catchError((err: HttpErrorResponse) => {
      if (err.status === 401 && token && !isAuthCall) {
        return auth.refresh().pipe(
          switchMap((newToken) => next(req.clone({ setHeaders: { Authorization: `Bearer ${newToken}` } }))),
          catchError((e) => {
            auth.logout(true, true);
            return throwError(() => e);
          }),
        );
      }
      return throwError(() => err);
    }),
  );
};

/** Hien thanh tien trinh chung khi dang goi API */
export const loadingInterceptor: HttpInterceptorFn = (req, next) => {
  const loading = inject(LoadingService);
  if (req.headers.has('X-Silent')) {
    return next(req.clone({ headers: req.headers.delete('X-Silent') }));
  }
  loading.start();
  return next(req).pipe(finalize(() => loading.stop()));
};
