package com.ddhouse.chat.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class MessageUnreadService {
    private final StringRedisTemplate redisTemplate;
    private static final String PREFIX = "unread";

    public void addUnreadChat(String roomId, String receiverId, UUID msgId) {
        // TODO G **: 안읽은 메시지 저장할때는 msgId를 저장하고 거기 value로 receiverId(List)를 저장
        String key = PREFIX + ":" + roomId + ":" + msgId;
        redisTemplate.opsForSet().add(key, receiverId);
        // 사용자 목록도 저장
        redisTemplate.opsForSet().add("unreadUsers:" + roomId, receiverId);
        System.out.println("redis에 안읽은 채팅 내역들 저장 완료!!");
    }

    // 안 읽은 메시지 목록 가져오기
    public Set<Object> getUnreadMessages(String roomId, String userId) {
        String key = PREFIX + ":" + roomId + ":" + userId;
        return Collections.singleton(redisTemplate.opsForSet().members(key));
    }

    // 해당 방에 상대방이 안읽은 메시지 수
    public Long getOtherUserUnreadCount(String roomId) {
        String pattern = PREFIX + ":" + roomId + ":*";
        ScanOptions options = ScanOptions.scanOptions().match(pattern).count(1000).build();

        Long totalCount = 0L;

        RedisConnection connection = redisTemplate.getConnectionFactory().getConnection();
        try (Cursor<byte[]> cursor = connection.scan(options)) {
            while (cursor.hasNext()) {
                String key = new String(cursor.next());
                Long size = redisTemplate.opsForSet().size(key);
                totalCount += (size != null) ? size : 0L;
            }
        } catch (Exception e) {
            e.printStackTrace(); // 필요시 로깅 처리
        }

        return totalCount;
    }

    // 각 메시지당 안읽은 사람 수
    public int getUnreadCountByMsgId(String roomId, String msgId) {
        String key = PREFIX + ":" + roomId + ":" + msgId;
        Long totalCount = redisTemplate.opsForSet().size(key);
        return totalCount != null ? totalCount.intValue() : 0;
    }

    // 내가 상대방의 메시지를 안읽은 개수
    public Long getUnreadMessageCount(String roomId, String userId) {
        String pattern = PREFIX + ":" + roomId + ":*";
        ScanOptions options = ScanOptions.scanOptions().match(pattern).count(100).build();
        Cursor<byte[]> cursor = redisTemplate.getConnectionFactory()
                .getConnection()
                .scan(options);

        long count = 0L;

        while (cursor.hasNext()) {
            String key = new String(cursor.next());
            if (redisTemplate.opsForSet().isMember(key, userId)) {
                count++;
            }
        }
        return count;
    }

    // 해당 방에 안 읽은 메시지가 있는지 없는지
    public boolean getUnreadCount(String roomId) {
        String key = "unreadUsers:" + roomId;
        if(Boolean.TRUE.equals(redisTemplate.opsForSet().size(key) == 0)) return false;
        else return true;
    }

    public void removeUnread(String roomId, String myId) {
        redisTemplate.opsForSet().remove("unreadUsers:" + roomId, myId);
        System.out.println("채팅방에 unread 삭제 완료! myId = " + myId);

        ScanOptions options = ScanOptions.scanOptions()
                .match(PREFIX + ":" + roomId + ":*") // 해당 채팅방에 있는 메시지들
                .count(500) // 한 번에 얼마나 스캔할지 (튜닝 가능)
                .build();

        try (Cursor<byte[]> cursor = redisTemplate.getConnectionFactory()
                .getConnection()
                .scan(options)) {

            while (cursor.hasNext()) {
                String key = new String(cursor.next());

                // myId 제거 -> 지웠으면 1, 아니면 0 (없었던 경우)
                Long removed = redisTemplate.opsForSet().remove(key, myId);

                if (removed != null && removed > 0) {
                    // Set이 비었으면 key 삭제
                    Long size = redisTemplate.opsForSet().size(key);
                    if (size != null && size == 0) {
                        redisTemplate.delete(key);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
