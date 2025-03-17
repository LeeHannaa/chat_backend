package com.ddhouse.chat.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketBrokerConfig implements WebSocketMessageBrokerConfigurer {

 @Override
 public void registerStompEndpoints(StompEndpointRegistry registry) {
  registry.addEndpoint("/ws-stomp")
          .setAllowedOrigins("*"); // 보안 강화
//          .setAllowedOriginPatterns("*")
//          .withSockJS();

  //Client에서 websocket 연결할 때 사용할 API 경로를 설정 - 채팅용
//  registry.addEndpoint("/chat")
//          .setAllowedOriginPatterns("*")
//          .withSockJS();
  //Client에서 websocket 연결할 때 사용할 API 경로를 설정 - 매칭용
//  registry.addEndpoint("/match")
//          .setAllowedOriginPatterns("*")
//          .withSockJS();


 }

 @Override
 public void configureMessageBroker(MessageBrokerRegistry registry) {
  registry.enableSimpleBroker("/topic"); // 구독할 경로 설정
  registry.setApplicationDestinationPrefixes("/app"); // 클라이언트에서 보낼 경로
 }
}