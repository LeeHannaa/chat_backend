package com.ddhouse.chat.repository;

import com.ddhouse.chat.exception.NotFoundException;
import com.ddhouse.chat.vo.ChatRoom;
import com.ddhouse.chat.vo.User;
import lombok.RequiredArgsConstructor;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ChatRoomRepository {
    private final SqlSessionTemplate sql;

    public List<ChatRoom> findByPhoneNumber(String phoneNumber){
        return sql.selectList("chatroomMapper.findByPhoneNumber", phoneNumber);
    }

    public ChatRoom save(ChatRoom chatRoom) {
        sql.insert("chatroomMapper.save", chatRoom);
        return chatRoom;
    }

    public ChatRoom findById(Long id) {
        ChatRoom chatRoom = sql.selectOne("chatroomMapper.findById", id);
        if (chatRoom == null) {
            throw new NotFoundException("해당 ID의 채팅방을 찾을 수 없습니다: " + id);
        }
        return chatRoom;
    }

    public void deleteById(Long id) {
        sql.delete("chatroomMapper.deleteById", id);
    }
}
