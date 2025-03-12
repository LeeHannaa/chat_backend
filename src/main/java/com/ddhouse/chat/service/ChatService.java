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

    public void createChatRoom(ChatRoomDto chatRoomDto) {
        chatRoomRepository.save(ChatRoom.from(chatRoomDto));
    }

    // 채팅 전체 리스트
    public List<ChatRoomDto> findChatRoomList() {
        List<ChatRoom> chatRooms = chatRoomRepository.findAll();
        return chatRooms.stream().map(ChatRoomDto::from).collect(Collectors.toList());
    }

    // TODO : apt에 등록했거나 문의한 id가 나의 id와 동일한 아이디를 다 전달 (아파트 등록자인 경우 아직 안함)
    public List<ChatRoomDto> findMyChatRoomList() {
        // TODO : 프론트에서 나의 id 받아와서 확인하기
        Long myId = 1L;
        List<ChatRoom> chatRooms = chatRoomRepository.findByUserId(myId);
        return chatRooms.stream().map(ChatRoomDto::from).collect(Collectors.toList());
    }


    public Mono<String> getLastMessage(Long roomId) {
        return chatMessageRepository.findAllByRoomId(roomId)
                .sort((m1, m2) -> m2.getCreatedDate().compareTo(m1.getCreatedDate()))  // 날짜 내림차순 정렬
                .next()  // 가장 첫 번째 (최신) 메시지를 가져옴
                .map(chatMessage -> chatMessage.getMsg());  // 메시지 내용만 반환
    }


}
