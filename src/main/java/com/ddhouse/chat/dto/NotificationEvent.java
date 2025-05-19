package com.ddhouse.chat.dto;

import lombok.Getter;

@Getter
public class NotificationEvent {
    private final String roomId;
    private final String userId;
    private final Long unread;
    private final NotificationType type;
    public enum NotificationType {
        ENTER, LEAVE
    }

    public NotificationEvent(String roomId, String userId, Long unread, NotificationType type) {
        this.roomId = roomId;
        this.userId = userId;
        this.unread = unread;
        this.type = type;
    }

}

