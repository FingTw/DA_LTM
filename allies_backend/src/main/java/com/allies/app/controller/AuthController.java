package com.allies.app.controller;

import com.allies.app.model.Account;
import com.allies.app.payload.request.LoginRequest;
import com.allies.app.payload.request.SignupRequest;
import com.allies.app.payload.response.JwtResponse;
import com.allies.app.repository.AccountRepository;
import com.allies.app.security.JwtUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:4200", maxAge = 3600) // Cho phép Angular Frontend
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    AuthenticationManager authenticationManager; // Từ SecurityConfig

    @Autowired
    AccountRepository accountRepository; // Để kiểm tra username tồn tại và lưu user mới

    @Autowired
    PasswordEncoder encoder; // Từ SecurityConfig (BCrypt)

    @Autowired
    JwtUtils jwtUtils; // Để sinh token

    // API Đăng nhập
    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        // 1. Xác thực người dùng (Spring Security sẽ gọi AccountService)
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        // 2. Đặt đối tượng xác thực vào Security Context
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        // 3. Sinh JWT và trả về
        String jwt = jwtUtils.generateJwtToken(authentication);
        Account userDetails = (Account) authentication.getPrincipal(); // Lấy Account từ đối tượng xác thực

        return ResponseEntity.ok(new JwtResponse(jwt,userDetails.getId(),userDetails.getUsername(),
                                                 null)); 
    }

    // API Đăng ký
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        
        // Kiểm tra Username đã tồn tại chưa
        if (accountRepository.findByUsername(signUpRequest.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Error: Username is already taken!");
        }

        // Tạo Account mới
        Account account = new Account();
        account.setUsername(signUpRequest.getUsername());
        
        // MÃ HÓA PASSWORD trước khi lưu vào DB
        account.setPassword(encoder.encode(signUpRequest.getPassword())); 

        accountRepository.save(account);

        return ResponseEntity.ok("Account registered successfully!");
    }
}