package com.ddhouse.chat.domain;

import jakarta.persistence.Table;
import lombok.*;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;

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
    private Date createdDate;

    public ChatMessage(Long roomId, String msg, Long writerId, Date createdDate) {
        this.id = UUID.randomUUID();
        this.roomId = roomId;
        this.msg = msg;
        this.writerId = writerId;
        this.createdDate = createdDate;
    }
}
