package com.ddhouse.chat.dto.request;

import com.ddhouse.chat.domain.ChatMessage;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.ZoneId;


@Getter
@Setter
@Builder
public class ChatMessageRequestDto{ // 프론트가 백으로 주는 정보
    private Long roomId;
    private String chatName;
    private Long writerId;
    private String writerName;
    private String msg;
    private LocalDateTime regDate;
}
