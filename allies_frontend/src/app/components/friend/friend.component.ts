import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { FriendService } from '../../services/Friend.service';
import { AuthService } from '../../services/auth.service';
import { Taikhoan } from '../../models/user.model';

@Component({
  selector: 'app-friend',
  standalone: true,
  imports: [CommonModule, FormsModule],
  styleUrls: ['./friend.component.css'],
  template: `
    <div class="friend-container">
      <!-- Friend Requests Section -->
      <div class="friend-section" *ngIf="friendRequests().length > 0">
        <div class="section-header">
          <h2 class="section-title">Friend Requests</h2>
          <span class="badge">{{ friendRequests().length }}</span>
        </div>
        <div class="friend-requests">
          <div class="request-card" *ngFor="let request of friendRequests()">
            <div class="friend-avatar">
              {{ getInitials(request.tenDn) }}
            </div>
            <div class="friend-info">
              <div class="friend-name">{{ request.tenDn }}</div>
              <div class="friend-status">Wants to connect with you</div>
            </div>
            <div class="request-actions">
              <button class="accept-button" (click)="acceptFriendRequest(request.maTk)">
                Accept
              </button>
              <button class="reject-button" (click)="rejectFriendRequest(request.maTk)">
                Reject
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- Find Friends Section -->
      <div class="friend-section">
        <div class="section-header">
          <h2 class="section-title">Find Friends</h2>
        </div>
        <div class="search-container">
          <span class="material-icons search-icon">search</span>
          <input
            type="text"
            [(ngModel)]="searchQuery"
            placeholder="Search by username..."
            class="search-input"
            (input)="onSearch()"
          />
        </div>

        <!-- Search Results -->
        @if (searchResults().length > 0) {
        <div class="friends-grid">
          @for (user of searchResults(); track user.maTk) {
          <div class="friend-card">
            <div class="friend-header">
              <div class="friend-avatar">
                {{ getInitials(user.tenDn) }}
              </div>
              <div class="friend-info">
                <div class="friend-name">{{ user.tenDn }}</div>
                <div class="friend-status">{{ getRandomStatus() }}</div>
              </div>
            </div>
            <div class="friend-actions">
              <button class="action-button primary-button" (click)="addFriend(user.tenDn)">
                <span class="material-icons">person_add</span>
                Add Friend
              </button>
            </div>
          </div>
          }
        </div>
        } @else if (searchQuery.length > 0) {
        <div class="text-center text-gray-500">
          <span class="material-icons text-4xl mb-2">search_off</span>
          <p>No matching users found</p>
        </div>
        }
        <p class="text-gray-500 text-center mb-6">Nhập từ khóa để tìm kiếm.</p>

        <!-- Danh sách bạn bè -->
        <h3 class="text-xl font-semibold mb-3 text-gray-800">Danh sách bạn bè</h3>
        @if (friends().length > 0) {
        <ul class="space-y-3 mb-6">
          @for (friend of friends(); track friend.maTk) {
          <li
            class="p-3 border border-gray-200 rounded-md flex justify-between items-center bg-gray-50 hover:bg-gray-100 transition-colors"
          >
            <span class="font-medium text-gray-800">{{ friend.tenDn }}</span>
            <button
              class="bg-green-500 text-white px-3 py-1 rounded-md hover:bg-green-600 transition-colors"
            >
              Chat
            </button>
          </li>
          }
        </ul>
        } @else {
        <p class="text-gray-500 text-center mb-6">Chưa có bạn bè nào.</p>
        }

        <!-- Lời mời kết bạn -->
        <h3 class="text-xl font-semibold mb-3 text-gray-800">Lời mời kết bạn</h3>
        @if (pendingRequests().length > 0) {
        <ul class="space-y-3">
          @for (request of pendingRequests(); track request.maLoiMoi) {
          <li
            class="p-3 border border-gray-200 rounded-md flex justify-between items-center bg-gray-50 hover:bg-gray-100 transition-colors"
          >
            <span class="font-medium text-gray-800">{{ request.maTkGui.tenDn }} muốn kết bạn</span>
            <div class="space-x-2">
              <button
                (click)="acceptRequest(request.maLoiMoi)"
                class="bg-green-500 text-white px-3 py-1 rounded-md hover:bg-green-600 transition-colors"
              >
                Chấp nhận
              </button>
              <button
                class="bg-red-500 text-white px-3 py-1 rounded-md hover:bg-red-600 transition-colors"
              >
                Từ chối
              </button>
            </div>
          </li>
          }
        </ul>
        } @else {
        <p class="text-gray-500 text-center">Không có lời mời nào.</p>
        }
      </div>
    </div>
  `,
  styles: [
    `
      :host {
        display: block;
        padding: 1rem;
      }
    `,
  ],
})
export class FriendComponent {
  searchQuery = '';
  searchResults = signal<Taikhoan[]>([]);
  friends = signal<Taikhoan[]>([]);
  pendingRequests = signal<any[]>([]);
  friendRequests = signal<Taikhoan[]>([]);
  currentUsername = signal('');

