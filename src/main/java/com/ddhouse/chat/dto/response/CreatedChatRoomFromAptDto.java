package com.ddhouse.chat.dto.response;

import com.ddhouse.chat.domain.Apt;
import com.ddhouse.chat.dto.AptDto;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
public class CreatedChatRoomFromAptDto {
    private Long roomId;
    private String name;

    public static CreatedChatRoomFromAptDto from(Long roomId, String name) {
        return CreatedChatRoomFromAptDto.builder()
                .roomId(roomId)
                .name(name)
                .build();
    }
}
