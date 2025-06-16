package com.ddhouse.chat.dto;

import com.ddhouse.chat.dto.request.message.ChatMessageRequestDto;
import com.ddhouse.chat.dto.request.message.GuestMessageRequestDto;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class SaveMessageDto {
    // 회원이든, 비회원이든 전송한 메시지를 저장하기 위해 필요한 변수들
    private Long roomId;
    private Long writerId;
    private String msg;
    private LocalDateTime cdate;

    public static SaveMessageDto from (ChatMessageRequestDto chatMessageRequestDto) {
        return SaveMessageDto.builder()
                .roomId(chatMessageRequestDto.getRoomId())
                .writerId(chatMessageRequestDto.getWriterId())
                .msg(chatMessageRequestDto.getMsg())
                .cdate(chatMessageRequestDto.getCdate())
                .build();
    }

    public static SaveMessageDto guest (GuestMessageRequestDto guestMessageRequestDto, Long roomId) {
        return SaveMessageDto.builder()
                .roomId(roomId)
                .msg(guestMessageRequestDto.getNoteText())
                .cdate(guestMessageRequestDto.getCdate())
                .build();
    }
}
