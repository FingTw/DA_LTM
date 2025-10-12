import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { User, LoginRequest, SignupRequest, AuthResponse } from '../models/user.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly API_URL = environment.apiUrl + '/auth';
  private currentUserSubject = new BehaviorSubject<User | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  public isAuthenticated = signal(false);

  constructor(private http: HttpClient) {
    this.loadStoredUser();
  }

  login(credentials: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.API_URL}/login`, credentials).pipe(
      tap((response) => {
        this.setCurrentUser({
          id: response.id,
          username: response.username,
          avatar: 'default-avatar.png',
        });
        localStorage.setItem('token', response.token);
        this.isAuthenticated.set(true);
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
      // In a real app, you'd decode the JWT to get user info
      // For now, we'll just set a default user
      this.setCurrentUser({
        id: 1,
        username: 'user',
        avatar: 'default-avatar.png',
      });
      this.isAuthenticated.set(true);
    }
  }

  getCurrentUser(): User | null {
    return this.currentUserSubject.value;
  }
}
