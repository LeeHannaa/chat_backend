package com.ddhouse.chat.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;


@Slf4j
@RequiredArgsConstructor
@Service
public class MessageUnreadService {
    private final StringRedisTemplate redisTemplate;
    private static final String PREFIX = "unread";

    public void addUnreadChat(String roomId, String receiverId, String msgId) {
        String key = PREFIX + ":" + roomId + ":" + msgId;
        redisTemplate.opsForSet().add(key, receiverId);
        System.out.println("redis에 안읽은 채팅 내역들 저장 완료!!");
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

    public void removeUnread(String roomId, String myId) {
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
