import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { ChatMessage } from '../models/chat.model';
import { Call, CallEvent, CallAnswer } from '../models/call.model';
import { environment } from '../../environments/environment';

declare var SockJS: any;
declare var Stomp: any;

@Injectable({
  providedIn: 'root',
})
export class WebSocketService {
  private stompClient: any;
  private isConnected = false;

  private messagesSubject = new BehaviorSubject<ChatMessage | null>(null);
  public messages$ = this.messagesSubject.asObservable();

  private callSubject = new BehaviorSubject<CallEvent | null>(null);
  public callEvents$ = this.callSubject.asObservable();

  private messageSubjects = new Map<string, BehaviorSubject<CallEvent | null>>();

  public onMessage(
    topic: string,
    filter?: (source: Observable<CallEvent | null>) => Observable<CallEvent | null>
  ): Observable<CallEvent | null> {
    if (!this.messageSubjects.has(topic)) {
      this.messageSubjects.set(topic, new BehaviorSubject<CallEvent | null>(null));
    }

    const subject = this.messageSubjects.get(topic)!;
    const observable = subject.asObservable();

    return filter ? observable.pipe(filter) : observable;
  }

  public send(topic: string, data: any): void {
    if (!this.isConnected || !this.stompClient) {
      console.error('WebSocket is not connected');
      return;
    }

    this.stompClient.send(`/app/${topic}`, {}, JSON.stringify(data));
  }

  connect(): void {
    if (this.isConnected) return;

    const socket = new SockJS(environment.apiUrl + '/ws');
    this.stompClient = Stomp.over(socket);

    this.stompClient.connect(
      {},
      (frame: any) => {
        console.log('Connected: ' + frame);
        this.isConnected = true;

        // Subscribe to public messages
        this.stompClient.subscribe('/topic/public', (message: any) => {
          try {
            const chatMessage: ChatMessage = JSON.parse(message.body);
            this.messagesSubject.next(chatMessage);
          } catch (error) {
            console.error('Error parsing public message:', error);
          }
        });

        // Subscribe to call events
        this.stompClient.subscribe('/topic/call', (message: any) => {
          try {
            const callEvent: CallEvent = JSON.parse(message.body);
            if (!callEvent.type) {
              console.error('Invalid call event: missing type property');
              return;
            }
            this.callSubject.next(callEvent);

            // Notify specific call events
            if (this.messageSubjects.has(callEvent.type)) {
              this.messageSubjects.get(callEvent.type)!.next(callEvent);
            }
          } catch (error) {
            console.error('Error parsing call event:', error);
          }
        });
      },
      (error: any) => {
        console.error('WebSocket connection error:', error);
        this.isConnected = false;
      }
    );
  }

  disconnect(): void {
    if (this.stompClient && this.isConnected) {
      this.stompClient.disconnect();
      this.isConnected = false;
    }
  }

  sendMessage(message: ChatMessage): void {
    if (this.stompClient && this.isConnected) {
      this.stompClient.send('/app/chat.sendMessage', {}, JSON.stringify(message));
    }
  }

  addUser(username: string): void {
    if (this.stompClient && this.isConnected) {
      this.stompClient.send('/app/chat.addUser', {}, JSON.stringify({ username }));
    }
  }

  initiateCall(call: Call): void {
    if (this.stompClient && this.isConnected) {
      const callEvent: CallEvent = {
        id: call.callId,
        type: 'incoming_call',
        caller: call.callerUsername,
        callee: call.receiverUsername,
        status: call.status,
        sdp: call.sdp,
        startTime: call.startTime,
        endTime: call.endTime,
      };
      this.stompClient.send('/app/call.initiate', {}, JSON.stringify(callEvent));
    }
  }

  answerCall(answer: CallAnswer): void {
    if (this.stompClient && this.isConnected) {
      const callEvent: CallEvent = {
        id: answer.id,
        type: answer.status === 'connected' ? 'call_accepted' : 'call_rejected',
        caller: '',
        callee: '',
        status: answer.status,
        sdp: answer.sdp,
      };
      this.stompClient.send('/app/call.answer', {}, JSON.stringify(callEvent));
    }
  }

  endCall(callId: string): void {
    if (this.stompClient && this.isConnected) {
      const callEvent: CallEvent = {
        id: callId,
        type: 'call_ended',
        caller: '',
        callee: '',
        status: 'ended',
        endTime: new Date(),
      };
      this.stompClient.send('/app/call.end', {}, JSON.stringify(callEvent));
    }
  }

  subscribeToUserQueue(username: string): void {
    if (this.stompClient && this.isConnected) {
      this.stompClient.subscribe(`/user/${username}/queue/messages`, (message: any) => {
        try {
          const chatMessage: ChatMessage = JSON.parse(message.body);
          this.messagesSubject.next(chatMessage);
        } catch (error) {
          console.error('Error parsing user queue message:', error);
        }
      });

      this.stompClient.subscribe(`/user/${username}/queue/call`, (message: any) => {
        try {
          const callEvent: CallEvent = JSON.parse(message.body);
          if (!callEvent.type) {
            console.error('Invalid call event: missing type property');
            return;
          }
          this.callSubject.next(callEvent);
        } catch (error) {
          console.error('Error parsing user queue call event:', error);
        }
      });
    }
  }
}
