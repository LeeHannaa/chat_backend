package com.ddhouse.chat.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class NoteUnreadService {
    private final StringRedisTemplate redisTemplate;
    private static final String PREFIX = "note";

    public void addUnreadNote(String noteId, String receiverId) {
        // 매물 문의를 받는 사람이 안읽은 쪽지 저장
        String key = PREFIX + ":unread:" + receiverId;
        redisTemplate.opsForSet().add(key, noteId);
        System.out.println("redis에 안읽은 쪽지 내역 저장 완료!!");
    }

    public boolean getUnreadCount(String userId) {
        // 유저가 안읽은 쪽지 있는지 없는지 리턴 -> 홈에서 안읽은 쪽지가 있다면 알림 아이콘 표시
        String key = PREFIX + ":unread:" + userId;
        if(Boolean.TRUE.equals(redisTemplate.opsForSet().size(key) == 0)) return false;
        else return true;
    }

    public boolean getUnreadCountByNote(String noteId, String userId) {
        // 쪽지 당 읽었는지 안읽었는지 확인
        String key = PREFIX + ":unread:" + userId;
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(key, noteId));
    }

    public void removeUnreadNote(String noteId, String myId) {
        // 해당 쪽지를 확인한 경우 - 안읽은 쪽지 리스트에서 삭제
        String key = PREFIX + ":unread:" + myId;
        if(redisTemplate.opsForSet().remove(key, noteId) > 0){
            Long size = redisTemplate.opsForSet().size(key);
            if (size != null && size == 0) {
                redisTemplate.delete(key);
            }
        }
    }
}
