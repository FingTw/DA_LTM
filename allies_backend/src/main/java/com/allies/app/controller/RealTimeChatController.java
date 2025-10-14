// File: RealTimeChatController.java (Mới)
package com.allies.app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

import com.allies.app.model.Chat;
import com.allies.app.model.Tinnhannhom;
import com.allies.app.service.ChatService;
import com.allies.app.dto.ChatMessageDto;
import com.allies.app.dto.TypingStatusDto;

// Sử dụng @Controller thay vì @RestController cho WebSocket
@Controller
public class RealTimeChatController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ChatService chatService;

    // Endpoint: /app/chat/send
    @MessageMapping("/chat/send")
    public void sendMessage(@Payload ChatMessageDto messageDto, @AuthenticationPrincipal UserDetails senderDetails) {
        // Lấy MA_TK của người gửi từ JWT (sau khi Interceptor xử lý)
        // Cần ánh xạ UserDetails về đối tượng Taikhoan để lấy ID, hoặc dùng TaikhoanService
        // Giả sử senderDetails.getUsername() là TEN_DN, và cần TaikhoanService để lấy MA_TK

        Integer senderId = chatService.getTaikhoanIdByUsername(senderDetails.getUsername());

        if (messageDto.getReceiverId() != null) {
            // 1. Nhắn tin 1-1 (Private Message)
            Chat savedMessage = chatService.savePrivateMessage(messageDto, senderId);

            // Gửi tin nhắn đến người nhận: /queue/messages
            // Tên người dùng cần phải là TEN_DN (username) để định tuyến
            messagingTemplate.convertAndSendToUser(
                    messageDto.getReceiverUsername(), // TEN_DN của người nhận
                    "/queue/messages",
                    savedMessage
            );
            // Gửi lại cho người gửi (để cập nhật trạng thái đã gửi): /queue/messages
            messagingTemplate.convertAndSendToUser(
                    senderDetails.getUsername(),
                    "/queue/messages",
                    savedMessage
            );
        } else if (messageDto.getGroupId() != null) {
            // 2. Nhắn tin Nhóm (Group Message)
            Tinnhannhom savedMessage = chatService.saveGroupMessage(messageDto, senderId);

            // Gửi tin nhắn đến nhóm: /topic/group/{MA_NHOM}
            messagingTemplate.convertAndSend(
                    "/topic/group/" + messageDto.getGroupId(),
                    savedMessage
            );
        }
    }

    // Endpoint: /app/messages/typing
    @MessageMapping("/messages/typing")
    public void sendTypingStatus(@Payload TypingStatusDto status) {
        // Chỉ truyền tải trạng thái gõ phím đến người nhận
        if (status.getReceiverUsername() != null) {
            messagingTemplate.convertAndSendToUser(
                    status.getReceiverUsername(),
                    "/queue/typing",
                    status.getSenderUsername()
            );
        } else if (status.getGroupId() != null) {
            // Truyền tải trạng thái gõ phím trong nhóm
            messagingTemplate.convertAndSend(
                    "/topic/group/" + status.getGroupId() + "/typing",
                    status.getSenderUsername()
            );
        }
    }

    // ... Thêm @MessageMapping cho messages/seen, call/signal (nên tách ra CallSignalingController)
}
