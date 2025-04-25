package com.ddhouse.chat.domain;

import com.ddhouse.chat.dto.request.ChatMessageRequestDto;
import jakarta.persistence.Table;
import lombok.*;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;

import java.time.*;
import java.util.Date;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(schema = "chatMessage")
public class ChatMessage {
    @PrimaryKey
    private UUID id;
    private String msg;

    public static ChatMessage from (String msg) {
        UUID uuid = UUID.randomUUID();
        return ChatMessage.builder()
                .id(uuid)
                .msg(msg)
                .build();
    }
}
