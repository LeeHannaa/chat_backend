package com.ddhouse.chat.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserChatRoomAddDto {
    String userIds;
    Long chatRoomId;
}
