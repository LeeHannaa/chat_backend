package com.ddhouse.chat.dto.response.message;

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

    public static ChatMessageResponseToFindMsgDto deleteFrom (ChatMessage chatMessage, ChatRoomMessage chatRoomMessage) {
        return ChatMessageResponseToFindMsgDto.builder()
                .id(chatMessage.getId())
                .roomId(chatRoomMessage.getChatRoom().getId())
                .writerId(chatRoomMessage.getUser().getId())
                .writerName(chatRoomMessage.getUser().getName())
                .isDelete(chatRoomMessage.getIsDelete()) // true면 해당 유저가 다시 초대 되었다는 뜻!!!
                .msg(chatMessage.getMsg())
                .type(chatRoomMessage.getType())
                .createdDate(chatRoomMessage.getRegDate())
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
