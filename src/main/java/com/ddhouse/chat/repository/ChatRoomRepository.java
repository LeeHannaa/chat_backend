package com.ddhouse.chat.repository;

import com.ddhouse.chat.domain.ChatRoom;
import com.ddhouse.chat.domain.UserChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    Optional<List<ChatRoom>> findByPhoneNumber(String phoneNumber);
}
