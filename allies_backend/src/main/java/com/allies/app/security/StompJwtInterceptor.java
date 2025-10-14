package com.allies.app.security;

import java.util.List;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.allies.app.service.TaikhoanService;

// BỔ SUNG: Interceptor để xử lý JWT trong WebSocket Handshake
@Component
public class StompJwtInterceptor implements ChannelInterceptor {

    private final JwtUtils jwtUtils;
    private final TaikhoanService taikhoanService;

    public StompJwtInterceptor(JwtUtils jwtUtils, TaikhoanService taikhoanService) {
        this.jwtUtils = jwtUtils;
        this.taikhoanService = taikhoanService;
    }

    // Chặn message trước khi gửi đến Message Channel
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor
                = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        // Chỉ xử lý khi có yêu cầu kết nối STOMP (CONNECT command)
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {

            // 1. Lấy JWT từ header "Authorization" (hoặc "Authorization" là STOMP header)
            List<String> authorization = accessor.getNativeHeader("Authorization");
            String jwt = null;

            if (authorization != null && !authorization.isEmpty()) {
                String headerAuth = authorization.get(0);
                if (headerAuth != null && headerAuth.startsWith("Bearer ")) {
                    jwt = headerAuth.substring(7);
                }
            }

            // 2. Xác thực và đặt Principal (User) vào phiên
            if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
                String username = jwtUtils.getUserNameFromJwtToken(jwt);

                // Tải thông tin người dùng từ DB
                UserDetails userDetails = taikhoanService.loadUserByUsername(username);

                // Tạo đối tượng xác thực (UsernamePasswordAuthenticationToken)
                UsernamePasswordAuthenticationToken authentication
                        = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                // Đặt đối tượng xác thực vào phiên STOMP
                accessor.setUser(authentication);
            } else {
                // Tùy chọn: Log lỗi hoặc ném ngoại lệ nếu JWT không hợp lệ
                // Hiện tại chỉ bỏ qua để Spring Security xử lý
                System.err.println("WebSocket connection blocked: Invalid or missing JWT.");
            }
        }
        return message;
    }
}
