package com.ddhouse.chat.dto;

import com.ddhouse.chat.domain.ChatRoom;
import com.ddhouse.chat.service.ChatService;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.socket.WebSocketSession;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Builder
public class ChatRoomDto {
    private Long id;
    private String name;
    private int memberNum;
    // 마지막 메시지와 그 시간 넘겨주기
    private LocalDateTime regDate;
    private String lastMsg;

    public static ChatRoomDto from(ChatRoom chatRoom) {
        return ChatRoomDto.builder()
                .id(chatRoom.getId())
                .name(chatRoom.getName())
                .memberNum(chatRoom.getMemberNum())
                .regDate(chatRoom.getRegDate())
                .build();
    }

}
