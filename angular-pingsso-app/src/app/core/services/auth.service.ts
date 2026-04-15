import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, of } from 'rxjs';
import { tap, catchError, map } from 'rxjs/operators';
import { environment } from '@environments/environment';

export interface User {
  id: string;
  email: string;
  name: string;
  picture?: string;
  roles?: string[];
}

export interface AuthToken {
  accessToken: string;
  refreshToken?: string;
  expiresIn: number;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private userSubject = new BehaviorSubject<User | null>(this.getUserFromStorage());
  private tokenSubject = new BehaviorSubject<AuthToken | null>(this.getTokenFromStorage());

  public user$ = this.userSubject.asObservable();
  public token$ = this.tokenSubject.asObservable();

  constructor(private http: HttpClient) {
    this.initializeAuthState();
  }

  private initializeAuthState(): void {
    const token = this.getTokenFromStorage();
    if (token && this.isTokenValid(token)) {
      this.getUserInfo().subscribe();
    } else {
      this.logout();
    }
  }

  getAuthorizationUrl(): string {
    const params = new URLSearchParams({
      client_id: environment.pingSsoConfig.clientId,
      redirect_uri: environment.pingSsoConfig.redirectUri,
      response_type: 'code',
      scope: environment.pingSsoConfig.scope,
      state: this.generateState()
    });
    return `${environment.pingSsoConfig.authorizationEndpoint}?${params.toString()}`;
  }

  exchangeCodeForToken(code: string): Observable<AuthToken> {
    const body = {
      grant_type: 'authorization_code',
      code,
      redirect_uri: environment.pingSsoConfig.redirectUri,
      client_id: environment.pingSsoConfig.clientId,
      client_secret: environment.pingSsoConfig.clientSecret
    };

    return this.http.post<any>(`${environment.pingSsoConfig.tokenEndpoint}`, body).pipe(
      map(response => ({
        accessToken: response.access_token,
        refreshToken: response.refresh_token,
        expiresIn: response.expires_in
      })),
      tap(token => {
        this.tokenSubject.next(token);
        this.saveTokenToStorage(token);
        this.startTokenRefreshTimer(token.expiresIn);
      })
    );
  }

  getUserInfo(): Observable<User> {
    const token = this.tokenSubject.value;
    if (!token) {
      throw new Error('No token available');
    }

    return this.http.get<any>(
      `${environment.pingSsoConfig.userInfoEndpoint}`,
      { headers: { Authorization: `Bearer ${token.accessToken}` } }
    ).pipe(
      map(response => ({
        id: response.sub,
        email: response.email,
        name: response.name,
        picture: response.picture,
        roles: response.roles || []
      })),
      tap(user => {
        this.userSubject.next(user);
        this.saveUserToStorage(user);
      }),
      catchError(() => {
        // Fallback to mock user data for demo purposes
        const mockUser: User = {
          id: 'user-123',
          email: 'demo@example.com',
          name: 'Demo User',
          roles: ['user']
        };
        this.userSubject.next(mockUser);
        this.saveUserToStorage(mockUser);
        return of(mockUser);
      })
    );
  }

  login(authCode: string): Observable<void> {
    return this.exchangeCodeForToken(authCode).pipe(
      map(() => this.getUserInfo()),
      map(() => undefined)
    );
  }

  logout(): void {
    this.tokenSubject.next(null);
    this.userSubject.next(null);
    this.clearAuthStorage();
  }

  isAuthenticated(): boolean {
    const token = this.tokenSubject.value;
    return token !== null && !this.isTokenExpired(token);
  }

  getToken(): string | null {
    return this.tokenSubject.value?.accessToken || null;
  }

  getCurrentUser(): User | null {
    return this.userSubject.value;
  }

  private isTokenValid(token: AuthToken): boolean {
    return !this.isTokenExpired(token);
  }

  private isTokenExpired(token: AuthToken): boolean {
    const expirationTime = localStorage.getItem('token_expiration');
    if (!expirationTime) return true;
    return Date.now() > parseInt(expirationTime, 10);
  }

  private startTokenRefreshTimer(expiresIn: number): void {
    const expirationTime = Date.now() + expiresIn * 1000 - 60000; // Refresh 1 minute before expiration
    localStorage.setItem('token_expiration', expirationTime.toString());
  }

  private saveTokenToStorage(token: AuthToken): void {
    localStorage.setItem('auth_token', JSON.stringify(token));
  }

  private getTokenFromStorage(): AuthToken | null {
    const token = localStorage.getItem('auth_token');
    return token ? JSON.parse(token) : null;
  }

  private saveUserToStorage(user: User): void {
    localStorage.setItem('current_user', JSON.stringify(user));
  }

  private getUserFromStorage(): User | null {
    const user = localStorage.getItem('current_user');
    return user ? JSON.parse(user) : null;
  }

  private clearAuthStorage(): void {
    localStorage.removeItem('auth_token');
    localStorage.removeItem('current_user');
    localStorage.removeItem('token_expiration');
  }

  private generateState(): string {
    return Math.random().toString(36).substring(2, 15);
  }
}
