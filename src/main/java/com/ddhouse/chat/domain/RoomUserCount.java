package com.ddhouse.chat.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@RedisHash("RoomUserCount")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoomUserCount {

    @Id
    private Long roomId;

    private int userCount;
}