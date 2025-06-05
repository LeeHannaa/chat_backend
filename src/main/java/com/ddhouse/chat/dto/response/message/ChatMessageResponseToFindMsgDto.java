package com.ddhouse.chat.dto.response.message;

import com.ddhouse.chat.vo.ChatRoomMessage;
import com.ddhouse.chat.vo.MessageType;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import java.time.LocalDateTime;


@Getter
@Setter
@SuperBuilder
public class ChatMessageResponseToFindMsgDto extends ChatMessageResponseDto{
    private Long id;
    private String writerName;
    private Long writerId;
    private String msg;
    private LocalDateTime createdDate;
    private MessageType type;
    private boolean isDelete;
    private int unreadCount;

    public static ChatMessageResponseToFindMsgDto from (ChatRoomMessage chatRoomMessage, int unreadCount) {
        return ChatMessageResponseToFindMsgDto.builder()
                .id(chatRoomMessage.getId())
                .roomId(chatRoomMessage.getChatRoom().getId())
                .writerId(chatRoomMessage.getUser() != null ? chatRoomMessage.getUser().getId() : null)
                .writerName(chatRoomMessage.getUser() != null ? chatRoomMessage.getUser().getName() : chatRoomMessage.getChatRoom().getPhoneNumber())
                .msg(chatRoomMessage.getMsg())
                .type(chatRoomMessage.getType())
                .isDelete(chatRoomMessage.getIsDelete())
                .createdDate(chatRoomMessage.getRegDate())
                .unreadCount(unreadCount)
                .build();
    }

    public static ChatMessageResponseToFindMsgDto deleteFrom (ChatRoomMessage chatRoomMessage) {
        return ChatMessageResponseToFindMsgDto.builder()
                .id(chatRoomMessage.getId())
                .roomId(chatRoomMessage.getChatRoom().getId())
                .writerId(chatRoomMessage.getUser().getId())
                .writerName(chatRoomMessage.getUser().getName())
                .isDelete(chatRoomMessage.getIsDelete()) // true면 해당 유저가 다시 초대 되었다는 뜻!!!
                .msg(chatRoomMessage.getMsg())
                .type(chatRoomMessage.getType())
                .createdDate(chatRoomMessage.getRegDate())
                .build();
    }

    public static ChatMessageResponseToFindMsgDto fromAllDelete (ChatRoomMessage chatRoomMessage, int unreadCount) {
        return ChatMessageResponseToFindMsgDto.builder()
                .id(chatRoomMessage.getId())
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
