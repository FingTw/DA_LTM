// File: CallController.java (Cập nhật)
package com.allies.app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
// Loại bỏ các import liên quan đến WebSocket

import com.allies.app.model.Cuocgoi;
import com.allies.app.model.Taikhoan;
import com.allies.app.service.CuocgoiService;
import com.allies.app.dto.CallStartDto;
import com.allies.app.service.TaikhoanService;
import java.util.Map;

import java.util.List;

@RestController
@RequestMapping("/api/calls")
@CrossOrigin(origins = "*", maxAge = 3600)
public class CallController {

    @Autowired
    private CuocgoiService cuocgoiService;

    @Autowired
    private TaikhoanService taikhoanService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // file: CallController.java (Phương thức initiateCall)
    @MessageMapping("/call.initiate")
    public void initiateCall(@Payload Map<String, Object> callData) {
        try {
            // SỬA LỖI: Cast về Integer. Nếu client gửi String, bạn phải parse (Integer.parseInt((String)...))
            // Giả sử client gửi Integer.
            Integer callerId = (Integer) callData.get("callerId");
            Integer receiverId = (Integer) callData.get("receiverId");
            String callType = (String) callData.get("callType");

            // Thêm kiểm tra null
            if (callerId == null || receiverId == null || callType == null) {
                throw new IllegalArgumentException("Dữ liệu cuộc gọi thiếu thông tin");
            }

            // ... (Logic tạo Cuocgoi và Thanhviencuocgoi)
            // Lấy TenDn của người nhận để gửi qua WebSocket
            Taikhoan receiver = taikhoanService.getTaikhoanById(receiverId)
                    .orElseThrow(() -> new RuntimeException("Receiver not found"));

            // Gửi thông báo đến người nhận
            messagingTemplate.convertAndSendToUser(
                    receiver.getTenDn(),
                    "/queue/call",
                    Map.of("type", "initiate", "callerId", callerId, "callType", callType)
            );
        } catch (Exception e) {
            // ... (Log lỗi)
        }
    }

    // V6.1: Khởi tạo/Bắt đầu Cuộc gọi (RESTful logging)
    @PostMapping("/start")
    public ResponseEntity<Cuocgoi> startCall(@RequestBody CallStartDto callData) {
        // Map CallStartDto to Cuocgoi entity and start the call
        Cuocgoi call = new Cuocgoi();
        call.setLoaiGoi(callData.getCallType());
        call.setThoiGianBatDau(java.time.Instant.now());
        // If groupId present, set it
        if (callData.getGroupId() != null) {
            com.allies.app.model.Nhom group = new com.allies.app.model.Nhom();
            group.setId(callData.getGroupId());
            call.setMaNhom(group);
        }
        Cuocgoi savedCall = cuocgoiService.startCall(call, callData.getCallerId());
        return ResponseEntity.status(201).body(savedCall);
    }

    // V6.5: Kết thúc Cuộc gọi
    @PutMapping("/{callId}/end")
    public ResponseEntity<Cuocgoi> endCall(@PathVariable Integer callId) {
        // Logic cập nhật THOI_GIAN_KET_THUC và TONG_THOI_LUONG_GIAY
        Cuocgoi updatedCall = cuocgoiService.endCall(callId);
        return ResponseEntity.ok(updatedCall);
    }

    // V6.6: Lấy lịch sử cuộc gọi (Sửa lỗi: dùng @AuthenticationPrincipal hoặc @PathVariable userId)
    @GetMapping("/history")
    public ResponseEntity<List<Cuocgoi>> getCallHistory(@RequestParam Integer userId) {
        // Dùng userId được truyền vào
        List<Cuocgoi> calls = cuocgoiService.getCallHistoryByUser(userId);
        return ResponseEntity.ok(calls);
    }

    // (Xóa bỏ tất cả các MessageMapping và POST /create cũ)
}
