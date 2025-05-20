package com.ddhouse.chat.dto.info;

import com.ddhouse.chat.domain.Apt;
import com.ddhouse.chat.domain.ChatRoom;
import com.ddhouse.chat.domain.User;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.ZoneId;

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


    // 처음 채팅방을 생성할 때 (문의 -> 채팅방 생성)
    // TODO : 아파트랑 채팅방이랑 관련 없음
    public static ChatRoomDto createChatRoomDto(Apt apt, User user) {
        return ChatRoomDto.builder()
                .name(apt.getName())
                .memberNum(2)
                .apt(apt)
                .user(user)
                .updateLastMsgTime(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
                .lastMsg("")
                .build();
    }

}
