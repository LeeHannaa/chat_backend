package com.ddhouse.chat.config;

import com.ddhouse.chat.handler.StompHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketBrokerConfig implements WebSocketMessageBrokerConfigurer {
 private final StompHandler stompHandler;


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
  registry.enableSimpleBroker("/topic"); // 메시지를 받을 때 경, “/queue”, “/topic”가 api에 prefix로 붙은 경우, messageBroker가 해당 경로를 가로챔
  registry.setApplicationDestinationPrefixes("/app"); // 메시지를 보낼 경로, 경로 앞에 “/app”이 붙어있으면 Broker로 보내짐.
 }

 @Override
 public void configureClientInboundChannel(ChannelRegistration registration) {
  registration.interceptors(stompHandler); // 여기서 interceptor 등록!
 }

}