package com.ddhouse.chat.dto.request;

import com.ddhouse.chat.domain.ChatMessage;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Getter
@Setter
@Builder
public class ChatRoomUpdateDto {
    private Long roomId;
    private String msg;
    private String writerName;
    private LocalDateTime regDate;
    // TODO : 나중에 업데이트 시간도 추가할 것


    public static ChatRoomUpdateDto from (ChatMessageRequestDto chatMessageRequestDto) {
        return ChatRoomUpdateDto.builder()
                .roomId(chatMessageRequestDto.getRoomId())
                .msg(chatMessageRequestDto.getMsg())
                .writerName(chatMessageRequestDto.getWriterName())
                .regDate(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
                .build();
    }

}
