// File: CallSignalingController.java (Mới)
package com.allies.app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import com.allies.app.dto.SignalingMessage;

// Endpoint: /app/call/signal
@Controller
public class CallSignalingController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // V6.2: Xử lý WebRTC Signaling (Offer, Answer, ICE Candidates)
    @MessageMapping("/call/signal")
    public void handleCallSignaling(@Payload SignalingMessage message) {

        if (message.getReceiverId() != null) {
            // 1. Gửi tín hiệu 1-1: Định tuyến đến người nhận
            messagingTemplate.convertAndSendToUser(
                    message.getReceiverUsername(),
                    "/queue/call/signal",
                    message
            );
        } else if (message.getGroupId() != null) {
            // 2. Gửi tín hiệu Nhóm: Gửi đến kênh nhóm
            messagingTemplate.convertAndSend(
                    "/topic/group/" + message.getGroupId() + "/call/signal",
                    message
            );
        }
    }

    // ... Thêm @MessageMapping cho /app/call/join và /app/call/leave
}
