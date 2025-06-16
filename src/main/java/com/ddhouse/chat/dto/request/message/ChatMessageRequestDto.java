package com.ddhouse.chat.dto.request.message;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;


@Getter
@Setter
@Builder
public class ChatMessageRequestDto{
    // 유저가 채팅방에서 채팅을 서버로 전송할 때
    private Long roomId;
    private Long aptId;
    private String chatName;
    private Long writerId;
    private String writerName;
    private String msg;
    private LocalDateTime cdate;

    public void addRoomId(Long roomId) {
        this.roomId = roomId;
    }
}
