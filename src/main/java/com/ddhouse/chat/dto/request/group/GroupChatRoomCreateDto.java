package com.ddhouse.chat.dto.request.group;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
public class GroupChatRoomCreateDto {
    // 단체 채팅방을 생성할 때
    List<Long> userIds;
    String chatRoomName;
    LocalDateTime regDate;
}
