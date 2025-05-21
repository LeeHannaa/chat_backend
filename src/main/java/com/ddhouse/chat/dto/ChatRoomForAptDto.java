package com.ddhouse.chat.dto;

import com.ddhouse.chat.domain.Apt;
import com.ddhouse.chat.domain.ChatRoom;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Builder
public class ChatRoomForAptDto {
    private Long roomId;
    private Apt apt;

    public static ChatRoomForAptDto from(ChatRoom chatRoom) {
        return ChatRoomForAptDto.builder()
                .roomId(chatRoom.getId())
                .apt(chatRoom.getApt())
                .build();
    }
}
