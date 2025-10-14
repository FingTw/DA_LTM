import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, of } from 'rxjs';
import { tap, catchError } from 'rxjs/operators';
import { User, LoginRequest, SignupRequest, AuthResponse } from '../models/user.model';
import { environment } from '../../environments/environment';
import { Taikhoan } from '../models/user.model';
import { Router } from '@angular/router';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly API_URL = environment.apiUrl + '/auth';
  private currentUserSubject = new BehaviorSubject<User | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();
  public isAuthenticated = signal(false);

  constructor(private http: HttpClient, private router: Router) {
    this.loadStoredUser();
  }

  login(credentials: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.API_URL}/login`, credentials).pipe(
      tap((response) => {
        console.log('Login response:', response); // Debug
        this.fetchUserProfile(response.id).subscribe(
          (user) => {
            this.setCurrentUser({
              id: user.maTk,
              username: user.tenDn,
              avatar: user.avarta || 'default-avatar.png',
            });
            localStorage.setItem('token', response.token);
            this.isAuthenticated.set(true);
            this.router
              .navigate(['/dashboard'])
              .then(() => {
                console.log('Navigated to dashboard');
              })
              .catch((err) => console.error('Navigation error:', err));
          },
          (error) => console.error('Fetch user profile error:', error)
        );
      }),
      catchError((error) => {
        console.error('Login error:', error);
        return of(error); // Trả về lỗi để xử lý
      })
    );
  }

  signup(userData: SignupRequest): Observable<any> {
    return this.http.post(`${this.API_URL}/signup`, userData);
  }

  logout(): void {
    localStorage.removeItem('token');
    this.currentUserSubject.next(null);
    this.isAuthenticated.set(false);
    this.router.navigate(['/login']);
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }

  private setCurrentUser(user: User): void {
    this.currentUserSubject.next(user);
  }

  private loadStoredUser(): void {
    const token = this.getToken();
    if (token) {
      this.fetchUserProfileFromToken().subscribe(
        (user) => {
          if (user) {
            this.setCurrentUser({
              id: user.maTk,
              username: user.tenDn,
              avatar: user.avarta || 'default-avatar.png',
            });
            this.isAuthenticated.set(true);
            this.router.navigate(['/dashboard']).then(() => {
              console.log('Loaded user and navigated to dashboard');
            });
          }
        },
        (error) => {
          console.error('Load user error:', error);
          this.logout();
        }
      );
    }
  }

  getCurrentUser(): User | null {
    return this.currentUserSubject.value;
  }

  private fetchUserProfile(userId: number): Observable<Taikhoan> {
    return this.http
      .get<Taikhoan>(`${environment.apiUrl}/users/${userId}`, {
        headers: { Authorization: `Bearer ${this.getToken()}` },
      })
      .pipe(
        catchError((error) => {
          console.error('Fetch user profile error:', error);
          return of({ maTk: userId, tenDn: 'unknown', avarta: 'default-avatar.png' } as Taikhoan); // Fallback
        })
      );
  }

  private fetchUserProfileFromToken(): Observable<Taikhoan> {
    return this.http
      .get<Taikhoan>(`${environment.apiUrl}/auth/me`, {
        headers: { Authorization: `Bearer ${this.getToken()}` },
      })
      .pipe(
        catchError((error) => {
          console.error('Fetch user from token error:', error);
          return of({ maTk: 0, tenDn: 'unknown', avarta: 'default-avatar.png' } as Taikhoan); // Fallback
        })
      );
  }
}
