// File: ChatController.java (Cập nhật)
package com.allies.app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
// Loại bỏ các import liên quan đến WebSocket (MessageMapping, SimpMessagingTemplate)

import com.allies.app.model.Nhom; // Giả sử có model Nhom
import com.allies.app.dto.GroupCreationDto;
import com.allies.app.service.ChatService;

import java.util.List;

@RestController
@RequestMapping("/api/conversations") // Đổi sang Conversations để dễ quản lý hơn
@CrossOrigin(origins = "*", maxAge = 3600)
public class ChatController {

    @Autowired
    private ChatService chatService;

    // 1. Task G4.4: Lấy danh sách Cuộc trò chuyện (Mới)
    @GetMapping
    public ResponseEntity<List<Nhom>> getConversations(@RequestParam Integer userId) {
        // Cần logic phức tạp để lấy cả chat 1-1 và nhóm, giả sử ChatService hỗ trợ
        return ResponseEntity.ok(chatService.getConversations(userId));
    }

    // 2. Task G4.1: Tạo Nhóm/Conversation (Mới)
    @PostMapping
    public ResponseEntity<Nhom> createGroup(@RequestBody GroupCreationDto dto) {
        // Cần logic để tạo Nhom và ThanhVienNhom
        Nhom newGroup = chatService.createGroup(dto);
        return ResponseEntity.status(201).body(newGroup);
    }

    // 3. Task G4.5: Lấy Lịch sử Tin nhắn
    @GetMapping("/messages/{convId}")
    public ResponseEntity<?> getConversationHistory(@PathVariable Integer convId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        // Xử lý logic lấy tin nhắn 1-1 hoặc tin nhắn nhóm dựa trên convId
        return ResponseEntity.ok(chatService.getConversationHistory(convId, page, size));
    }

}
