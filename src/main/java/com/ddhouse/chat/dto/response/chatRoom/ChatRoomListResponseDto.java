package com.ddhouse.chat.dto.response.chatRoom;

import com.ddhouse.chat.dto.request.message.ChatMessageRequestDto;
import com.ddhouse.chat.dto.request.message.GuestMessageRequestDto;
import com.ddhouse.chat.vo.ChatRoom;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Getter
@Setter
@Builder
public class ChatRoomListResponseDto {
    // 채팅방 목록에서 필요한 정보
    private Long roomId;
    private String name;
    private String lastMsg;
    private int memberNum;
    private LocalDateTime updateLastMsgTime;
    private Long unreadCount;

    public static ChatRoomListResponseDto one(ChatRoom chatRoom, String chatName) {
        return ChatRoomListResponseDto.builder()
                .roomId(chatRoom.getIdx())
                .name(chatName)
                .memberNum(chatRoom.getMemberNum())
                .build();
    }

    public static ChatRoomListResponseDto group(ChatRoom chatRoom) {
        return ChatRoomListResponseDto.builder()
                .roomId(chatRoom.getIdx())
                .name(chatRoom.getName())
                .memberNum(chatRoom.getMemberNum())
                .build();
    }
    public static ChatRoomListResponseDto from (ChatMessageRequestDto chatMessageRequestDto, String userName, Long count, int memberNum) {
        return ChatRoomListResponseDto.builder()
                .roomId(chatMessageRequestDto.getRoomId())
                .name(userName != null ? userName : chatMessageRequestDto.getChatName())
                .lastMsg(chatMessageRequestDto.getMsg())
                .memberNum(memberNum)
                .updateLastMsgTime(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
                .unreadCount(count)
                .build();
    }

    public static ChatRoomListResponseDto delete (Long roomId) {
        return ChatRoomListResponseDto.builder()
                .roomId(roomId)
                .lastMsg("삭제된 메시지입니다.")
                .updateLastMsgTime(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
                .build();
    }

    public static ChatRoomListResponseDto guest (GuestMessageRequestDto guestMessageRequestDto, Long count, ChatRoom chatRoom) {
        return ChatRoomListResponseDto.builder()
                .roomId(chatRoom.getIdx())
                .name(guestMessageRequestDto.getPhoneNumber())
                .lastMsg(guestMessageRequestDto.getNoteText())
                .memberNum(chatRoom.getMemberNum())
                .updateLastMsgTime(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
                .unreadCount(count)
                .build();
    }

}
