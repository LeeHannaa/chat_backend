package com.ddhouse.chat.repository;

import com.ddhouse.chat.exception.NotFoundException;
import com.ddhouse.chat.vo.ChatRoom;
import lombok.RequiredArgsConstructor;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;

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

    public ChatRoom memberNumUpdate(ChatRoom chatRoom) {
        sql.update("chatroomMapper.memberNumUpdate", chatRoom);
        return chatRoom;
    }


    public ChatRoom findByIdx(Long idx) {
        ChatRoom chatRoom = sql.selectOne("chatroomMapper.findByIdx", idx);
        if (chatRoom == null) {
            throw new NotFoundException("해당 ID의 채팅방을 찾을 수 없습니다: " + idx);
        }
        return chatRoom;
    }

    public void deleteByIdx(Long idx) {
        sql.delete("chatroomMapper.deleteByIdx", idx);
    }
}
