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
    private Long roomId;
    private String msg;
    private Long writerId;
    private LocalDateTime createdDate;
    private String writerName;

    public ChatMessage(ChatMessageRequestDto chatMessageRequestDto) {
        this.id = UUID.randomUUID();
        this.roomId = chatMessageRequestDto.getRoomId();
        this.msg = chatMessageRequestDto.getMsg();
        this.writerId = chatMessageRequestDto.getWriterId();
        this.writerName = chatMessageRequestDto.getWriterName();
        this.createdDate = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
    }


}
