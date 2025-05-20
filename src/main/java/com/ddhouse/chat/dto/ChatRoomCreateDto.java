package com.ddhouse.chat.dto;

import com.ddhouse.chat.domain.ChatRoom;
import com.ddhouse.chat.domain.User;
import com.ddhouse.chat.dto.request.GuestMessageRequestDto;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Getter
@Setter
@Builder
public class ChatRoomCreateDto {
    private User user;
    private int memberNum;
    private String phoneNumber; // 비회원 매물 문의 시 등록
    private String lastMsg;
    private LocalDateTime updateLastMsgTime;

    // 처음 채팅방을 생성할 때 (문의 -> 채팅방 생성)
    public static ChatRoomCreateDto to(GuestMessageRequestDto guestMessageRequestDto, User user) {
        return ChatRoomCreateDto.builder()
                .user(user)
                .memberNum(2)
                .phoneNumber(guestMessageRequestDto.getPhoneNumber())
                .updateLastMsgTime(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
                .lastMsg(guestMessageRequestDto.getNoteText())
                .build();
    }
}
