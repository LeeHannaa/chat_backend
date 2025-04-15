package com.ddhouse.chat.dto.response;

import com.ddhouse.chat.domain.ChatMessage;
import com.ddhouse.chat.domain.ChatRoomMessage;
import com.ddhouse.chat.dto.request.ChatMessageRequestDto;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@SuperBuilder
public class ChatMessageResponseToFindMsgDto extends ChatMessageResponseDto{
    private UUID id;
    private String writerName;
    private Long writerId;
    private String msg;
    private LocalDateTime createdDate;

    public static ChatMessageResponseToFindMsgDto from (ChatMessage chatMessage, ChatRoomMessage chatRoomMessage) {
        return ChatMessageResponseToFindMsgDto.builder()
                .id(chatMessage.getId())
                .roomId(chatRoomMessage.getChatRoom().getId())
                .writerId(chatRoomMessage.getUser().getId())
                .writerName(chatRoomMessage.getUser().getName())
                .msg(chatMessage.getMsg())
                .createdDate(chatRoomMessage.getRegDate())
                .build();
    }

}