  private statuses = ['Online', 'Last seen recently', 'Last seen today', 'Offline'];

  constructor(private friendService: FriendService, private authService: AuthService) {
    const user = this.authService.getCurrentUser();
    if (user?.username) {
      this.currentUsername.set(user.username);
      this.loadFriends();
      this.loadFriendRequests();
      this.loadPendingRequests();
    } else {
      console.error('No authenticated user found');
    }
  }

  onSearch() {
    if (this.searchQuery.length > 0) {
      this.friendService.searchUsers(this.searchQuery).subscribe({
        next: (allUsers) => {
          const username = this.currentUsername();
          this.friendService.getFriends(username).subscribe({
            next: (friends) => {
              const friendList = friends.map((f) => f.tenDn);
              const filtered = allUsers.filter(
                (u) => u.tenDn !== username && !friendList.includes(u.tenDn)
              );
              this.searchResults.set(filtered);
            },
            error: (error) => {
              console.error('Error fetching friends:', error);
              this.searchResults.set([]);
            },
          });
        },
        error: (error) => {
          console.error('Error searching users:', error);
          this.searchResults.set([]);
        },
      });
    } else {
      this.searchResults.set([]);
    }
  }

  getInitials(name: string): string {
    return name
      .split(' ')
      .map((word) => word[0])
      .join('')
      .toUpperCase()
      .slice(0, 2);
  }

  getRandomStatus(): string {
    return this.statuses[Math.floor(Math.random() * this.statuses.length)];
  }

  addFriend(username: string) {
    const currentUser = this.currentUsername();
    this.friendService.sendFriendRequest(currentUser, username).subscribe({
      next: () => {
        this.searchResults.update((users) => users.filter((u) => u.tenDn !== username));
      },
      error: (error) => {
        console.error('Error sending friend request:', error);
      },
    });
  }

  acceptFriendRequest(userId: number) {
    this.friendService.acceptFriendRequest(userId).subscribe({
      next: () => {
        this.loadFriendRequests();
        this.loadFriends();
      },
      error: (error) => {
        console.error('Error accepting friend request:', error);
      },
    });
  }

  rejectFriendRequest(userId: number) {
    this.friendService.rejectFriendRequest(userId).subscribe({
      next: () => {
        this.loadFriendRequests();
      },
      error: (error) => {
        console.error('Error rejecting friend request:', error);
      },
    });
  }

  loadFriendRequests() {
    const username = this.currentUsername();
    this.friendService.getFriendRequests(username).subscribe({
      next: (requests) => this.friendRequests.set(requests),
      error: (error) => {
        console.error('Error loading friend requests:', error);
        this.friendRequests.set([]);
      },
    });
  }

  acceptRequest(requestId: number) {
    this.friendService.acceptFriendRequest(requestId).subscribe(() => {
      this.loadPendingRequests();
      this.loadFriends();
    });
  }

  loadFriends() {
    const currentUser = this.authService.getCurrentUser()?.username || '';
    this.friendService.getFriends(currentUser).subscribe((friends) => {
      console.log('Loaded friends:', friends); // Debug load friends
      this.friends.set(friends);
    });
  }

  loadPendingRequests() {
    const currentUser = this.authService.getCurrentUser()?.username || '';
    this.friendService.getPendingRequests(currentUser).subscribe((requests) => {
      console.log('Loaded pending requests:', requests); // Debug load requests
      this.pendingRequests.set(requests);
    });
  }
}
