package com.ddhouse.chat.dto;

import com.ddhouse.chat.dto.request.message.ChatMessageRequestDto;
import com.ddhouse.chat.vo.ChatRoom;
import com.ddhouse.chat.vo.User;
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
//    private Apt apt;
    private User user;
    private ChatRoom chatRoom;
    private Long unreadCount;


    // 처음 채팅방을 생성할 때 (문의 -> 채팅방 생성)
    // TODO : 아파트랑 채팅방이랑 관련 없음
    // 나중에 ChatRoomCreateDto랑 합칠 수 있으면 합치기!!!!!
    public static ChatRoomDto createChatRoomDto(ChatMessageRequestDto chatMessageRequestDto, User user) {
        return ChatRoomDto.builder()
//                .name(apt.getName())
                .memberNum(2)
//                .apt(apt)
                .user(user) // UserChatRoom때 사용
                .regDate(chatMessageRequestDto.getRegDate())
                .updateLastMsgTime(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
                .lastMsg(chatMessageRequestDto.getMsg())
                .build();
    }

    public static ChatRoomDto person(User user) {
        return ChatRoomDto.builder()
                .memberNum(2)
                .user(user)
                .regDate(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
                .updateLastMsgTime(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
                .lastMsg("")
                .build();
    }

    public static ChatRoomDto from() {
        return ChatRoomDto.builder()
                .memberNum(2)
                .regDate(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
                .updateLastMsgTime(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
                .lastMsg("")
                .build();
    }

}
