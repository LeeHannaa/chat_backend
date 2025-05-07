package com.ddhouse.chat.config;

import com.ddhouse.chat.service.MessageUnreadService;
import com.ddhouse.chat.service.RoomUserCountService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class WebSocketEventListener {
    private final RoomUserCountService roomUserCountService;
    private final MessageUnreadService messageUnreadService;
    @Autowired
    private SimpMessageSendingOperations messagingTemplate;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

        String roomId = accessor.getFirstNativeHeader("roomId");
        String userId = accessor.getFirstNativeHeader("myId");

        if (roomId != null) {
            roomUserCountService.addUserInChatRoom(roomId, userId);
            accessor.getSessionAttributes().put("roomId", roomId);
            accessor.getSessionAttributes().put("myId", userId);
            System.out.println("✅ 사용자 입장: " + roomId + ", 접속자 id : " + userId);
            int userCount = roomUserCountService.getUserCount(Long.valueOf(roomId));
            // TODO : 상대방 입장 시 상대가 해당 채팅방에서 읽지 않았던 메시지 개수만큼 정보 전달!
            Long NumberToBeRead = messageUnreadService.getUnreadMessageCount(roomId.toString(), userId.toString());
            if (userCount >= 2) {
                Map<String, Object> infoMessage = Map.of(
                        "type", "INFO",
                        "message", NumberToBeRead
                );
                messagingTemplate.convertAndSend("/topic/chatroom/" + roomId, infoMessage);
            }
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String roomId = (String) accessor.getSessionAttributes().get("roomId");
        String userId = (String) accessor.getSessionAttributes().get("myId");
        System.out.println("채팅방을 나갈때 myID를 확인해봦!!!!" + userId);

        Map<String, Object> outMessage = Map.of(
                "type", "OUT",
                "message", "상대방 퇴장"
        );
        if (roomId != null) {
            roomUserCountService.outUserInChatRoom(roomId, userId);
            messagingTemplate.convertAndSend("/topic/chatroom/" + roomId, outMessage);
            System.out.println("👋 사용자 퇴장: " + roomId + ", count 감소");
        }
    }
}
