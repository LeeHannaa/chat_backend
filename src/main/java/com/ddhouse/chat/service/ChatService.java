package com.ddhouse.chat.service;


import com.ddhouse.chat.domain.ChatRoom;
import com.ddhouse.chat.dto.ChatRoomDto;
import com.ddhouse.chat.repository.ChatMessageRepository;
import com.ddhouse.chat.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class ChatService {
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;

    public ChatRoom createChatRoom(ChatRoomDto chatRoomDto) {
        return chatRoomRepository.save(ChatRoom.from(chatRoomDto));
    }

    // 채팅 전체 리스트
    public List<ChatRoomDto> findChatRoomList() {
        List<ChatRoom> chatRooms = chatRoomRepository.findAll();
        return chatRooms.stream().map(ChatRoomDto::from).collect(Collectors.toList());
    }

    public List<ChatRoomDto> findMyChatRoomList(Long myId) {
        List<ChatRoom> chatRooms = chatRoomRepository.findByUserId(myId);
        return chatRooms.stream().map(ChatRoomDto::from).collect(Collectors.toList());
    }


    public Mono<String> getLastMessage(Long roomId) {
        return chatMessageRepository.findAllByRoomId(roomId)
                .sort((m1, m2) -> m2.getCreatedDate().compareTo(m1.getCreatedDate()))  // 날짜 내림차순 정렬
                .next()  // 가장 첫 번째 (최신) 메시지를 가져옴
                .map(chatMessage -> chatMessage.getMsg());  // 메시지 내용만 반환
    }

    public void deleteChatRoom(Long roomId){
        // 해당 채팅방 데이터들을 먼저 다 지우기
        chatMessageRepository.deleteByRoomId(roomId);
        // 해당 채팅방 지우기
        chatRoomRepository.deleteById(roomId);
    }
}
