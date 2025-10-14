package com.allies.app.config;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import com.allies.app.repository.TaikhoanRepository;
import com.allies.app.security.JwtAuthTokenFilter;
import com.allies.app.security.JwtUtils;
import com.allies.app.service.TaikhoanService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthTokenFilter jwtAuthTokenFilter) throws Exception {
        http
            .cors(cors -> {}) // ✅ Bật CORS (dùng cấu hình ở bean dưới)
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // ✅ Cho phép các endpoint public
                .requestMatchers("/api/auth/**", "/api/test/**", "/ws/**").permitAll()
                .anyRequest().permitAll()
            )
            .addFilterBefore(jwtAuthTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // ✅ Cấu hình CORS cho phép domain ngrok/public IP
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        
        // 🔥 Đây là phần QUAN TRỌNG — cho phép tất cả origin patterns
        config.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:*",
            "https://*.ngrok-free.dev",
            "https://*.ngrok.io",
            "http://127.0.0.1:*",
            "http://192.168.*.*",  // cho LAN
            "http://10.*.*.*"      // cho LAN
        ));
        
        config.setAllowedHeaders(Arrays.asList("*"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setExposedHeaders(Arrays.asList("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public JwtAuthTokenFilter jwtAuthTokenFilter(JwtUtils jwtUtils, TaikhoanService taikhoanService) {
        return new JwtAuthTokenFilter(jwtUtils, taikhoanService);
    }

    @Bean
    public JwtUtils jwtUtils() {
        return new JwtUtils();
    }

    @Bean
    public TaikhoanService taikhoanService(TaikhoanRepository taikhoanRepository, PasswordEncoder passwordEncoder) {
        return new TaikhoanService(taikhoanRepository, passwordEncoder);
    }
}
