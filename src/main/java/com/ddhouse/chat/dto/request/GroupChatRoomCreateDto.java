package com.ddhouse.chat.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class GroupChatRoomCreateDto {
    List<Long> userIds;
    String chatRoomName;
}
