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
    private Long userIdx;
    private String userId;  // 비유저 전화번호 쪽지위해 임시 데이터
    private String sts; // I, D
    private LocalDateTime cdate;
}
