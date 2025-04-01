package com.ddhouse.chat.dto.response;

import com.ddhouse.chat.domain.ChatMessage;
import com.ddhouse.chat.domain.UserChatRoom;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;


@Getter
@Setter
@Builder
public class ChatMessageResponseDto {
    private UUID id;
    private Long roomId;
    private String writerName;
    private Long writerId;
    private String msg;
    // TODO : 날짜 타입 전체적으로 통일시키기
    private Date createdDate;

    // 처음 방 생성한 경우
    private String roomName;
    private LocalDateTime regDate;
    private int memberNum;

    public static ChatMessageResponseDto from (ChatMessage chatMessage) {
        return ChatMessageResponseDto.builder()
                .id(chatMessage.getId())
                .roomId(chatMessage.getRoomId())
                .writerId(chatMessage.getWriterId())
                .writerName(chatMessage.getWriterName())
                .msg(chatMessage.getMsg())
                .createdDate(chatMessage.getCreatedDate())
                .build();
    }

    public static ChatMessageResponseDto create (UserChatRoom userChatRoom) {
        return ChatMessageResponseDto.builder()
                .roomId(userChatRoom.getChatRoom().getId())
                .roomName(userChatRoom.getChatRoom().getName())
                .memberNum(2)
                .regDate(userChatRoom.getRegDate())
                .build();
    }
}
