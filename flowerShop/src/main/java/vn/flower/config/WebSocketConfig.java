package vn.flower.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker // Kích hoạt message broker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Endpoint kết nối (giữ nguyên)
        registry.addEndpoint("/ws").withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // === THAY ĐỔI DÒNG NÀY ===
        // Kích hoạt broker cho cả /queue (private) và /topic (public)
        // /queue là tiền tố cho các đích đến tin nhắn riêng tư
        registry.enableSimpleBroker("/queue", "/topic");
        
        // Tiền tố cho tin nhắn gửi TỪ client VÀO server (giữ nguyên)
        registry.setApplicationDestinationPrefixes("/app");

        // === THÊM DÒNG NÀY ===
        // Định nghĩa tiền tố cho các đích đến riêng tư của người dùng
        // Khi bạn gửi đến /user/somebody/queue/private, Spring sẽ
        // tự động chuyển nó đến 1 kênh riêng mà chỉ 'somebody' nhận được.
        registry.setUserDestinationPrefix("/user");
    }
}