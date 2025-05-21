package com.ddhouse.chat.dto.request.group;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserChatRoomAddDto {
    // 기존 채팅방에 유저 추가할 경우
    String userIds;
    Long chatRoomId;
}
