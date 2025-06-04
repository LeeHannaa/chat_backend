package com.ddhouse.chat.vo;

import com.ddhouse.chat.dto.ChatRoomCreateDto;
import com.ddhouse.chat.dto.ChatRoomDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserChatRoom {
    private Long id;
    private Boolean isInRoom;
    private LocalDateTime entryTime;
    private LocalDateTime regDate;
    private User user;
    private ChatRoom chatRoom;

    public static UserChatRoom from(ChatRoomDto dto, ChatRoom chatRoom) {
        return UserChatRoom.builder()
                .isInRoom(Boolean.TRUE)
                .entryTime(dto.getRegDate())
                .user(dto.getUser())
                .chatRoom(chatRoom)
                .regDate(dto.getRegDate())
                .build();
    }

    public static UserChatRoom person(ChatRoomDto dto, ChatRoom chatRoom) {
        return UserChatRoom.builder()
                .isInRoom(Boolean.TRUE)
                .entryTime(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
                .user(dto.getUser())
                .regDate(dto.getRegDate())
                .chatRoom(chatRoom)
                .build();
    }
    public static UserChatRoom from(ChatRoomCreateDto chatRoomCreateDto, ChatRoom chatRoom) {
        // 비회원이 매물 문의했을 때 채팅 방이 생성되는 경우
        return UserChatRoom.builder()
                .isInRoom(Boolean.TRUE)
                .entryTime(chatRoom.getRegDate())
                .user(chatRoomCreateDto.getUser())
                .regDate(chatRoom.getRegDate())
                .chatRoom(chatRoom)
                .build();
    }

    public static UserChatRoom group(ChatRoom chatRoom, User user){
        return UserChatRoom.builder()
                .isInRoom(Boolean.TRUE)
                .entryTime(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
                .user(user)
                .regDate(chatRoom.getRegDate())
                .chatRoom(chatRoom)
                .build();
    }

    public void leaveTheChatRoom(){
        if(this.isInRoom) {
            this.isInRoom = Boolean.FALSE;
            this.entryTime = LocalDateTime.now(ZoneId.of("Asia/Seoul")); // 채팅방 나가는 기점을 시작으로 시간 업데이트
        }
    }

    public void reEntryInChatRoom() {
        if (!this.isInRoom) {
            this.isInRoom = Boolean.TRUE;
        }
    }
}
