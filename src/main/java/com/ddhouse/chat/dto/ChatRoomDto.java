package com.ddhouse.chat.dto;

import com.ddhouse.chat.domain.ChatRoom;
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
    private Long counselId;
    private Long consultId;
    private LocalDateTime regDate;
    private String lastMsg;

    public static ChatRoomDto from(ChatRoom chatRoom) {
        return ChatRoomDto.builder()
                .id(chatRoom.getId())
                .name(chatRoom.getApt().getName()) // 매물 이름
                .memberNum(chatRoom.getMemberNum())
                .counselId(chatRoom.getUser().getId()) // 매물 문의자
                .consultId(chatRoom.getApt().getUser().getId()) // 매물 소유자
                .regDate(chatRoom.getRegDate())
                .build();
    }

}
