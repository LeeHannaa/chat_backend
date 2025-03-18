package com.ddhouse.chat.repository;

import com.ddhouse.chat.domain.UserChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserChatRoomRepository extends JpaRepository<UserChatRoom, Long> {

    @Query("SELECT DISTINCT u FROM UserChatRoom u WHERE u.user.id = :id OR u.consultId = :id")
    List<UserChatRoom> findByUserIdOrConsultId(@Param("id") Long id);
    Optional<UserChatRoom> findByUserIdAndConsultId(Long userId, Long consultId);
}
