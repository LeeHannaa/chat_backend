package com.ddhouse.chat.dto.request.message;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class GuestMessageRequestDto {
    // 비회원이 매물 문의 내용을 서버로 전송할 때
    private Long aptId; // 상대방을 알기 위함
    private String phoneNumber;
    private String noteText;
    private LocalDateTime regDate;
}
