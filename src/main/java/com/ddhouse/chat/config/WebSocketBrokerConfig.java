package com.ddhouse.chat.config;

import com.ddhouse.chat.handler.WebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketBrokerConfig implements WebSocketMessageBrokerConfigurer {
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) { // STOMP에서 사용하는 메시지 브로커를 설정
        registry.enableSimpleBroker("/sub"); // 메시지를 구독(수신)하는 요청 엔드포인트
        registry.setApplicationDestinationPrefixes("/pub"); // 메시지를 발행(송신)하는 엔드포인트
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry){
        registry.addEndpoint( "/chatting") // 초기 핸드셰이크에서 사용할 endpoint
                .setAllowedOrigins("*")
                .withSockJS();
    }
}
