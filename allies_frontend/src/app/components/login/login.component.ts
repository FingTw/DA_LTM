import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { LoginRequest } from '../../models/user.model';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  styleUrls: ['./login.component.css'],
  template: `
    <div class="login-container">
      <div class="login-card">
        <div class="brand-title">
          <img src="/assets/logo.svg" alt="Allies Logo" class="mx-auto h-12 mb-4" />
          <h1>Welcome to Allies</h1>
          <p class="text-sm text-gray-300">Connect with friends and colleagues</p>
        </div>

        <form (ngSubmit)="onSubmit()" class="space-y-6 mt-8">
          <div class="form-group">
            <label for="username" class="form-label">Username</label>
            <input
              id="username"
              name="username"
              type="text"
              [(ngModel)]="loginForm.username"
              required
              class="form-input"
              placeholder="Enter your username"
              [class.error]="submitted && !loginForm.username"
            />
          </div>

          <div class="form-group">
            <label for="password" class="form-label">Password</label>
            <div class="relative">
              <input
                id="password"
                name="password"
                [type]="showPassword ? 'text' : 'password'"
                [(ngModel)]="loginForm.password"
                required
                class="form-input"
                placeholder="Enter your password"
                [class.error]="submitted && !loginForm.password"
              />
              <button
                type="button"
                class="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 hover:text-gray-300"
                (click)="togglePasswordVisibility()"
              >
                <i [class]="showPassword ? 'fas fa-eye-slash' : 'fas fa-eye'"></i>
              </button>
            </div>
          </div>

          <div *ngIf="errorMessage()" class="error-message">
            {{ errorMessage() }}
          </div>

          <button type="submit" [disabled]="isLoading()" class="btn btn-primary w-full py-2">
            <span *ngIf="isLoading(); else signInText" class="animate-pulse">Signing in...</span>
            <ng-template #signInText>Sign In</ng-template>
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
  submitted = false;
  showPassword = false;

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

  togglePasswordVisibility(): void {
    this.showPassword = !this.showPassword;
  }
}
