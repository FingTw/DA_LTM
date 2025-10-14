import { Component, OnInit, OnDestroy, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { WebSocketService } from '../../services/websocket.service';
import { User } from '../../models/user.model';
import { ChatComponent } from '../chat/chat.component';
import { FriendComponent } from '../friend/friend.component';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, ChatComponent, FriendComponent],
  styleUrls: ['./dashboard.component.css'],
  template: `
    <div class="dashboard-container">
      <!-- Header -->
      <header class="header">
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div class="flex justify-between items-center h-16">
            <!-- Logo -->
            <div class="flex items-center">
              <div class="flex-shrink-0">
                <img src="/assets/logo.svg" alt="Allies" class="h-8 w-auto" />
              </div>
            </div>

            <!-- Search -->
            <div class="flex-1 max-w-lg mx-8">
              <div class="relative">
                <input
                  type="search"
                  placeholder="Search messages or friends..."
                  class="w-full py-2 pl-10 pr-4 rounded-full bg-gray-100 focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
                <span
                  class="material-icons absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400"
                >
                  search
                </span>
              </div>
            </div>

            <!-- User Menu -->
            <div class="flex items-center space-x-4">
              <button class="relative p-2 text-gray-400 hover:text-gray-500">
                <span class="material-icons">notifications</span>
                @if (notificationCount() > 0) {
                <span
                  class="absolute -top-1 -right-1 bg-red-500 text-white text-xs rounded-full h-5 w-5 flex items-center justify-center"
                >
                  {{ notificationCount() }}
                </span>
                }
              </button>
              <div class="relative">
                <button class="flex items-center space-x-2" (click)="toggleUserMenu()">
                  <img
                    [src]="currentUser()?.avatar || '/assets/default-avatar.png'"
                    alt="Profile"
                    class="w-8 h-8 rounded-full"
                  />
                  <span class="material-icons">expand_more</span>
                </button>
                <div
                  *ngIf="showUserMenu()"
                  class="absolute right-0 mt-2 w-48 bg-white rounded-md shadow-lg py-1"
                >
                  <button class="nav-button w-full text-left" (click)="logout()">
                    <span class="material-icons mr-2">logout</span>
                    Logout
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </header>

      <!-- Main Content -->
      <div class="flex">
        <!-- Sidebar -->
        <aside class="sidebar">
          <nav class="space-y-1 p-4">
            <button
              (click)="setActiveTab('chat')"
              class="nav-button"
              [class.active]="activeTab() === 'chat'"
            >
              <span class="material-icons">chat</span>
              Messages
            </button>
            <button
              (click)="setActiveTab('friends')"
              class="nav-button"
              [class.active]="activeTab() === 'friends'"
            >
              <span class="material-icons">people</span>
              Friends
            </button>
            <button
              (click)="setActiveTab('calls')"
              class="nav-button"
              [class.active]="activeTab() === 'calls'"
            >
              <span class="material-icons">call</span>
              Calls
            </button>
            <button
              (click)="setActiveTab('settings')"
              class="nav-button"
              [class.active]="activeTab() === 'settings'"
            >
              <span class="material-icons">settings</span>
              Settings
            </button>
          </nav>

          <!-- User Profile -->
          <div class="user-profile">
            <img
              [src]="currentUser()?.avatar || '/assets/default-avatar.png'"
              alt="Profile"
              class="user-avatar"
            />
            <div class="user-info">
              <div class="user-name">{{ currentUser()?.username }}</div>
              <div class="user-status">Online</div>
            </div>
            <button class="logout-button" (click)="logout()">
              <span class="material-icons">logout</span>
            </button>
          </div>
        </aside>

        <!-- Main Content -->
        <main class="main-content">
          @if (activeTab() === 'chat') {
          <app-chat></app-chat>
          } @else if (activeTab() === 'friends') {
          <app-friend></app-friend>
          } @else if (activeTab() === 'calls') {
          <div class="bg-white rounded-lg shadow">
            <div class="px-6 py-4 border-b border-gray-200">
              <h2 class="text-lg font-semibold text-gray-900">Call History</h2>
            </div>
            <div class="p-6">
              <div class="text-center text-gray-500">
                <span class="material-icons text-4xl mb-4 block">phone</span>
                <p>No calls yet</p>
              </div>
            </div>
          </div>
          } @else if (activeTab() === 'settings') {
          <div class="bg-white rounded-lg shadow">
            <div class="px-6 py-4 border-b border-gray-200">
              <h2 class="text-lg font-semibold text-gray-900">Settings</h2>
            </div>
            <div class="p-6">
              <p class="text-gray-500">Settings content goes here</p>
            </div>
          </div>
          }
        </main>
      </div>
    </div>
  `,
})
export class DashboardComponent implements OnInit, OnDestroy {
  activeTab = signal<'chat' | 'friends' | 'calls' | 'settings'>('chat');
  currentUser = signal<User | null>(null);
  notificationCount = signal(0);
  showUserMenu = signal(false);

  constructor(
    private authService: AuthService,
    private webSocketService: WebSocketService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.currentUser.set(this.authService.getCurrentUser());

    if (!this.currentUser()) {
      this.router.navigate(['/login']);
      return;
    }

    this.webSocketService.connect();
    if (this.currentUser()?.username) {
      this.webSocketService.subscribeToUserQueue(this.currentUser()!.username);
    }
  }

  ngOnDestroy(): void {
    this.webSocketService.disconnect();
  }

  setActiveTab(tab: 'chat' | 'friends' | 'calls' | 'settings'): void {
    this.activeTab.set(tab);
  }

  toggleUserMenu(): void {
    this.showUserMenu.set(!this.showUserMenu());
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  getInitials(name: string): string {
    return name
      .split(' ')
      .map((n) => n[0])
      .join('')
      .toUpperCase();
  }
}
