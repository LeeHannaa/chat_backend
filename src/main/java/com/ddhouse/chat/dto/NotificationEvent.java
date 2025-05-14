package com.ddhouse.chat.dto;

public class NotificationEvent {
    private final String roomId;
    private final Long unread;
    private final NotificationType type;
    public enum NotificationType {
        ENTER, LEAVE
    }

    public NotificationEvent(String roomId, Long unread, NotificationType type) {
        this.roomId = roomId;
        this.unread = unread;
        this.type = type;
    }
    public String getRoomId() { return roomId; }
    public Long getUnread() { return unread; }
    public NotificationType getType() { return type; }

}

