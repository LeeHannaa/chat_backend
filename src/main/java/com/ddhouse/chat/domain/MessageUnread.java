package com.ddhouse.chat.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import lombok.*;

@RedisHash("MessageUnread")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MessageUnread {

    @Id
    private String id;

    private String msgId;
    private String userId;
    private boolean isRead;
}
