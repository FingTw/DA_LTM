import { Component, ElementRef, ViewChild } from '@angular/core';

@Component({
  selector: 'app-video-call',
  templateUrl: './video-call.component.html',
  styleUrls: ['./video-call.component.css']
})
export class VideoCallComponent {
  @ViewChild('localVideo') localVideo!: ElementRef<HTMLVideoElement>;
  @ViewChild('remoteVideo') remoteVideo!: ElementRef<HTMLVideoElement>;

  private peerConnection?: RTCPeerConnection;
  private localStream?: MediaStream;

  cameraOn = true;
  micOn = true;
  callStatus = '● Sẵn sàng kết nối';

  async startCall() {
    this.callStatus = '● Đang kết nối...';
    console.log('Bắt đầu gọi video...');

    // Bật camera/micro
    this.localStream = await navigator.mediaDevices.getUserMedia({ video: true, audio: true });
    this.localVideo.nativeElement.srcObject = this.localStream;

    // Tạo kết nối peer (demo nội bộ)
    const config = { iceServers: [{ urls: 'stun:stun.l.google.com:19302' }] };
    this.peerConnection = new RTCPeerConnection(config);

    // Thêm track từ local stream
    this.localStream.getTracks().forEach(track => {
      this.peerConnection?.addTrack(track, this.localStream!);
    });

    // Khi có track remote (demo)
    this.peerConnection.ontrack = (event) => {
      this.remoteVideo.nativeElement.srcObject = event.streams[0];
    };

    // Tạo offer (demo)
    const offer = await this.peerConnection.createOffer();
    await this.peerConnection.setLocalDescription(offer);

    console.log('Offer created:', offer.sdp);
  }

  toggleCamera() {
    if (this.localStream) {
      this.cameraOn = !this.cameraOn;
      this.localStream.getVideoTracks().forEach(track => track.enabled = this.cameraOn);
      console.log(this.cameraOn ? 'Camera bật' : 'Camera tắt');
    }
  }

  toggleMic() {
    if (this.localStream) {
      this.micOn = !this.micOn;
      this.localStream.getAudioTracks().forEach(track => track.enabled = this.micOn);
      console.log(this.micOn ? 'Mic bật' : 'Mic tắt');
    }
  }

  endCall() {
    this.callStatus = '● Đã ngắt kết nối';
    console.log('Kết thúc cuộc gọi.');
    this.localStream?.getTracks().forEach(track => track.stop());
    this.peerConnection?.close();
  }
}
