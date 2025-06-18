package com.ddhouse.chat.handler;

import com.ddhouse.chat.dto.NotificationEvent;
import com.ddhouse.chat.service.RoomUserCountService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final SimpMessageSendingOperations messagingTemplate;
    private final RoomUserCountService roomUserCountService;

    @Async
    @EventListener
    public void handleNotification(NotificationEvent event) {
        List<Long> userIds = roomUserCountService.getUserIdsInChatRoomIncludingMe(Long.valueOf(event.getRoomId()));
        if (event.getType() == NotificationEvent.NotificationType.ENTER) {
            userIds.forEach(userId -> {
                messagingTemplate.convertAndSend("/topic/chat/" + userId, Map.of(
                        "type", "INFO",
                        "roomId", event.getRoomId(),
                        "message", event.getUnread()
                ));
            });
        } else if (event.getType() == NotificationEvent.NotificationType.LEAVE) {
            userIds.forEach(userId -> {
                messagingTemplate.convertAndSend("/topic/chat/" + userId, Map.of(
                        "type", "OUT",
                        "roomId", event.getRoomId(),
                        "message", "상대방 퇴장"
                ));
            });
        }
    }
}
