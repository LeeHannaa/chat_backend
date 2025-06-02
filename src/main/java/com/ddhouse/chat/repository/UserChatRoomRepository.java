package com.ddhouse.chat.repository;

import com.ddhouse.chat.exception.NotFoundException;
import com.ddhouse.chat.vo.ChatRoom;
import com.ddhouse.chat.vo.User;
import com.ddhouse.chat.vo.UserChatRoom;
import lombok.RequiredArgsConstructor;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
@Repository
@RequiredArgsConstructor
public class UserChatRoomRepository {
    private final SqlSessionTemplate sql;

    public List<UserChatRoom> findByUserId(Long userId){
        return sql.selectList("userchatroomMapper.findByUserId", userId);
    }

    public UserChatRoom findByUserIdAndChatRoomId(Long userId, Long chatRoomId) {
        UserChatRoom userChatRoom = sql.selectOne("userchatroomMapper.findByUserIdAndChatRoomId", Map.of(
                "userId", userId,
                "chatRoomId", chatRoomId
        ));
        if (userChatRoom == null) {
            throw new NotFoundException("해당 유저 정보와 채팅방 정보를 가진 UserChatRoom 정보를 찾을 수 없습니다: ");
        }
        return userChatRoom;
    }

    public Optional<UserChatRoom> findOpponent(Long myId, Long chatRoomId) {
        Map<String, Object> params = Map.of(
                "myId", myId,
                "chatRoomId", chatRoomId
        );
        return Optional.ofNullable(sql.selectOne("userchatroomMapper.findOpponent", params));
    }

    public void deleteByChatRoomId(Long roomId) {
        sql.delete("userchatroomMapper.deleteByChatRoomId", roomId);
    }
    public void deleteById(Long id) {
        sql.delete("userchatroomMapper.deleteById", id);
    }

    public List<UserChatRoom> findAllByChatRoomId(Long roomId) {
        return sql.selectList("userchatroomMapper.findAllByChatRoomId", roomId);
    }

    public boolean existsByUserAndChatRoom(User user, ChatRoom chatRoom){
        Integer result = sql.selectOne("userchatroomMapper.existsByUserAndChatRoom", Map.of(
                "userId", user.getId(),
                "chatRoomId", chatRoom.getId()
        ));
        return result != null && result > 0;
    }

    public UserChatRoom save(UserChatRoom userChatRoom) {
        sql.insert("userchatroomMapper.save", userChatRoom);
        return userChatRoom;
    }

    public void saveAll(List<UserChatRoom> userChatRooms) {
        sql.insert("userchatroomMapper.saveAll", userChatRooms);
    }
}
