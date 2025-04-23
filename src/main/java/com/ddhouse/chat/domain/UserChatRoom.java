package com.ddhouse.chat.domain;

import com.ddhouse.chat.BaseEntity;
import com.ddhouse.chat.dto.info.ChatRoomDto;
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
//    private Long consultId; // 매물 등록자 id
    private Boolean isInRoom; // 채팅방에 있는지
    private LocalDateTime entryTime;

    @ManyToOne
    @JoinColumn(name = "userId", nullable = false) // 매물 문의자
    private User user;

    @ManyToOne
    @JoinColumn(name = "chatRoomId", nullable = false)
    private ChatRoom chatRoom;

    public static UserChatRoom from(ChatRoomDto dto, ChatRoom chatRoom) {
        return UserChatRoom.builder()
//                .consultId(dto.getApt().getUser().getId())
                .isInRoom(Boolean.TRUE)
                // TODO : TIMEERROR
                .entryTime(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
                .user(dto.getUser())
                .chatRoom(chatRoom)
                .build();
    }
    public static UserChatRoom otherFrom(ChatRoomDto dto, ChatRoom chatRoom) {
        return UserChatRoom.builder()
//                .consultId(dto.getApt().getUser().getId())
                .isInRoom(Boolean.TRUE)
                // TODO : TIMEERROR
                .entryTime(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
                .user(dto.getApt().getUser())
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
            // TODO : TIMEERROR
            this.entryTime = LocalDateTime.now(ZoneId.of("Asia/Seoul")); // 채팅방 나가는 기점을 시작으로 시간 업데이트
        }
    }

    public void reEntryInChatRoom() {
        if(!this.isInRoom){
            this.isInRoom = Boolean.TRUE;
        }
    }

    public static UserChatRoom addUsser(User user, ChatRoom chatRoom) {
        return UserChatRoom.builder()
                .isInRoom(Boolean.TRUE)
                .entryTime(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
                .user(user)
                .chatRoom(chatRoom)
                .build();
    }
}

