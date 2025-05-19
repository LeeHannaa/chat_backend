package com.ddhouse.chat.dto;

import lombok.Getter;

@Getter
public class UserSessionInfo {
    private final String userId;
    private final String roomId;

    public UserSessionInfo(String userId, String roomId) {
        this.userId = userId;
        this.roomId = roomId;
    }
}
