package com.ddhouse.chat.dto.response.message;

import com.ddhouse.chat.vo.ChatRoomMessage;
import com.ddhouse.chat.vo.MessageType;
import com.ddhouse.chat.dto.request.message.ChatMessageRequestDto;
import com.ddhouse.chat.dto.request.message.GuestMessageRequestDto;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

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
    private LocalDateTime createdDate;
    private int unreadCount;

    public static ChatMessageResponseToChatRoomDto from (ChatRoomMessage chatRoomMessage, String userName, ChatMessageRequestDto chatMessageRequestDto, int unreadCount, MessageType messageType) {
        return ChatMessageResponseToChatRoomDto.builder()
                .id(chatRoomMessage.getId())
                .chatName(userName != null ? userName : chatMessageRequestDto.getChatName())
                .roomId(chatMessageRequestDto.getRoomId())
                .writerId(chatMessageRequestDto.getWriterId())
                .writerName(chatMessageRequestDto.getWriterName())
                .type(messageType)
                .msg(chatRoomMessage.getMsg())
                .createdDate(chatMessageRequestDto.getRegDate())
                .unreadCount(unreadCount)
                .build();
    }

    public static ChatMessageResponseToChatRoomDto guest (GuestMessageRequestDto guestMessageRequestDto, Long roomId, MessageType messageType, ChatRoomMessage chatRoomMessage) {
        return ChatMessageResponseToChatRoomDto.builder()
                .id(chatRoomMessage.getId())
                .chatName(guestMessageRequestDto.getPhoneNumber())
                .roomId(roomId)
                .writerName(guestMessageRequestDto.getPhoneNumber())
                .type(messageType)
                .msg(chatRoomMessage.getMsg())
                .createdDate(guestMessageRequestDto.getRegDate())
                .unreadCount(0)
                .build();
    }

    public static ChatMessageResponseToChatRoomDto deleteInviteFrom (ChatRoomMessage chatRoomMessage, String msg, Long beforeMsgId) {
        return ChatMessageResponseToChatRoomDto.builder()
                .id(chatRoomMessage.getId())
                .chatName(chatRoomMessage.getChatRoom().getName())
                .roomId(chatRoomMessage.getChatRoom().getId())
                .writerId(chatRoomMessage.getUser().getId())
                .writerName(chatRoomMessage.getUser().getName())
                .type(chatRoomMessage.getType())
                .msg(msg)
                .beforeMsgId(beforeMsgId)
                .createdDate(chatRoomMessage.getRegDate())
//                .unreadCount(unreadCount)
                .build();
    }

    public static ChatMessageResponseToChatRoomDto deleteFrom (ChatRoomMessage chatRoomMessage, String msg) {
        return ChatMessageResponseToChatRoomDto.builder()
                .id(chatRoomMessage.getId())
                .chatName(chatRoomMessage.getChatRoom().getName())
                .roomId(chatRoomMessage.getChatRoom().getId())
                .writerId(chatRoomMessage.getUser().getId())
                .writerName(chatRoomMessage.getUser().getName())
                .type(chatRoomMessage.getType())
                .msg(msg)
                .createdDate(chatRoomMessage.getRegDate())
//                .unreadCount(unreadCount)
                .build();
    }
}
