package com.ddhouse.chat.dto;

import com.ddhouse.chat.dto.request.message.GuestMessageRequestDto;
import com.ddhouse.chat.vo.User;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Getter
@Setter
@Builder
public class ChatRoomCreateDto {
    // 채팅방을 처음 생성하는 경우 (일단 비회원)
    private User user;
    private int memberNum;
    private String phoneNumber; // 비회원 매물 문의 시 등록
    private String lastMsg;
    private LocalDateTime updateLastMsgTime;
    private LocalDateTime regDate;

    // 처음 채팅방을 생성할 때 (문의 -> 채팅방 생성)
    public static ChatRoomCreateDto guest(GuestMessageRequestDto guestMessageRequestDto, User user) {
        return ChatRoomCreateDto.builder()
                .user(user)
                .memberNum(1)
                .phoneNumber(guestMessageRequestDto.getPhoneNumber())
                .updateLastMsgTime(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
                .regDate(guestMessageRequestDto.getRegDate())
                .lastMsg(guestMessageRequestDto.getNoteText())
                .build();
    }

}
