package com.ddhouse.chat.repository;

import com.ddhouse.chat.domain.ChatRoom;
import com.ddhouse.chat.domain.User;
import com.ddhouse.chat.domain.UserChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface UserChatRoomRepository extends JpaRepository<UserChatRoom, Long> {
    List<UserChatRoom> findByUserId(Long userId);
    Optional<UserChatRoom> findByUserIdAndChatRoomId(Long userId, Long chatRoomId);

    @Query("SELECT u FROM UserChatRoom u WHERE u.chatRoom.id = :chatRoomId AND u.user.id <> :myId")
    Optional<UserChatRoom> findOpponent(@Param("myId") Long myId, @Param("chatRoomId") Long chatRoomId);
    void deleteByChatRoomId(Long roomId);
    Optional<UserChatRoom> findByChatRoomId(Long roomId);
    List<UserChatRoom> findAllByChatRoomId(Long chatRoomId);
    boolean existsByUserAndChatRoom(User user, ChatRoom chatRoom);

}
