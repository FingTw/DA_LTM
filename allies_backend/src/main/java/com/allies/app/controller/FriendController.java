// File: FriendController.java (Cập nhật)
package com.allies.app.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal; // Thêm import
import org.springframework.security.core.userdetails.UserDetails; // Thêm import
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam; // Thêm import
import org.springframework.web.bind.annotation.RestController;

import com.allies.app.dto.FriendRequestDto;
import com.allies.app.model.Loimoiketban;
import com.allies.app.model.Taikhoan;
import com.allies.app.service.LoimoiketbanService;
import com.allies.app.service.QuanheService;
import com.allies.app.service.TaikhoanService;

@RestController
@RequestMapping("/api/friends")
public class FriendController {

    @Autowired
    private LoimoiketbanService friendRequestService;
    @Autowired
    private QuanheService friendRelationService;
    @Autowired
    private TaikhoanService taikhoanService; // Tiêm TaikhoanService

    // Task P2.4: Tìm kiếm người dùng bằng query (Mới)
    @GetMapping("/search")
    public ResponseEntity<List<Taikhoan>> searchUsers(@RequestParam String query) {
        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.ok(List.of()); // Trả về list rỗng nếu query trống
        }
        // SỬA LỖI: Gọi đúng Service để tìm kiếm
        return ResponseEntity.ok(taikhoanService.searchUsers(query));
    }

    // Task F3.1: Gửi lời mời kết bạn (Sử dụng ID người gửi từ JWT)
    @PostMapping("/request")
    public ResponseEntity<Loimoiketban> sendFriendRequest(
            @RequestBody FriendRequestDto dto,
            @AuthenticationPrincipal UserDetails senderDetails // Lấy TEN_DN của người gửi từ JWT
    ) {
        // Lấy MA_TK của người gửi từ TEN_DN
        Integer senderId = taikhoanService.getTaikhoanByTenDn(senderDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Người gửi không tồn tại"))
                .getMaTk();

        Loimoiketban request = friendRequestService.sendFriendRequest(
                senderId, dto.getReceiverId(), dto.getNoiDung());

        return ResponseEntity.status(HttpStatus.CREATED).body(request);
    }

    // Task F3.2: Chấp nhận lời mời (Giữ nguyên, cần đảm bảo logic Service kiểm tra quyền)
    @PutMapping("/request/{id}/accept")
    public ResponseEntity<Loimoiketban> acceptRequest(@PathVariable Integer id) {
        return ResponseEntity.ok(friendRequestService.acceptFriendRequest(id));
    }

    // Task F3.2: Từ chối lời mời
    @PutMapping("/request/{id}/reject")
    public ResponseEntity<Loimoiketban> rejectRequest(@PathVariable Integer id) {
        return ResponseEntity.ok(friendRequestService.rejectFriendRequest(id));
    }

    // Task F3.3: Lấy danh sách bạn bè (Sử dụng ID người dùng từ JWT)
    @GetMapping("/list")
    public ResponseEntity<List<Taikhoan>> getFriends(
            @AuthenticationPrincipal UserDetails userDetails // Lấy TEN_DN của người dùng
    ) {
        Integer userId = taikhoanService.getTaikhoanByTenDn(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"))
                .getMaTk();

        return ResponseEntity.ok(friendRelationService.getFriends(userId));
    }

    // Task F3.x: Lấy danh sách lời mời đang chờ (Người dùng nhận được) (Cập nhật)
    @GetMapping("/requests/pending")
    public ResponseEntity<List<Loimoiketban>> getPendingRequests(
            @AuthenticationPrincipal UserDetails userDetails // Lấy TEN_DN của người dùng
    ) {
        Integer receiverId = taikhoanService.getTaikhoanByTenDn(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"))
                .getMaTk();

        // Đổi tên endpoint từ /requests sang /requests/pending cho rõ ràng
        return ResponseEntity.ok(friendRequestService.getPendingRequests(receiverId));
    }
}
