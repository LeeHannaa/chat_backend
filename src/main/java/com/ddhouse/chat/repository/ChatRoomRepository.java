package com.ddhouse.chat.repository;

import com.ddhouse.chat.domain.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
//    Optional<ChatRoom> findByAptIdAndUserId(Long aptId, Long userId);
//    List<ChatRoom> findByUserId(Long userId);

}
