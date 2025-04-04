package com.ddhouse.chat.fcm.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class FcmTokenSaveRequest {
    Long userId;
    String fcmToken;
}
