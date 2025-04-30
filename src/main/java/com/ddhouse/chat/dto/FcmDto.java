package com.ddhouse.chat.dto;

import com.ddhouse.chat.domain.Apt;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class FcmDto {
    String targetToken;
    String title;
    String body;
    String roomId;
    String roomName;

    public static FcmDto chat(String targetToken, String body, String roomId, String roomName) {
        return FcmDto.builder()
                .targetToken(targetToken)
                .title("새 메시지 도착!")
                .body(body)
                .roomId(roomId)
                .roomName(roomName)
                .build();
    }

    public static FcmDto note(String targetToken, String body) {
        return FcmDto.builder()
                .targetToken(targetToken)
                .title("새로운 문의 쪽지 도착!")
                .body(body)
                .build();
    }
}
