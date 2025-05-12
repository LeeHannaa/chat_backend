package com.ddhouse.chat.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Getter
@Setter
@Builder
public class ChatRoomUpdateDto {
    private Long roomId;
    private String name;
    private String lastMsg;
    private int memberNum;
    private LocalDateTime updateLastMsgTime;
    private Long unreadCount;

    public static ChatRoomUpdateDto from (ChatMessageRequestDto chatMessageRequestDto, Long count, int memberNum) {
        return ChatRoomUpdateDto.builder()
                .roomId(chatMessageRequestDto.getRoomId())
                .name(chatMessageRequestDto.getChatName())
                .lastMsg(chatMessageRequestDto.getMsg())
                .memberNum(memberNum)
                .updateLastMsgTime(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
                .unreadCount(count)
                .build();
    }

}
