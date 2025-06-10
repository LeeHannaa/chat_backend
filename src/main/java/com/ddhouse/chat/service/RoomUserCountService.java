package com.ddhouse.chat.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class RoomUserCountService {
    private final StringRedisTemplate redisTemplate;

    @Async
    public void addUserInChatRoom(String roomId, String userId) {
        String key = "chat:room:usercount:" + roomId;
        redisTemplate.opsForSet().add(key, userId);
        System.out.println("redis에 유저 카톡방 들어온 정보 저장!!");
    }

    @Async
    public void outUserInChatRoom(String roomId, String userId) {
        String key = "chat:room:usercount:" + roomId;
        Set<String> members = redisTemplate.opsForSet().members(key);
        System.out.println("🧐 현재 Redis에 남아있는 유저 목록: " + members);
        System.out.println("👤 제거하려는 유저 ID: " + userId);
        redisTemplate.opsForSet().remove(key, userId);

        Long size = redisTemplate.opsForSet().size(key);
        System.out.println("🚪 유저 나감 처리: roomId=" + roomId + ", 현재 인원 수=" + size);

        // 0 이하일 경우 삭제
        if (size != null && size <= 0) {
            redisTemplate.delete(key);
        }
    }

    public int getUserCount(Long roomId) {
        String key = "chat:room:usercount:" + roomId.toString();
        int size = redisTemplate.opsForSet().size(key).intValue();
        return size;
    }

    public List<Long> getUserIdsInChatRoom(Long roomId, Long myId){
        String key = "chat:room:usercount:" + roomId.toString();
        Set<String> userIdStrings = redisTemplate.opsForSet().members(key);
        if (userIdStrings == null) {
            return Collections.emptyList();
        }

        return userIdStrings.stream()
                .map(Long::valueOf)
                .filter(userId -> !userId.equals(myId))
                .collect(Collectors.toList());
    }
}
