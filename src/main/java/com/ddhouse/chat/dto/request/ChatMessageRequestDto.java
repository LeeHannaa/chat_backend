package com.ddhouse.chat.dto.request;

import com.ddhouse.chat.domain.ChatMessage;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Builder
public class ChatMessageRequestDto {
    private Long roomId;
    private Long writerId;
    private String msg;

    public static ChatMessageRequestDto from (ChatMessage chatMessage) {
        return ChatMessageRequestDto.builder()
                .roomId(chatMessage.getRoomId())
                .writerId(chatMessage.getWriterId())
                .msg(chatMessage.getMsg())
                .build();
    }
}
