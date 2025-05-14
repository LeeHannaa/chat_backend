package com.ddhouse.chat.handler;

import com.ddhouse.chat.dto.NotificationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final SimpMessageSendingOperations messagingTemplate;

    @EventListener
    public void handleNotification(NotificationEvent event) {
        if (event.getType() == NotificationEvent.NotificationType.ENTER) {
            messagingTemplate.convertAndSend("/topic/chatroom/" + event.getRoomId(), Map.of(
                    "type", "INFO",
                    "message", event.getUnread()
            ));
        } else if (event.getType() == NotificationEvent.NotificationType.LEAVE) {
            messagingTemplate.convertAndSend("/topic/chatroom/" + event.getRoomId(), Map.of(
                    "type", "OUT",
                    "message", "상대방 퇴장"
            ));
        }
    }
}
