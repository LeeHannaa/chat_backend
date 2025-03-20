package com.ddhouse.chat.dto.response;

import com.ddhouse.chat.domain.ChatMessage;
import com.ddhouse.chat.domain.UserChatRoom;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;


@Getter
@Setter
@Builder
public class ChatMessageResponseDto {
    private UUID id;
    private Long roomId;
    private String writerName;
    private Long writerId;
    private String msg;
    private Date createdDate;

    public static ChatMessageResponseDto from (ChatMessage chatMessage) {
        return ChatMessageResponseDto.builder()
                .id(chatMessage.getId())
                .roomId(chatMessage.getRoomId())
                .writerId(chatMessage.getWriterId())
                .msg(chatMessage.getMsg())
                .createdDate(chatMessage.getCreatedDate())
                .build();
    }

    public static ChatMessageResponseDto create (UserChatRoom userChatRoom) {
        return ChatMessageResponseDto.builder()
                .roomId(userChatRoom.getChatRoom().getId())
                .build();
    }
}
