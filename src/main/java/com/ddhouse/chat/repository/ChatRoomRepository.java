package com.ddhouse.chat.repository;

import com.ddhouse.chat.domain.ChatRoom;
import com.ddhouse.chat.domain.UserChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    Optional<List<ChatRoom>> findByPhoneNumber(String phoneNumber);
    @Query("""
        SELECT cr
        FROM ChatRoom cr
        JOIN UserChatRoom ucr1 ON ucr1.chatRoom = cr
        JOIN UserChatRoom ucr2 ON ucr2.chatRoom = cr
        WHERE ucr1.user.id = :myId
          AND ucr2.user.id = :opponentId
          AND cr.isGroup = false
    """)
    Optional<ChatRoom> findPrivateChatRoomBetweenUsers(@Param("myId") Long myId,
                                                       @Param("opponentId") Long opponentId);
}
