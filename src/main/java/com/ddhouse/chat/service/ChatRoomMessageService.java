package com.ddhouse.chat.service;

import com.ddhouse.chat.domain.ChatMessage;
import com.ddhouse.chat.domain.ChatRoom;
import com.ddhouse.chat.domain.ChatRoomMessage;
import com.ddhouse.chat.domain.User;
import com.ddhouse.chat.dto.request.ChatMessageRequestDto;
import com.ddhouse.chat.dto.response.ChatMessageResponseToChatRoomDto;
import com.ddhouse.chat.exception.NotFoundException;
import com.ddhouse.chat.repository.ChatRoomMessageRepository;
import com.ddhouse.chat.repository.ChatRoomRepository;
import com.ddhouse.chat.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class ChatRoomMessageService {
    private final ChatRoomMessageRepository chatRoomMessageRepository;
    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;

    public void saveChatRoomMessage(ChatMessageResponseToChatRoomDto chatMessageResponseToChatRoomDto){
    }

    public ChatRoomMessage findByMessageId(UUID msgId){
        return chatRoomMessageRepository.findByMessageId(msgId)
                .orElseThrow(() -> new NotFoundException("messageId에 해당하는 ChatRoomMessage가 없습니다."));
    }

    public Mono<ChatRoomMessage> saveChatRoomMessage(ChatMessageRequestDto chatMessageRequestDto, UUID msgId){
        User user = userRepository.findById(chatMessageRequestDto.getWriterId())
                .orElseThrow(() -> new NotFoundException("해당 사용자를 찾을 수 없습니다."));
        ChatRoom chatRoom = chatRoomRepository.findById(chatMessageRequestDto.getRoomId())
                        .orElseThrow(() -> new NotFoundException("해당 채팅방을 찾을 수 없습니다."));
        ChatRoomMessage chatRoomMessage = ChatRoomMessage.save(msgId, user, chatRoom);

        return Mono.just(chatRoomMessageRepository.save(chatRoomMessage));
    }
}
