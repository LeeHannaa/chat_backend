package com.ddhouse.chat.dto.response.ChatMessage;

import com.ddhouse.chat.domain.ChatMessage;
import com.ddhouse.chat.domain.ChatRoomMessage;
import com.ddhouse.chat.domain.MessageType;
import com.ddhouse.chat.dto.request.ChatMessageRequestDto;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@SuperBuilder
public class ChatMessageResponseToChatRoomDto extends ChatMessageResponseDto{
    private UUID id;
    private String chatName;
    private String writerName;
    private Long writerId;
    private MessageType type;
    private String msg;
    private UUID beforeMsgId;
    private LocalDateTime createdDate;
    private int unreadCount;

    public static ChatMessageResponseToChatRoomDto from (ChatMessage chatMessage, ChatMessageRequestDto chatMessageRequestDto, int unreadCount, MessageType messageType) {
        return ChatMessageResponseToChatRoomDto.builder()
                .id(chatMessage.getId())
                .chatName(chatMessageRequestDto.getChatName())
                .roomId(chatMessageRequestDto.getRoomId())
                .writerId(chatMessageRequestDto.getWriterId())
                .writerName(chatMessageRequestDto.getWriterName())
                .type(messageType)
                .msg(chatMessage.getMsg())
                .createdDate(chatMessageRequestDto.getRegDate())
                .unreadCount(unreadCount)
                .build();
    }

    public static ChatMessageResponseToChatRoomDto deleteInviteFrom (ChatRoomMessage chatRoomMessage, String msg, UUID beforeMsgId) {
        return ChatMessageResponseToChatRoomDto.builder()
                .id(chatRoomMessage.getMessageId())
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
                .id(chatRoomMessage.getMessageId())
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
