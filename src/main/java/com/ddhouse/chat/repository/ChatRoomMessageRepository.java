package com.ddhouse.chat.repository;

import com.ddhouse.chat.exception.NotFoundException;
import com.ddhouse.chat.vo.ChatRoomMessage;
import lombok.RequiredArgsConstructor;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;

@Repository
@RequiredArgsConstructor
public class ChatRoomMessageRepository{
    private final SqlSessionTemplate sql;

    public List<ChatRoomMessage> findRecentMessages(Long roomId, LocalDateTime entryTime){
        Map<String, Object> params = new HashMap<>();
        params.put("roomId", roomId);
        params.put("entryTime", entryTime);
        return sql.selectList("chatroommessageMapper.findRecentMessages", params);
    }

    public ChatRoomMessage findById(Long chatRoomMessageId) {
        ChatRoomMessage chatRoomMessage = sql.selectOne("chatroommessageMapper.findById", chatRoomMessageId);
        if (chatRoomMessage == null) {
            throw new NotFoundException("해당 유저 정보와 채팅방 정보를 가진 ChatRoomMessage 정보를 찾을 수 없습니다: ");
        }
        return chatRoomMessage;
    }

    public ChatRoomMessage save(ChatRoomMessage chatRoomMessage) {
        sql.insert("chatroommessageMapper.save", chatRoomMessage);
        return chatRoomMessage;
    }

    public ChatRoomMessage update(ChatRoomMessage chatRoomMessage) {
        sql.update("chatroommessageMapper.update", chatRoomMessage);
        return chatRoomMessage;
    }

    public List<ChatRoomMessage> findAllByChatRoomId(Long roomId) {
        return sql.selectList("chatroommessageMapper.findAllByChatRoomId", roomId);
    }

    public boolean existsByChatRoomId(Long roomId){
        int result = sql.selectOne("chatroommessageMapper.existsByChatRoomId", roomId);
        return result > 0;
    }

    public void deleteByChatRoomId(Long roomId) {
        sql.delete("chatroommessageMapper.deleteByChatRoomId", roomId);
    }
}
