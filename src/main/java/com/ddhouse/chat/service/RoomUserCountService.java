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

    public void increaseUserCount(String roomId, String userId) {
        // G : userId를 value 값으로 저장
        String key = getRoomUserCountKey(roomId);
        redisTemplate.opsForValue().append(roomId, userId);
    }

    public void decreaseUserCount(String roomId) {
        // G : userId를 value에서 제거
        String key = getRoomUserCountKey(roomId);
        Long count = redisTemplate.opsForValue().decrement(key);

        // 0 이하일 경우 삭제
        if (count != null && count <= 0) {
            redisTemplate.delete(key);
        }
    }

    public int getUserCount(Long roomId) {
        String key = getRoomUserCountKey(roomId.toString());
        // G : 접속자 수 value의 크기로 전달
        int count = redisTemplate.opsForValue().get(key).length();
        return count;
    }
}
