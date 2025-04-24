package com.ddhouse.chat.repository;

import com.ddhouse.chat.domain.ChatRoomMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChatRoomMessageRepository extends JpaRepository<ChatRoomMessage, Long> {
//    ChatRoomMessage findTopByChatRoomIdOrderByRegDateDesc(Long roomId);
    List<ChatRoomMessage> findTop100ByChatRoomIdAndRegDateAfterOrderByRegDateDesc(Long roomId, LocalDateTime regDate);

    Optional<ChatRoomMessage> findByMessageId(UUID messageId);
    List<ChatRoomMessage> findAllByChatRoomId(Long roomId);
    boolean existsByChatRoomId(Long chatRoomId);
    void deleteByChatRoomId(Long roomId);

}
