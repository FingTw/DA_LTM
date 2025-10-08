package com.allies.app.controller;

import com.allies.app.payload.FriendDTO;
import com.allies.app.repository.AccountRepository;
import com.allies.app.service.FriendshipService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/friends")
@CrossOrigin(origins = "http://localhost:4200")
public class FriendController {

    private final FriendshipService friendshipService;
    private final AccountRepository accountRepo;

    public FriendController(FriendshipService friendshipService, AccountRepository accountRepo) {
        this.friendshipService = friendshipService;
        this.accountRepo = accountRepo;
    }

    // Lấy danh sách bạn của user hiện tại (dựa vào authentication)
    @GetMapping("/me")
    public ResponseEntity<List<FriendDTO>> getMyFriends(Authentication authentication) {
        String username = authentication.getName();
        Integer maTk = accountRepo.findByUsername(username)
                .map(acc -> acc.getId())
                .orElse(null);
        if (maTk == null) return ResponseEntity.status(401).build();
        List<FriendDTO> friends = friendshipService.getFriendsOf(maTk);
        return ResponseEntity.ok(friends);
    }

    // Lấy danh sách bạn của user theo id (public)
    @GetMapping("/list/{maTk}")
    public ResponseEntity<List<FriendDTO>> getFriendsBy(@PathVariable Integer maTk) {
        List<FriendDTO> friends = friendshipService.getFriendsOf(maTk);
        return ResponseEntity.ok(friends);
    }

    // Tìm kiếm: đăng nhập (Authentication) optional - nếu có thì loại bỏ self và loại bỏ bạn hiện có
    @GetMapping("/search")
    public ResponseEntity<List<FriendDTO>> search(@RequestParam String keyword, Authentication authentication) {
        Integer maTk = null;
        if (authentication != null) {
            maTk = accountRepo.findByUsername(authentication.getName()).map(a -> a.getId()).orElse(null);
        }
        // nếu maTk null => caller anonymous => pass -1 to mean anonymous (will only exclude none)
        List<FriendDTO> results = friendshipService.searchUsers(maTk == null ? -1 : maTk, keyword);
        return ResponseEntity.ok(results);
    }
}
