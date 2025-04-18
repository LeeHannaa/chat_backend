package com.ddhouse.chat.dto.response.ChatMessage;

import com.ddhouse.chat.domain.ChatMessage;
import com.ddhouse.chat.domain.UserChatRoom;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;


@Getter
@Setter
@SuperBuilder
public class ChatMessageResponseDto {
    private Long roomId;
}
