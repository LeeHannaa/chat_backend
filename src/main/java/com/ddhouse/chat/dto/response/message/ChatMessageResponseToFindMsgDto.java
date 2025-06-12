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
    private LocalDateTime cdate;
    private MessageType type;
    private boolean isDelete;
    private int unreadCount;

    public static ChatMessageResponseToFindMsgDto from (ChatRoomMessage chatRoomMessage, int unreadCount) {
        return ChatMessageResponseToFindMsgDto.builder()
                .id(chatRoomMessage.getIdx())
                .roomId(chatRoomMessage.getChatRoom().getIdx())
                .writerId(chatRoomMessage.getUser() != null ? chatRoomMessage.getUser().getUserIdx() : null)
                .writerName(chatRoomMessage.getUser() != null ? (
                        "I".equals(chatRoomMessage.getUser().getSts()) ? chatRoomMessage.getUser().getUserId() : "알 수 없음")
                        : chatRoomMessage.getChatRoom().getPhoneNumber())
                .msg(chatRoomMessage.getMsg())
                .type(chatRoomMessage.getType())
                .isDelete(chatRoomMessage.getIsDelete())
                .cdate(chatRoomMessage.getCdate())
                .unreadCount(unreadCount)
                .build();
    }

    public static ChatMessageResponseToFindMsgDto deleteFrom (ChatRoomMessage chatRoomMessage) {
        return ChatMessageResponseToFindMsgDto.builder()
                .id(chatRoomMessage.getIdx())
                .roomId(chatRoomMessage.getChatRoom().getIdx())
                .writerId(chatRoomMessage.getUser().getUserIdx())
                .writerName(chatRoomMessage.getUser().getUserId())
                .isDelete(chatRoomMessage.getIsDelete()) // true면 해당 유저가 다시 초대 되었다는 뜻!!!
                .msg(chatRoomMessage.getMsg())
                .type(chatRoomMessage.getType())
                .cdate(chatRoomMessage.getCdate())
                .build();
    }

    public static ChatMessageResponseToFindMsgDto fromAllDelete (ChatRoomMessage chatRoomMessage, int unreadCount) {
        return ChatMessageResponseToFindMsgDto.builder()
                .id(chatRoomMessage.getIdx())
                .roomId(chatRoomMessage.getChatRoom().getIdx())
                .writerId(chatRoomMessage.getUser().getUserIdx())
                .writerName(chatRoomMessage.getUser().getUserId())
                .msg("삭제된 메시지입니다.")
                .type(chatRoomMessage.getType())
                .isDelete(chatRoomMessage.getIsDelete())
                .cdate(chatRoomMessage.getCdate())
                .unreadCount(unreadCount)
                .build();
    }

}
