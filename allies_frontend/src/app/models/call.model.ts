export type CallStatus =
  | 'calling'
  | 'connected'
  | 'rejected'
  | 'ended'
  | 'disconnected'
  | 'ringing';

export type CallType = 'audio' | 'video';

export type CallEventType = 'incoming_call' | 'call_accepted' | 'call_rejected' | 'call_ended';

export interface Call {
  callId: string;
  callerUsername: string;
  receiverUsername: string;
  callType: CallType;
  status: CallStatus;
  startTime: Date;
  endTime?: Date;
  sdp?: RTCSessionDescriptionInit;
}

export interface CallEvent {
  id: string;
  type: CallEventType;
  caller: string;
  callee: string;
  status: CallStatus;
  sdp?: RTCSessionDescriptionInit;
  startTime?: Date;
  endTime?: Date;
}

export interface CallingData extends CallEvent {
  status: 'calling';
  sdp: RTCSessionDescriptionInit;
  startTime: Date;
}

export interface CallAnswer {
  id: string;
  status: 'connected' | 'rejected';
  sdp?: RTCSessionDescriptionInit;
}
