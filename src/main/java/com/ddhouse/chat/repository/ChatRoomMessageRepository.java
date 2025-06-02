package com.ddhouse.chat.repository;

import com.ddhouse.chat.exception.NotFoundException;
import com.ddhouse.chat.vo.ChatRoom;
import com.ddhouse.chat.vo.ChatRoomMessage;
import com.ddhouse.chat.vo.User;
import com.ddhouse.chat.vo.UserChatRoom;
import lombok.RequiredArgsConstructor;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;

@Repository
@RequiredArgsConstructor
public class ChatRoomMessageRepository{
    private final SqlSessionTemplate sql;

    public List<ChatRoomMessage> findRecentMessages(Long roomId, LocalDateTime regDate){
        Map<String, Object> params = new HashMap<>();
        params.put("roomId", roomId);
        params.put("regDate", regDate);
        return sql.selectList("chatroommessageMapper.findRecentMessages", params);
    }

    public ChatRoomMessage findByMessageId(UUID messageId) {
        ChatRoomMessage chatRoomMessage = sql.selectOne("chatroommessageMapper.findByMessageId", messageId);
        if (chatRoomMessage == null) {
            throw new NotFoundException("해당 유저 정보와 채팅방 정보를 가진 ChatRoomMessage 정보를 찾을 수 없습니다: ");
        }
        return chatRoomMessage;
    }

    public ChatRoomMessage save(ChatRoomMessage chatRoomMessage) {
        sql.insert("chatroommessageMapper.save", chatRoomMessage);
        return chatRoomMessage;
    }

    public List<ChatRoomMessage> findAllByChatRoomId(Long roomId) {
        return sql.selectList("chatroommessageMapper.findAllByChatRoomId", roomId);
    }

    public boolean existsByChatRoomId(Long roomId){
        Integer result = sql.selectOne("chatroommessageMapper.existsByChatRoomId", roomId);
        return result != null && result > 0;
    }

    public void deleteByChatRoomId(Long roomId) {
        sql.delete("chatroommessageMapper.deleteByChatRoomId", roomId);
    }
}
