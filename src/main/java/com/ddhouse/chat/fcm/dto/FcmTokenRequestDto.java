package com.ddhouse.chat.fcm.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FcmTokenRequestDto {
    private String title;
    private String body;
}
