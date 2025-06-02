package com.ddhouse.chat.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Long id;
    private String name;  // 비유저 전화번호 쪽지위해 임시 데이터
    private String fcmToken;
    private LocalDateTime regDate;

    public void setUpdateFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }
}
