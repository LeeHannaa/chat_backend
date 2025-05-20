package com.ddhouse.chat.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Getter
@Setter
@Builder
public class ChatRoomListUpdateDto {
    private Long roomId;
    private String name;
    private String lastMsg;
    private int memberNum;
    private LocalDateTime updateLastMsgTime;
    private Long unreadCount;

    public static ChatRoomListUpdateDto from (ChatMessageRequestDto chatMessageRequestDto, Long count, int memberNum) {
        return ChatRoomListUpdateDto.builder()
                .roomId(chatMessageRequestDto.getRoomId())
                .name(chatMessageRequestDto.getChatName())
                .lastMsg(chatMessageRequestDto.getMsg())
                .memberNum(memberNum)
                .updateLastMsgTime(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
                .unreadCount(count)
                .build();
    }

    // TODO
    public static ChatRoomListUpdateDto guest (ChatMessageRequestDto chatMessageRequestDto, Long count, int memberNum) {
        return ChatRoomListUpdateDto.builder()
                .roomId(chatMessageRequestDto.getRoomId())
                .name(chatMessageRequestDto.getChatName())
                .lastMsg(chatMessageRequestDto.getMsg())
                .memberNum(memberNum)
                .updateLastMsgTime(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
                .unreadCount(count)
                .build();
    }

}
