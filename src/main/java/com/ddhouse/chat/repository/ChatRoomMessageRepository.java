package com.ddhouse.chat.repository;

import com.ddhouse.chat.domain.ChatMessage;
import com.ddhouse.chat.domain.ChatRoomMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChatRoomMessageRepository extends JpaRepository<ChatRoomMessage, Long> {
    ChatRoomMessage findTopByChatRoomIdOrderByRegDateDesc(Long roomId);
    Optional<ChatRoomMessage> findByMessageId(UUID messageId);
    List<ChatRoomMessage> findAllByChatRoomId(Long roomId);
    boolean existsByChatRoomId(Long chatRoomId);

}
