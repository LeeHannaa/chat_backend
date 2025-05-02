package com.ddhouse.chat.dto.response;

import com.ddhouse.chat.domain.ChatRoom;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class ChatRoomInfoResponseDto {
    private Long roomId;
    private String name;
    private int memberNum;
    private String lastMsg; // controller에서 지정
    private LocalDateTime updateLastMsgTime; // controller에서 지정
    private Long unreadCount; // controller에서 지정

    public static ChatRoomInfoResponseDto create(ChatRoom chatRoom) {
        return ChatRoomInfoResponseDto.builder()
                .roomId(chatRoom.getId())
                .name(chatRoom.getName())
                .memberNum(chatRoom.getMemberNum())
                .build();
    }
}
