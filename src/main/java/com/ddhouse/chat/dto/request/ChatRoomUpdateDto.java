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
public class ChatRoomUpdateDto {
    private Long roomId;
    private String msg;
    private String writerName;
    private LocalDateTime regDate;
    private LocalDateTime updateLastMsgTime;

    public static ChatRoomUpdateDto from (ChatMessageRequestDto chatMessageRequestDto) {
        return ChatRoomUpdateDto.builder()
                .roomId(chatMessageRequestDto.getRoomId())
                .msg(chatMessageRequestDto.getMsg())
                .writerName(chatMessageRequestDto.getWriterName())
                .regDate(chatMessageRequestDto.getRegDate())
                .updateLastMsgTime(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
                .build();
    }

}
