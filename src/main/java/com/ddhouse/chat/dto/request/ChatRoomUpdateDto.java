package com.ddhouse.chat.dto.request;

import com.ddhouse.chat.domain.ChatMessage;
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
    private String chatName;
    private String msg;
    private String writerName;
    private int memberNum;
    private LocalDateTime regDate;
    private LocalDateTime updateLastMsgTime;
    private Long unreadCount;

    public static ChatRoomUpdateDto from (ChatMessageRequestDto chatMessageRequestDto, Long count, int memberNum) {
        return ChatRoomUpdateDto.builder()
                .roomId(chatMessageRequestDto.getRoomId())
                .chatName(chatMessageRequestDto.getChatName())
                .msg(chatMessageRequestDto.getMsg())
                .writerName(chatMessageRequestDto.getWriterName())
                .memberNum(memberNum)
                .regDate(chatMessageRequestDto.getRegDate())
                .updateLastMsgTime(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
                .unreadCount(count)
                .build();
    }

}
