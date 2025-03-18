package com.ddhouse.chat.dto;

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
//    private Long counselId;
//    private Long consultId;
    private LocalDateTime regDate;
    private String lastMsg;
    private Apt apt;
    private User user;
    private ChatRoom chatRoom;

    public static ChatRoomDto from(ChatRoom chatRoom) {
        return ChatRoomDto.builder()
                .id(chatRoom.getId())
                .name(chatRoom.getApt().getName()) // 매물 이름
                .memberNum(chatRoom.getMemberNum())
//                .counselId(myId) // 매물 문의자
//                .consultId(chatRoom.getApt().getUser().getId()) // 매물 소유자
                .regDate(chatRoom.getRegDate())
                .build();
    }

    public static ChatRoomDto createChatRoomDto(Apt apt, User user) {
        return ChatRoomDto.builder()
                .name(apt.getName())
                .memberNum(2)
                .apt(apt)
                .user(user)
                .regDate(LocalDateTime.now())
                .lastMsg("")
                .build();
    }

}
