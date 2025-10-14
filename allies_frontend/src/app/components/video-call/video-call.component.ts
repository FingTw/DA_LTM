import { Component, OnInit, OnDestroy, signal, ElementRef, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { WebSocketService } from '../../services/websocket.service';
import { AuthService } from '../../services/auth.service';
import { Call, CallEvent, CallAnswer } from '../../models/call.model';

@Component({
  selector: 'app-video-call',
  standalone: true,
  imports: [CommonModule],
  styleUrls: ['./video-call.component.css'],
  templateUrl: './video-call.component.html',
})
export class VideoCallComponent implements OnInit, OnDestroy {
  readonly callInfo = signal<Call | null>(null);
  readonly incomingCall = signal<Call | null>(null);
  readonly isCallActive = signal(false);
  readonly isMuted = signal(false);
  readonly isVideoOff = signal(false);
  readonly isScreenSharing = signal(false);
  readonly remotePeerConnected = signal(false);
  readonly connectionStatus = signal<'connecting' | 'connected' | 'disconnected'>('disconnected');
  readonly callStartTime = signal<Date | null>(null);

  @ViewChild('localVideo') localVideo!: ElementRef<HTMLVideoElement>;
  @ViewChild('remoteVideo') remoteVideo!: ElementRef<HTMLVideoElement>;

  private localStream: MediaStream | null = null;
  private remoteStream: MediaStream | null = null;
  private screenStream: MediaStream | null = null;
  private peerConnection: RTCPeerConnection | null = null;
  private readonly destroy$ = new Subject<void>();

  constructor(private webSocketService: WebSocketService, private authService: AuthService) {}

  ngOnInit(): void {
    this.subscribeToCallEvents();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    this.endCall();
  }

  private subscribeToCallEvents(): void {
    this.webSocketService.callEvents$.pipe(takeUntil(this.destroy$)).subscribe((event) => {
      if (event) {
        switch (event.type) {
          case 'incoming_call':
            const call = this.mapCallEventToCall(event);
            this.handleIncomingCall(call);
            break;
          case 'call_accepted':
            this.startCall();
            break;
          case 'call_rejected':
          case 'call_ended':
            this.endCall();
            break;
        }
      }
    });
  }

  private mapCallEventToCall(event: CallEvent): Call {
    return {
      callId: event.id,
      callerUsername: event.caller,
      receiverUsername: event.callee,
      callType: event.sdp?.type === 'offer' ? 'video' : 'audio', // Infer callType; adjust if backend provides it
      status: event.status,
      startTime: event.startTime || new Date(),
      endTime: event.endTime,
      sdp: event.sdp,
    };
  }

  private async handleIncomingCall(call: Call): Promise<void> {
    this.incomingCall.set(call);
    this.callInfo.set(call);
  }

  private async createPeerConnection(): Promise<void> {
    try {
      this.peerConnection = new RTCPeerConnection({
        iceServers: [{ urls: 'stun:stun.l.google.com:19302' }],
      });

      this.peerConnection.onicecandidate = ({ candidate }) => {
        if (candidate && this.callInfo()) {
          this.webSocketService.send('iceCandidate', {
            callId: this.callInfo()!.callId,
            candidate,
          });
        }
      };

      this.peerConnection.ontrack = ({ streams }) => {
        this.remoteStream = streams[0];
        if (this.remoteVideo?.nativeElement) {
          this.remoteVideo.nativeElement.srcObject = this.remoteStream;
        }
        this.remotePeerConnected.set(true);
        this.connectionStatus.set('connected');
      };

      this.peerConnection.oniceconnectionstatechange = () => {
        switch (this.peerConnection?.iceConnectionState) {
          case 'disconnected':
          case 'failed':
            this.connectionStatus.set('disconnected');
            break;
          case 'checking':
            this.connectionStatus.set('connecting');
            break;
          case 'connected':
            this.connectionStatus.set('connected');
            break;
        }
      };

      if (this.localStream) {
        this.localStream.getTracks().forEach((track) => {
          this.peerConnection?.addTrack(track, this.localStream!);
        });
      }
    } catch (error) {
      console.error('Error creating peer connection:', error);
      throw error;
    }
  }

  private async setupLocalMedia(constraints: MediaStreamConstraints): Promise<void> {
    try {
      this.localStream = await navigator.mediaDevices.getUserMedia(constraints);
      if (this.localVideo?.nativeElement) {
        this.localVideo.nativeElement.srcObject = this.localStream;
      }
    } catch (error) {
      console.error('Error setting up local media:', error);
      throw error;
    }
  }

  startVideoCall(): void {
    this.initiateCall('video');
  }

  startVoiceCall(): void {
    this.initiateCall('audio');
  }

  private initiateCall(type: 'audio' | 'video'): void {
    const currentUser = this.authService.getCurrentUser();
    if (!currentUser) return;

    const call: Call = {
      callId: Date.now().toString(),
      callerUsername: currentUser.username,
      receiverUsername: 'receiver', // Should come from selected chat
      callType: type,
      status: 'ringing',
      startTime: new Date(),
    };

    this.callInfo.set(call);
    this.webSocketService.send('initiateCall', call);
  }

  async startCall(type?: 'audio' | 'video'): Promise<void> {
    try {
      await this.setupLocalMedia({
        video: type === 'audio' ? false : true,
        audio: true,
      });

      await this.createPeerConnection();

      const offer = await this.peerConnection!.createOffer({
        offerToReceiveAudio: true,
        offerToReceiveVideo: type === 'video',
      });

      await this.peerConnection!.setLocalDescription(offer);

      const call = this.callInfo();
      if (!call) return;

      this.webSocketService.send('offer', {
        ...call,
        sdp: offer,
      });

      this.isCallActive.set(true);
      this.callStartTime.set(new Date());
    } catch (error) {
      console.error('Error starting call:', error);
      this.cleanupCall();
    }
  }

  async answerCall(): Promise<void> {
    if (!this.incomingCall()) return;

    try {
      const call = this.incomingCall()!;

      await this.setupLocalMedia({
        video: call.callType === 'video',
        audio: true,
      });

      await this.createPeerConnection();

      await this.peerConnection!.setRemoteDescription(new RTCSessionDescription(call.sdp!));

      const answer = await this.peerConnection!.createAnswer();
      await this.peerConnection!.setLocalDescription(answer);

      this.webSocketService.send('answer', {
        ...call,
        sdp: answer,
      });

      this.isCallActive.set(true);
      this.callStartTime.set(new Date());
      this.incomingCall.set(null);
    } catch (error) {
      console.error('Error answering call:', error);
      this.rejectCall();
    }
  }

  rejectCall(): void {
    if (!this.incomingCall()) return;

    this.webSocketService.send('reject', {
      ...this.incomingCall()!,
      status: 'rejected',
    });

    this.incomingCall.set(null);
  }

  async toggleMute(): Promise<void> {
    if (!this.localStream) return;

    const audioTracks = this.localStream.getAudioTracks();
    if (audioTracks.length > 0) {
      audioTracks.forEach((track) => {
        track.enabled = !track.enabled;
      });
      this.isMuted.set(!audioTracks[0].enabled);
    }
  }

  async toggleVideo(): Promise<void> {
    if (!this.localStream) return;

    const videoTracks = this.localStream.getVideoTracks();
    if (videoTracks.length > 0) {
      videoTracks.forEach((track) => {
        track.enabled = !track.enabled;
      });
      this.isVideoOff.set(!videoTracks[0].enabled);
    }
  }

  async toggleScreenShare(): Promise<void> {
    try {
      if (this.isScreenSharing()) {
        await this.stopScreenSharing();
      } else {
        await this.startScreenSharing();
      }
    } catch (error) {
      console.error('Error toggling screen share:', error);
    }
  }

  private async startScreenSharing(): Promise<void> {
    try {
      this.screenStream = await navigator.mediaDevices.getDisplayMedia({
        video: true,
      });

      const videoTrack = this.screenStream.getVideoTracks()[0];

      if (this.peerConnection) {
        const sender = this.peerConnection.getSenders().find((s) => s.track?.kind === 'video');

        if (sender) {
          await sender.replaceTrack(videoTrack);
        }
      }

      videoTrack.onended = () => {
        this.stopScreenSharing();
      };

      this.isScreenSharing.set(true);
    } catch (error) {
      console.error('Error starting screen share:', error);
      this.isScreenSharing.set(false);
    }
  }

  private async stopScreenSharing(): Promise<void> {
    if (!this.screenStream || !this.localStream || !this.peerConnection) return;

    try {
      const videoTrack = this.localStream.getVideoTracks()[0];
      const sender = this.peerConnection.getSenders().find((s) => s.track?.kind === 'video');

      if (sender && videoTrack) {
        await sender.replaceTrack(videoTrack);
      }

      this.screenStream.getTracks().forEach((track) => track.stop());
      this.screenStream = null;
      this.isScreenSharing.set(false);
    } catch (error) {
      console.error('Error stopping screen share:', error);
    }
  }

  endCall(): void {
    if (this.callInfo()) {
      this.webSocketService.send('end', {
        ...this.callInfo()!,
        status: 'ended',
        endTime: new Date(),
      });
    }

    this.cleanupCall();
  }

  private cleanupCall(): void {
    if (this.localStream) {
      this.localStream.getTracks().forEach((track) => track.stop());
      this.localStream = null;
    }

    if (this.screenStream) {
      this.screenStream.getTracks().forEach((track) => track.stop());
      this.screenStream = null;
    }

    if (this.remoteStream) {
      this.remoteStream.getTracks().forEach((track) => track.stop());
      this.remoteStream = null;
    }

    if (this.peerConnection) {
      this.peerConnection.close();
      this.peerConnection = null;
    }

    this.isCallActive.set(false);
    this.isMuted.set(false);
    this.isVideoOff.set(false);
    this.isScreenSharing.set(false);
    this.remotePeerConnected.set(false);
    this.connectionStatus.set('disconnected');
    this.callInfo.set(null);
    this.incomingCall.set(null);
    this.callStartTime.set(null);
  }

  getCallDuration(): string {
    if (!this.callStartTime()) return '00:00';

    const now = new Date();
    const duration = Math.floor((now.getTime() - this.callStartTime()!.getTime()) / 1000);
    const minutes = Math.floor(duration / 60);
    const seconds = duration % 60;

    return `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
  }

  connectionStatusClass(): string {
    return `status-${this.connectionStatus()}`;
  }
}
