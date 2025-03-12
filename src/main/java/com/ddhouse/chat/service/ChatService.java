package com.ddhouse.chat.service;

import com.ddhouse.chat.domain.ChatMessage;
import com.ddhouse.chat.domain.ChatRoom;
import com.ddhouse.chat.dto.ChatMessageDto;
import com.ddhouse.chat.dto.ChatRoomDto;
import com.ddhouse.chat.exception.UserNotFoundException;
import com.ddhouse.chat.repository.ChatMessageRepository;
import com.ddhouse.chat.repository.ChatRoomRepository;
import com.ddhouse.chat.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class ChatService {
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;

    public void createChatRoom(ChatRoomDto chatRoomDto) {
        chatRoomRepository.save(ChatRoom.from(chatRoomDto));
    }

    public List<ChatRoomDto> findChatRoomList() {
        List<ChatRoom> chatRooms = chatRoomRepository.findAll();
        return chatRooms.stream().map(ChatRoomDto::from).collect(Collectors.toList());
    }

    public Flux<List<ChatMessageDto>> findChatMessages(Long id) {
        Flux<ChatMessage> chatMessages = chatMessageRepository.findAllByRoomId(id);
        return chatMessages
                .flatMap(chatMessage -> {
                    ChatMessageDto dto = ChatMessageDto.from(chatMessage);

                    return Mono.justOrEmpty(userRepository.findById(dto.getWriterId()))
                            .flatMap(user -> {
                                dto.setWriterName(user.getName());
                                return Mono.just(dto);
                            })
                            .switchIfEmpty(Mono.error(new UserNotFoundException("해당 유저와의 채팅이 없습니다.")));
                })
                .collectList()
                .flatMapMany(chatMessagesList -> {
                    chatMessagesList.sort(Comparator.comparing(ChatMessageDto::getCreatedDate));
                    return Flux.fromIterable(chatMessagesList)
                            .buffer(50);
                });
    }

    public Mono<ChatMessage> saveChatMessage(ChatMessageDto chat) {
        return chatMessageRepository.save(
                new ChatMessage(chat.getRoomId(), chat.getMsg(), chat.getWriterId()));
    }

    public Mono<String> getLastMessage(Long roomId) {
        return chatMessageRepository.findAllByRoomId(roomId)
                .sort((m1, m2) -> m2.getCreatedDate().compareTo(m1.getCreatedDate()))  // 날짜 내림차순 정렬
                .next()  // 가장 첫 번째 (최신) 메시지를 가져옴
                .map(chatMessage -> chatMessage.getMsg());  // 메시지 내용만 반환
    }
}
