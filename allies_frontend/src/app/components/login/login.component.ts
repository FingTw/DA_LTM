import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { LoginRequest } from '../../models/user.model';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="min-h-screen flex items-center justify-center bg-gray-900">
      <div class="max-w-xl w-full space-y-8 p-6 bg-gray-800 rounded-lg shadow-lg mx-auto">
        <div class="text-center">
          <h2 class="text-3xl font-bold text-white mb-2">Welcome to Allies</h2>
          <p class="text-gray-200">Sign in to your account</p>
        </div>

        <div class="card">
          <form (ngSubmit)="onSubmit()" class="space-y-6">
            <div class="form-group">
              <label for="username" class="form-label text-white">Username</label>
              <input
                id="username"
                name="username"
                type="text"
                [(ngModel)]="loginForm.username"
                required
                class="form-input w-full"
                placeholder="Enter your username"
              />
            </div>

            <div class="form-group">
              <label for="password" class="form-label text-white">Password</label>
              <input
                id="password"
                name="password"
                type="password"
                [(ngModel)]="loginForm.password"
                required
                class="form-input w-full"
                placeholder="Enter your password"
              />
            </div>

            @if (errorMessage()) {
            <div class="text-error text-sm text-red-500">{{ errorMessage() }}</div>
            }

            <button type="submit" [disabled]="isLoading()" class="btn btn-primary w-full py-2">
              @if (isLoading()) {
              <span class="animate-pulse">Signing in...</span>
              } @else { Sign In }
            </button>
          </form>

          <div class="mt-6 text-center">
            <p class="text-gray-600">
              Don't have an account?
              <button (click)="toggleMode()" class="text-blue-400 hover:underline">Sign up</button>
            </p>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [
    `
      .min-h-screen {
        min-height: 100vh;
        width: 100%; /* Đảm bảo chiếm toàn bộ chiều rộng */
      }

      .w-full {
        width: 100%;
      }

      .max-w-xl {
        max-width: 36rem; /* Giữ nguyên kích thước "căng" */
      }

      .space-y-8 > * + * {
        margin-top: 2rem;
      }

      .space-y-6 > * + * {
        margin-top: 1.5rem;
      }

      .form-input {
        @apply w-full px-3 py-2 border border-gray-700 rounded-md bg-gray-700 text-white focus:outline-none focus:ring-2 focus:ring-blue-500;
      }

      .form-label {
        @apply block text-sm font-medium mb-1;
      }

      .form-group {
        @apply space-y-2;
      }

      .card {
        @apply bg-gray-800 p-6 rounded-lg shadow-md;
      }

      .btn {
        @apply font-medium rounded-md text-white focus:outline-none;
      }

      .btn-primary {
        @apply bg-blue-600 hover:bg-blue-700;
      }

      .btn:disabled {
        @apply bg-gray-500 cursor-not-allowed;
      }

      /* Thêm class mx-auto để căn giữa */
      .mx-auto {
        margin-left: auto;
        margin-right: auto;
      }
    `,
  ],
})
export class LoginComponent {
  loginForm: LoginRequest = { username: '', password: '' };
  isLoading = signal(false);
  errorMessage = signal('');
  isSignupMode = signal(false);

  constructor(private authService: AuthService, private router: Router) {}

  onSubmit(): void {
    if (this.isSignupMode()) {
      this.signup();
    } else {
      this.login();
    }
  }

  login(): void {
    this.isLoading.set(true);
    this.errorMessage.set('');

    this.authService.login(this.loginForm).subscribe({
      next: () => {
        this.router.navigate(['/dashboard']);
      },
      error: (error) => {
        this.errorMessage.set('Invalid username or password');
        this.isLoading.set(false);
      },
    });
  }

  signup(): void {
    this.isLoading.set(true);
    this.errorMessage.set('');

    this.authService.signup(this.loginForm).subscribe({
      next: () => {
        this.errorMessage.set('');
        this.login();
      },
      error: (error) => {
        this.errorMessage.set('Username already exists or invalid data');
        this.isLoading.set(false);
      },
    });
  }

  toggleMode(): void {
    this.isSignupMode.set(!this.isSignupMode());
    this.errorMessage.set('');
    this.loginForm = { username: '', password: '' };
  }
}
