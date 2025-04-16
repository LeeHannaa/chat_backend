package com.ddhouse.chat.dto.info;

import com.ddhouse.chat.domain.Apt;
import com.ddhouse.chat.domain.ChatRoom;
import com.ddhouse.chat.domain.User;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class ChatRoomDto {
    private Long id;
    private String name;
    private int memberNum;
    private LocalDateTime regDate; // 채팅방 생성일
    private LocalDateTime updateLastMsgTime;
    private String lastMsg;
    private Apt apt;
    private User user;
    private ChatRoom chatRoom;
    private Long unreadCount;

    public static ChatRoomDto createChatRoomDto(Apt apt, User user) {
        return ChatRoomDto.builder()
                .name(apt.getName())
                .memberNum(2)
                .apt(apt)
                .user(user)
                .regDate(LocalDateTime.now())
                .updateLastMsgTime(LocalDateTime.now()) // 처음 생성 할 때
                .lastMsg("")
                .build();
    }

}
