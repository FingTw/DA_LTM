import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Taikhoan } from '../models/user.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class FriendService {
  private readonly API_URL = environment.apiUrl + '/friends';

  constructor(private http: HttpClient) {}

  searchUsers(query: string): Observable<Taikhoan[]> {
    return this.http.get<Taikhoan[]>(`${this.API_URL}/search?query=${query}`);
  }

  sendFriendRequest(sender: string, receiver: string): Observable<any> {
    return this.http.post(`${this.API_URL}/request`, { sender, receiver });
  }

  getFriendRequests(username: string): Observable<Taikhoan[]> {
    return this.http.get<Taikhoan[]>(`${this.API_URL}/requests/received?username=${username}`);
  }

  getPendingRequests(username: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.API_URL}/requests/sent?username=${username}`);
  }

  acceptFriendRequest(requestId: number): Observable<any> {
    return this.http.put(`${this.API_URL}/request/${requestId}/accept`, {});
  }

  rejectFriendRequest(requestId: number): Observable<any> {
    return this.http.put(`${this.API_URL}/request/${requestId}/reject`, {});
  }

  getFriends(username: string): Observable<Taikhoan[]> {
    return this.http.get<Taikhoan[]>(`${this.API_URL}/list?username=${username}`);
  }
}
