package com.ddhouse.chat.dto.response.message;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;


@Getter
@Setter
@SuperBuilder
public class ChatMessageResponseDto {
    private Long roomId;
}
