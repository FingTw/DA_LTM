import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class VideoSocketService {
  private socket?: WebSocket;
  private onMessageHandler?: (data: any) => void;

  connect(serverUrl: string): void {
    if (this.socket) {
      console.warn('[VideoSocket] Socket already connected');
      return;
    }

    this.socket = new WebSocket(serverUrl);

    this.socket.onopen = () => {
      console.log('[VideoSocket] Connected to signaling server:', serverUrl);
    };

    this.socket.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data);
        console.log('[VideoSocket] Received:', data);
        this.onMessageHandler?.(data);
      } catch (err) {
        console.error('[VideoSocket] Error parsing message:', err);
      }
    };

    this.socket.onclose = () => {
      console.log('[VideoSocket] Disconnected from signaling server');
      this.socket = undefined;
    };

    this.socket.onerror = (err) => {
      console.error('[VideoSocket] WebSocket error:', err);
    };
  }

  send(data: any): void {
    if (!this.socket || this.socket.readyState !== WebSocket.OPEN) {
      console.error('[VideoSocket] Socket not connected');
      return;
    }
    this.socket.send(JSON.stringify(data));
  }

  disconnect(): void {
    if (this.socket) {
      this.socket.close();
      this.socket = undefined;
    }
  }

  onMessage(callback: (data: any) => void): void {
    this.onMessageHandler = callback;
  }
}
