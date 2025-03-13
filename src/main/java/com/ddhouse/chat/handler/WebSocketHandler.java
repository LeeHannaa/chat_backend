package com.ddhouse.chat.handler;

import com.ddhouse.chat.service.ChatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketHandler extends TextWebSocketHandler {
    private final ObjectMapper objectMapper;
    private final ChatService chatService;
    private static final ConcurrentHashMap<String, WebSocketSession> CLIENTS
             = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception{ // 사용자가 웹소켓 서버에 접속하면 동작
        System.out.print("웹소켓 연결됨: {}" +  session.getId());
        CLIENTS.put(session.getId(), session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception { // 웹소켓 서버에 접속이 끝난 경우
        System.out.println("웹소켓 연결 종료: {}" + session.getId());
        CLIENTS.remove(session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception { // 사용자의 메시지를 받게 된 경우
//        String id = session.getId();  // 메시지를 보낸 아이디
        log.info("서버에서 받은 메시지 : ", message.getPayload());
//        CLIENTS.entrySet().forEach( arg->{
//            if(!arg.getKey().equals(id)) {  // 같은 아이디가 아니면 메시지를 전달
//                try {
//                    arg.getValue().sendMessage(message);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//        String payload = message.getPayload();
//        ChatMessage chatMessage = objectMapper.readValue(payload, ChatMessage.class);
//        ChatRoomDto room = chatService.findRoomById(chatMessage.getRoomId());
//        room.handleActions(session, chatMessage, chatService);
    }
}
