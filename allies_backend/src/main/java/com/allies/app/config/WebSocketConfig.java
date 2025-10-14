package com.allies.app.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer; // Thêm import này

import com.allies.app.security.StompJwtInterceptor; // Thêm import này

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final StompJwtInterceptor stompJwtInterceptor; // Khai báo

    // Constructor Injection (Spring sẽ tự động tiêm StompJwtInterceptor vào)
    public WebSocketConfig(StompJwtInterceptor stompJwtInterceptor) {
        this.stompJwtInterceptor = stompJwtInterceptor;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
    
    // BỔ SUNG: Đăng ký Interceptor để bảo mật Input Channel
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompJwtInterceptor);
    }
}