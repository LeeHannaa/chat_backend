package com.ddhouse.chat.dto.response.message;

import com.ddhouse.chat.vo.UserChatRoom;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter
@Setter
@SuperBuilder
public class ChatMessageResponseCreateDto extends ChatMessageResponseDto{
    // 처음 방 생성한 경우
    private String roomName;
    private LocalDateTime cdate;
    private int memberNum;
    private Boolean isGroup;


    public static ChatMessageResponseCreateDto create (UserChatRoom userChatRoom) {
        return ChatMessageResponseCreateDto.builder()
                .roomId(userChatRoom.getChatRoom().getIdx())
                .roomName(userChatRoom.getChatRoom().getName())
                .memberNum(2)
                .isGroup(userChatRoom.getChatRoom().getIsGroup())
                .cdate(userChatRoom.getCdate())
                .build();
    }
}
