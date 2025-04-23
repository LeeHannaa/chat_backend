package com.ddhouse.chat.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
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

    private String getRoomUserCountKey(String roomId) {
        return "chat:room:usercount:" + roomId;
    }

    public void addUserInChatRoom(String roomId, String userId) {
        String key = getRoomUserCountKey(roomId);
        redisTemplate.opsForSet().add(key, userId);
    }

    public void outUserInChatRoom(String roomId, String userId) {
        String key = getRoomUserCountKey(roomId);
        redisTemplate.opsForSet().remove(key, userId);

        Long size = redisTemplate.opsForSet().size(key);
        // 0 이하일 경우 삭제
        if (size != null && size <= 0) {
            redisTemplate.delete(key);
        }
    }

    public int getUserCount(Long roomId) {
        // TODO G **: 현재 접속한 사람의 수
        String key = getRoomUserCountKey(roomId.toString());
        int size = redisTemplate.opsForSet().size(key).intValue();
        return size;
    }

    public List<Long> getUserIdsInChatRoom(Long roomId, Long myId){
        // TODO G **: 현재 접속한 사람의 ids (나빼고)
        String key = getRoomUserCountKey(roomId.toString());
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
