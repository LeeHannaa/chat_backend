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
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class StompChannelInterceptor implements ChannelInterceptor {
    private final RoomUserCountService roomUserCountService;
    private final MessageUnreadService messageUnreadService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        // TODO : 소켓 연결 로직 변경하면서 해당 파트 수정 필요!
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
//        String destination = accessor.getDestination();
//        System.out.println("destination : " + destination);
        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            String subscriptionId = accessor.getFirstNativeHeader("id");
            System.out.println("subscriptionId 확인하기 입장시 : " + subscriptionId);
            String roomId = extractRoomId(subscriptionId);
            String userId = extractUserId(subscriptionId);
            // 구독 (입장)
            if (roomId != null && userId != null) {
                roomUserCountService.addUserInChatRoom(roomId, userId);
                Long unread = messageUnreadService.getUnreadMessageCount(roomId, userId);
                if (roomUserCountService.getUserCount(Long.valueOf(roomId)) >= 2) {
                    // 순환 참조 문제로 인해 EventListener로 진행
                    eventPublisher.publishEvent(new NotificationEvent(roomId, userId, unread, NotificationEvent.NotificationType.ENTER));
                }
                if (unread > 0) {
                    messageUnreadService.removeUnread(roomId, userId);
                }
                System.out.println("✅ 구독: " + roomId + ", 유저: " + userId);
            }
        } else if (StompCommand.UNSUBSCRIBE.equals(accessor.getCommand())) {
            String subscriptionId = accessor.getSubscriptionId();
            System.out.println("subscriptionId 확인하기 퇴장시 : " + subscriptionId);
            String roomId = extractRoomId(subscriptionId);
            String userId = extractUserId(subscriptionId);

            // 구독 취소 (퇴장)
            if (roomId != null && userId != null) {
                roomUserCountService.outUserInChatRoom(roomId, userId);
                // 순환 참조 문제로 인해 EventListener로 진행
                eventPublisher.publishEvent(new NotificationEvent(roomId, userId,null, NotificationEvent.NotificationType.LEAVE));
                System.out.println("👋 구독 취소: " + roomId + ", 유저: " + userId);
            }
        }

        return message;
    }

    // [해결책1] subscriptionId 예: chatroom-23-user-456
    String extractRoomId(String subscriptionId) {
        if (subscriptionId != null && subscriptionId.startsWith("chatroom-") && subscriptionId.contains("-user-")) {
            return subscriptionId.substring("chatroom-".length(), subscriptionId.indexOf("-user-"));
        }
        return null;
    }

    String extractUserId(String subscriptionId) {
        if (subscriptionId != null && subscriptionId.contains("-user-")) {
            return subscriptionId.substring(subscriptionId.indexOf("-user-") + "-user-".length());
        }
        return null;
    }

}

