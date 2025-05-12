package com.ddhouse.chat.dto.request;

import com.ddhouse.chat.domain.ChatRoom;
import com.ddhouse.chat.domain.User;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;


@Getter
@Setter
@Builder
public class ChatMessageRequestDto{
    private Long roomId;
    private String chatName;
    private Long writerId;
    private String writerName;
    private String msg;
    private LocalDateTime regDate;

    public static ChatMessageRequestDto from(ChatRoom chatRoom, User user, String text) {
        return ChatMessageRequestDto.builder()
                .roomId(chatRoom.getId())
                .chatName(chatRoom.getName())
                .writerId(user.getId())
                .writerName(user.getName())
                .msg(text)
                .build();
    }
}
