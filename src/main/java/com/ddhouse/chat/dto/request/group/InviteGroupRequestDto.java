package com.ddhouse.chat.dto.request.group;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
public class InviteGroupRequestDto {
    // 단체 채팅에서 나간 유저를 재초대
    private Long userId;
    private Long roomId;
    private Long chatRoomMessageId; // 유저가 채팅방을 나갔다는 메시지id를 가져와서 isDelete를 ture로 변경
}
