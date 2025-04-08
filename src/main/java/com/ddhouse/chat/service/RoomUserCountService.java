package com.ddhouse.chat.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class RoomUserCountService {
    private final StringRedisTemplate redisTemplate;

    private String getRoomUserCountKey(String roomId) {
        return "chat:room:usercount:" + roomId;
    }

    public void increaseUserCount(String roomId) {
        String key = getRoomUserCountKey(roomId);
        redisTemplate.opsForValue().increment(key);
    }

    public void decreaseUserCount(String roomId) {
        String key = getRoomUserCountKey(roomId);
        Long count = redisTemplate.opsForValue().decrement(key);

        // 0 이하일 경우 삭제
        if (count != null && count <= 0) {
            redisTemplate.delete(key);
        }
    }

    public int getUserCount(Long roomId) {
        String key = getRoomUserCountKey(roomId.toString());
        String count = redisTemplate.opsForValue().get(key);
        return count != null ? Integer.parseInt(count) : 0;
    }
}
