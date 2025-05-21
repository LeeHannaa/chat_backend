package com.ddhouse.chat.domain;

import com.ddhouse.chat.BaseEntity;
import com.ddhouse.chat.dto.ChatRoomCreateDto;
import com.ddhouse.chat.dto.ChatRoomDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserChatRoom extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Boolean isInRoom;
    private LocalDateTime entryTime;

    @ManyToOne
    @JoinColumn(name = "userId", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "chatRoomId", nullable = false)
    private ChatRoom chatRoom;

    public static UserChatRoom from(ChatRoomDto dto, ChatRoom chatRoom) {
        return UserChatRoom.builder()
                .isInRoom(Boolean.TRUE)
                .entryTime(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
                .user(dto.getUser())
                .chatRoom(chatRoom)
                .build();
    }
    public static UserChatRoom otherFrom(ChatRoomDto dto, ChatRoom chatRoom) {
        return UserChatRoom.builder()
                .isInRoom(Boolean.TRUE)
                .entryTime(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
                .user(dto.getApt().getUser())
                .chatRoom(chatRoom)
                .build();
    }

    public static UserChatRoom from(ChatRoomCreateDto chatRoomCreateDto, ChatRoom chatRoom) {
        // 비회원이 매물 문의했을 때 채팅 방이 생성되는 경우
        return UserChatRoom.builder()
                .isInRoom(Boolean.TRUE)
                .entryTime(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
                .user(chatRoomCreateDto.getUser())
                .chatRoom(chatRoom)
                .build();
    }

    public static UserChatRoom group(ChatRoom chatRoom, User user){
        return UserChatRoom.builder()
                .isInRoom(Boolean.TRUE)
                .entryTime(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
                .user(user)
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

