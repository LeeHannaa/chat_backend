package com.ddhouse.chat.dto;

import com.ddhouse.chat.domain.ChatMessage;
import com.ddhouse.chat.domain.ChatRoom;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;


@Getter
@Setter
@Builder
public class ChatMessageDto {
    private UUID id;
    private Long roomId;
    private String writerName;
    private Long writerId;
    private String msg;
    private Date createdDate;

    public static ChatMessageDto from (ChatMessage chatMessage) {
        return ChatMessageDto.builder()
                .id(chatMessage.getId())
                .roomId(chatMessage.getRoomId())
                .writerId(chatMessage.getWriterId())
                .msg(chatMessage.getMsg())
                .createdDate(chatMessage.getCreatedDate())
                .build();
    }
}
