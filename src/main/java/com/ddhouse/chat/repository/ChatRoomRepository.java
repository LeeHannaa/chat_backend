package com.ddhouse.chat.repository;

import com.ddhouse.chat.domain.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
//    Optional<ChatRoom> findByAptIdAndUserId(Long aptId, Long userId);
//    List<ChatRoom> findByUserId(Long userId);

}
