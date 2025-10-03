package com.allies.app.config;


import org.springframework.beans.factory.annotation.Autowired; // Import Filter
import org.springframework.context.annotation.Bean; // Import Service
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.allies.app.security.JwtAuthTokenFilter;
import com.allies.app.service.AccountService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // 1. Dịch vụ người dùng (tải thông tin từ DB)
    @Autowired
    private AccountService accountService;

    // 2. JWT Filter: Tạo Bean để Spring có thể quản lý và Inject
    @Bean
    public JwtAuthTokenFilter authenticationJwtTokenFilter() {
        // Lớp này sẽ được sử dụng để kiểm tra JWT trong mọi yêu cầu
        return new JwtAuthTokenFilter(); 
    }

    // 3. Mã hóa mật khẩu: Sử dụng BCrypt để lưu mật khẩu an toàn
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    // 4. Authentication Manager: Bean quản lý quá trình xác thực (Dùng trong AuthController)
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    // 5. Chuỗi Bộ lọc Bảo mật (Security Filter Chain): Cấu hình chính
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Cho phép CORS (Để Angular Frontend gọi API)
            .cors(Customizer.withDefaults()) 
            
            // Tắt CSRF vì chúng ta đang dùng API RESTful và JWT (StateLess)
            .csrf(csrf -> csrf.disable()) 
            
            // QUAN TRỌNG: Thiết lập cơ chế quản lý session là STATELESS. 
            // Điều này báo cho Spring Security biết không cần tạo session, 
            // thay vào đó sử dụng Token (JWT) cho mỗi yêu cầu.
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // Thiết lập quyền truy cập cho các Request
            .authorizeHttpRequests(auth -> auth
                // API Đăng ký và Đăng nhập (vào /api/auth/**) được phép truy cập công khai
                .requestMatchers("/api/auth/**").permitAll() 
                
                // Cần cho phép truy cập public vào endpoint WebSocket
                .requestMatchers("/ws/**").permitAll() 

                // Yêu cầu xác thực (có JWT hợp lệ) cho TẤT CẢ các request khác
                .anyRequest().authenticated() 
            );

        // THÊM JWT FILTER VÀO CHUỖI:
        // Đặt JwtAuthTokenFilter trước bộ lọc xử lý xác thực mặc định của Spring
        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}