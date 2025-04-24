package com.ddhouse.chat.dto.response.ChatMessage;

import com.ddhouse.chat.domain.ChatMessage;
import com.ddhouse.chat.domain.ChatRoomMessage;
import com.ddhouse.chat.domain.MessageType;
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
    private MessageType type;
    private boolean isDelete;
    private int unreadCount;

    public static ChatMessageResponseToFindMsgDto from (ChatMessage chatMessage, ChatRoomMessage chatRoomMessage, int unreadCount) {
        return ChatMessageResponseToFindMsgDto.builder()
                .id(chatMessage.getId())
                .roomId(chatRoomMessage.getChatRoom().getId())
                .writerId(chatRoomMessage.getUser().getId())
                .writerName(chatRoomMessage.getUser().getName())
                .msg(chatMessage.getMsg())
                .type(chatRoomMessage.getType())
                .isDelete(chatRoomMessage.getIsDelete())
                .createdDate(chatRoomMessage.getRegDate())
                .unreadCount(unreadCount)
                .build();
    }

    public static ChatMessageResponseToFindMsgDto fromAllDelete (ChatMessage chatMessage, ChatRoomMessage chatRoomMessage, int unreadCount) {
        return ChatMessageResponseToFindMsgDto.builder()
                .id(chatMessage.getId())
                .roomId(chatRoomMessage.getChatRoom().getId())
                .writerId(chatRoomMessage.getUser().getId())
                .writerName(chatRoomMessage.getUser().getName())
                .msg("삭제된 메시지입니다.")
                .type(chatRoomMessage.getType())
                .isDelete(chatRoomMessage.getIsDelete())
                .createdDate(chatRoomMessage.getRegDate())
                .unreadCount(unreadCount)
                .build();
    }

}
