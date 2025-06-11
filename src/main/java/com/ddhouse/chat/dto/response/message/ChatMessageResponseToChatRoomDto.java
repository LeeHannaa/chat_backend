package com.ddhouse.chat.dto.response.message;

import com.ddhouse.chat.vo.ChatRoomMessage;
import com.ddhouse.chat.vo.MessageType;
import com.ddhouse.chat.dto.request.message.ChatMessageRequestDto;
import com.ddhouse.chat.dto.request.message.GuestMessageRequestDto;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import java.time.LocalDateTime;

@Getter
@Setter
@SuperBuilder
public class ChatMessageResponseToChatRoomDto extends ChatMessageResponseDto{
    private Long id;
    private String chatName;
    private String writerName;
    private Long roomId;
    private Long writerId;
    private MessageType type;
    private String msg;
    private Long beforeMsgId;
    private LocalDateTime cdate;
    private int unreadCount;

    public static ChatMessageResponseToChatRoomDto from (ChatRoomMessage chatRoomMessage, String userName, ChatMessageRequestDto chatMessageRequestDto, int unreadCount, MessageType messageType) {
        return ChatMessageResponseToChatRoomDto.builder()
                .id(chatRoomMessage.getIdx())
                .chatName(userName != null ? userName : chatMessageRequestDto.getChatName())
                .roomId(chatMessageRequestDto.getRoomId())
                .writerId(chatMessageRequestDto.getWriterId())
                .writerName(chatMessageRequestDto.getWriterName())
                .type(messageType)
                .msg(chatRoomMessage.getMsg())
                .cdate(chatMessageRequestDto.getCdate())
                .unreadCount(unreadCount)
                .build();
    }

    public static ChatMessageResponseToChatRoomDto guest (GuestMessageRequestDto guestMessageRequestDto, Long roomId, MessageType messageType, ChatRoomMessage chatRoomMessage) {
        return ChatMessageResponseToChatRoomDto.builder()
                .id(chatRoomMessage.getIdx())
                .chatName(guestMessageRequestDto.getPhoneNumber())
                .roomId(roomId)
                .writerName(guestMessageRequestDto.getPhoneNumber())
                .type(messageType)
                .msg(chatRoomMessage.getMsg())
                .cdate(guestMessageRequestDto.getCdate())
                .unreadCount(0)
                .build();
    }

    public static ChatMessageResponseToChatRoomDto deleteInviteFrom (ChatRoomMessage chatRoomMessage, String msg, Long beforeMsgId) {
        return ChatMessageResponseToChatRoomDto.builder()
                .id(chatRoomMessage.getIdx())
                .chatName(chatRoomMessage.getChatRoom().getName())
                .roomId(chatRoomMessage.getChatRoom().getIdx())
                .writerId(chatRoomMessage.getUser().getUserIdx())
                .writerName(chatRoomMessage.getUser().getUserId())
                .type(chatRoomMessage.getType())
                .msg(msg)
                .beforeMsgId(beforeMsgId)
                .cdate(chatRoomMessage.getCdate())
//                .unreadCount(unreadCount)
                .build();
    }

    public static ChatMessageResponseToChatRoomDto deleteFrom (ChatRoomMessage chatRoomMessage, String msg) {
        return ChatMessageResponseToChatRoomDto.builder()
                .id(chatRoomMessage.getIdx())
                .chatName(chatRoomMessage.getChatRoom().getName())
                .roomId(chatRoomMessage.getChatRoom().getIdx())
                .writerId(chatRoomMessage.getUser().getUserIdx())
                .writerName(chatRoomMessage.getUser().getUserId())
                .type(chatRoomMessage.getType())
                .msg(msg)
                .cdate(chatRoomMessage.getCdate())
//                .unreadCount(unreadCount)
                .build();
    }
}
