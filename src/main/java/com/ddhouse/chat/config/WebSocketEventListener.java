package com.ddhouse.chat.config;

import com.ddhouse.chat.dto.response.ChatMessageResponseDto;
import com.ddhouse.chat.service.MessageUnreadService;
import com.ddhouse.chat.service.RoomUserCountService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import reactor.core.publisher.Flux;

import java.util.List;
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
        System.out.println("userId : " + userId + "roomId : " + roomId);
        if (roomId != null) {
            roomUserCountService.increaseUserCount(roomId);
            System.out.println("✅ 사용자 입장: " + roomId + ", count 증가");
            int userCount = roomUserCountService.getUserCount(Long.valueOf(roomId));
            if (userCount >= 2) {
                Map<String, Object> infoMessage = Map.of(
                        "type", "INFO",
                        "message", "상대방 입장"
                );
                messagingTemplate.convertAndSend("/topic/chatroom/" + roomId, infoMessage);
            }
            if(messageUnreadService.getUnreadCount(roomId)) {
                // 해당 채팅방에 안읽은 메시지가 존재하는 경우
                /*
                 *   2-1. 해당 방에 unread 메시지가 저장되어있을 때
                 *       2-1-1. 나의 id와 저장된 userId가 다른 경우 (내가 읽지 않은 메시지가 있다는 뜻 -> 이제 읽음 처리 된 메시지들) : redis에서 삭제 -> 채팅 내역들 보내주고
                 *       2-1-2. id가 같은 경우 : 안읽은 메시지 개수 보내주기 -> 프론트에서 해당 개수만큼 (아래부터) '안읽음' 보여주기
                 *   2-2. 해당 방에 unread 메시지가 없을 경우
                 *       2-2-1. 그냥 원래대로
                */
                // 내가 읽지 않은 메시지가 있다는 뜻 -> 이제 읽음 처리 된 메시지들 : redis에서 삭제
                Long unreadCount = messageUnreadService.getUnreadMessageCount(roomId, userId);
                if(unreadCount > 0){
                    messageUnreadService.removeUnread(roomId, userId);
                } else {
                    // 채팅방에 내가 보낸 메시지를 상대방이 안읽은 경우 : 안읽은 메시지 개수 보내주기 -> 프론트에서 해당 개수만큼 (아래부터) '안읽음' 보여주기
                    System.out.println("📝 상대방이 현재 안읽은 메시지가 있습니다!!" + messageUnreadService.getOtherUserUnreadCount(roomId) + "개");
                }
            }
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String roomId = (String) accessor.getSessionAttributes().get("roomId");
        if (roomId != null) {
            roomUserCountService.decreaseUserCount(roomId);
            System.out.println("👋 사용자 퇴장: " + roomId + ", count 감소");
        }
    }
}
