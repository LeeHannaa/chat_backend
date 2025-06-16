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
    private Long idx;
    private Boolean isInRoom;
    private LocalDateTime entryTime;
    private LocalDateTime cdate;
    private User user;
    private ChatRoom chatRoom;

    public static UserChatRoom from(ChatRoomDto dto, ChatRoom chatRoom) {
        return UserChatRoom.builder()
                .isInRoom(Boolean.TRUE)
                .entryTime(chatRoom.getCdate())
                .user(dto.getUser())
                .chatRoom(chatRoom)
                .cdate(chatRoom.getCdate())
                .build();
    }

    public static UserChatRoom person(ChatRoomDto dto, ChatRoom chatRoom) {
        return UserChatRoom.builder()
                .isInRoom(Boolean.TRUE)
                .entryTime(chatRoom.getCdate())
                .user(dto.getUser())
                .cdate(chatRoom.getCdate())
                .chatRoom(chatRoom)
                .build();
    }
    public static UserChatRoom from(ChatRoomCreateDto chatRoomCreateDto, ChatRoom chatRoom) {
        // 비회원이 매물 문의했을 때 채팅 방이 생성되는 경우
        return UserChatRoom.builder()
                .isInRoom(Boolean.TRUE)
                .entryTime(chatRoom.getCdate())
                .user(chatRoomCreateDto.getUser())
                .cdate(chatRoom.getCdate())
                .chatRoom(chatRoom)
                .build();
    }

    public static UserChatRoom group(ChatRoom chatRoom, User user){
        return UserChatRoom.builder()
                .isInRoom(Boolean.TRUE)
                .entryTime(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
                .user(user)
                .cdate(chatRoom.getCdate())
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
