package com.ddhouse.chat.handler;

import com.ddhouse.chat.dto.NotificationEvent;
import com.ddhouse.chat.service.MessageUnreadService;
import com.ddhouse.chat.service.RoomUserCountService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class StompChannelInterceptor implements ChannelInterceptor {
    private final RoomUserCountService roomUserCountService;
    private final MessageUnreadService messageUnreadService;
    private final ApplicationEventPublisher eventPublisher;
    private final Map<String, String> subscriptionIdToUserId = new ConcurrentHashMap<>();

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        String subscriptionId = accessor.getSubscriptionId();
        String userId = accessor.getFirstNativeHeader("myId");
        String roomId = extractRoomIdFromDestination(accessor.getDestination()); // 예: /topic/chatroom/123

        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            // 구독 (입장)
            if (roomId != null && userId != null) {
                subscriptionIdToUserId.put(subscriptionId, userId);
                roomUserCountService.addUserInChatRoom(roomId, userId);
                Long unread = messageUnreadService.getUnreadMessageCount(roomId, userId);

                if (roomUserCountService.getUserCount(Long.valueOf(roomId)) >= 2) {
                    // 순환 참조 문제로 인해 EventListener로 진행
                    eventPublisher.publishEvent(new NotificationEvent(roomId, unread, NotificationEvent.NotificationType.ENTER));
                }
                if (unread > 0) {
                    messageUnreadService.removeUnread(roomId, userId);
                }
                System.out.println("✅ 구독: " + roomId + ", 유저: " + userId);
            }

        } else if (StompCommand.UNSUBSCRIBE.equals(accessor.getCommand())) {
            subscriptionId = accessor.getSubscriptionId();
            roomId = subscriptionId.replace("chatroom-", "");
            userId = subscriptionIdToUserId.get(subscriptionId);
            System.out.println("userId랑 roomId 항상 확인해보기 : " + userId + ", " + roomId);

            // 구독 취소 (퇴장)
            if (roomId != null && userId != null) {
                roomUserCountService.outUserInChatRoom(roomId, userId);
                // 순환 참조 문제로 인해 EventListener로 진행
                eventPublisher.publishEvent(new NotificationEvent(roomId, null, NotificationEvent.NotificationType.LEAVE));
                System.out.println("👋 구독 취소: " + roomId + ", 유저: " + userId);
            }
        }

        return message;
    }

    private String extractRoomIdFromDestination(String destination) {
        if (destination != null && destination.startsWith("/topic/chatroom/")) {
            return destination.substring("/topic/chatroom/".length());
        }
        return null;
    }
}

