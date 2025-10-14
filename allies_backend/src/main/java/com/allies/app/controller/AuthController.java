package com.allies.app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.allies.app.model.Taikhoan;
import com.allies.app.payload.request.LoginRequest;
import com.allies.app.payload.request.SignupRequest;
import com.allies.app.payload.response.JwtResponse;
import com.allies.app.security.JwtUtils;
import com.allies.app.service.TaikhoanService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private TaikhoanService taikhoanService;

    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);

            Taikhoan user = taikhoanService.getTaikhoanByTenDn(loginRequest.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found: " + loginRequest.getUsername()));

            return ResponseEntity.ok(new JwtResponse(jwt, user.getMaTk(), user.getTenDn()));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Sai tên đăng nhập hoặc mật khẩu");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi hệ thống, vui lòng thử lại sau");
        }
    }

    // Thêm vào file AuthController.java
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        try {
            if (taikhoanService.getTaikhoanByTenDn(signUpRequest.getUsername()).isPresent()) {

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Tên đăng nhập đã tồn tại!");
            }

            Taikhoan user = new Taikhoan();
            user.setTenDn(signUpRequest.getUsername());
            user.setMk(signUpRequest.getPassword());

            taikhoanService.createTaikhoan(user);

            return ResponseEntity.status(HttpStatus.CREATED).body("Đăng ký tài khoản thành công!");
        } catch (IllegalArgumentException e) {

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Lỗi: " + e.getMessage());
        } catch (Exception e) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi hệ thống, vui lòng thử lại sau");
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(
            @CurrentSecurityContext(expression = "authentication.principal") UserDetails userDetails) {
        try {
            if (userDetails == null) {

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Chưa đăng nhập");
            }
            String username = userDetails.getUsername();
            Taikhoan taikhoan = taikhoanService.getTaikhoanByTenDn(username)
                    .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng: " + username));

            return ResponseEntity.ok(taikhoan);
        } catch (UsernameNotFoundException e) {

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Không tìm thấy người dùng");
        } catch (Exception e) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi hệ thống, vui lòng thử lại sau");
        }
    }
}
