import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, finalize, map, shareReplay, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { AuthResponse, MessageResponse, UserSummary, VerifyEmailResponse } from './models';

const KEY = 'toyland_session';

interface Session {
  accessToken: string;
  refreshToken: string;
  user: UserSummary;
}

/** Dang nhap / dang ky / refresh token (luu localStorage). */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);
  private api = `${environment.apiUrl}/auth`;
  private refreshing$: Observable<string> | null = null;

  readonly session = signal<Session | null>(this.load());
  readonly user = computed(() => this.session()?.user ?? null);
  readonly isLoggedIn = computed(() => this.session() !== null);
  readonly isStaff = computed(() => ['ADMIN', 'STAFF'].includes(this.session()?.user.role ?? ''));
  readonly isAdmin = computed(() => this.session()?.user.role === 'ADMIN');
  /** Khach hang chua xac nhan email (nhan vien / quan tri luon coi nhu da xac nhan) */
  readonly needsEmailVerification = computed(() => {
    const u = this.session()?.user;
    return !!u && u.role === 'CUSTOMER' && u.emailVerified === false;
  });

  constructor() {
    // Dong bo giua cac tab: xac nhan email / dang xuat o tab khac -> tab nay cap nhat theo
    window.addEventListener('storage', (e) => {
      if (e.key === KEY) this.session.set(this.load());
    });
  }

  get accessToken(): string | null {
    return this.session()?.accessToken ?? null;
  }

  login(username: string, password: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.api}/login`, { username, password }).pipe(tap((r) => this.save(r)));
  }

  register(body: { username: string; password: string; fullName: string; email: string; phone?: string; agreeTerms: boolean }) {
    return this.http.post<AuthResponse>(`${this.api}/register`, body).pipe(tap((r) => this.save(r)));
  }

  forgotPassword(email: string) {
    return this.http.post<MessageResponse>(`${this.api}/forgot-password`, { email });
  }

  resetPassword(token: string, newPassword: string) {
    return this.http.post<MessageResponse>(`${this.api}/reset-password`, { token, newPassword });
  }

  /** Bam link trong email: xac nhan (khong can dang nhap); neu dang dang nhap dung tai khoan do -> cap nhat ngay */
  verifyEmail(token: string) {
    return this.http.post<VerifyEmailResponse>(`${this.api}/verify-email`, { token }).pipe(
      tap((r) => {
        const u = this.user();
        if (u && u.email.toLowerCase() === r.email.toLowerCase()) this.updateUser({ emailVerified: true });
      }),
    );
  }

  resendVerification() {
    return this.http.post<MessageResponse>(`${this.api}/resend-verification`, {});
  }

  /** Lay lai thong tin tai khoan tu server (vd trang thai xac nhan email) */
  refreshMe(): void {
    if (!this.session()) return;
    this.http.get<UserSummary>(`${this.api}/me`).subscribe({ next: (u) => this.updateUser(u), error: () => {} });
  }

  /** Doi refresh token lay access token moi; nhieu request 401 cung luc chi goi 1 lan. */
  refresh(): Observable<string> {
    const s = this.session();
    if (!s) throw new Error('no session');
    if (!this.refreshing$) {
      this.refreshing$ = this.http.post<AuthResponse>(`${this.api}/refresh`, { refreshToken: s.refreshToken }).pipe(
        tap((r) => this.save(r)),
        map((r) => r.accessToken),
        finalize(() => (this.refreshing$ = null)),
        shareReplay(1),
      );
    }
    return this.refreshing$;
  }

  updateUser(patch: Partial<UserSummary>): void {
    const s = this.session();
    if (s) this.persist({ ...s, user: { ...s.user, ...patch } });
  }

  logout(redirect = true, expired = false): void {
    const s = this.session();
    if (s) this.http.post(`${this.api}/logout`, { refreshToken: s.refreshToken }).subscribe({ error: () => {} });
    localStorage.removeItem(KEY);
    this.session.set(null);
    if (redirect) this.router.navigate(['/login'], expired ? { queryParams: { expired: 1 } } : {});
  }

  private save(r: AuthResponse): void {
    this.persist({ accessToken: r.accessToken, refreshToken: r.refreshToken, user: r.user });
  }

  private persist(s: Session): void {
    localStorage.setItem(KEY, JSON.stringify(s));
    this.session.set(s);
  }

  private load(): Session | null {
    try {
      const raw = localStorage.getItem(KEY);
      return raw ? (JSON.parse(raw) as Session) : null;
    } catch {
      return null;
    }
  }
}
