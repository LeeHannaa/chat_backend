package com.ddhouse.chat.service;

import com.ddhouse.chat.domain.*;
import com.ddhouse.chat.dto.request.ChatMessageRequestDto;
import com.ddhouse.chat.exception.NotFoundException;
import com.ddhouse.chat.repository.ChatRoomMessageRepository;
import com.ddhouse.chat.repository.ChatRoomRepository;
import com.ddhouse.chat.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class ChatRoomMessageService {
    private final ChatRoomMessageRepository chatRoomMessageRepository;
    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;


    public ChatRoomMessage findByMessageId(UUID msgId){
        return chatRoomMessageRepository.findByMessageId(msgId)
                .orElseThrow(() -> new NotFoundException("messageId에 해당하는 ChatRoomMessage가 없습니다."));
    }

    public Mono<ChatRoomMessage> saveChatRoomMessage(ChatMessageRequestDto chatMessageRequestDto, UUID msgId){
        User user = userRepository.findById(chatMessageRequestDto.getWriterId())
                .orElseThrow(() -> new NotFoundException("해당 사용자를 찾을 수 없습니다."));
        ChatRoom chatRoom = chatRoomRepository.findById(chatMessageRequestDto.getRoomId())
                        .orElseThrow(() -> new NotFoundException("해당 채팅방을 찾을 수 없습니다."));
        ChatRoomMessage chatRoomMessage = ChatRoomMessage.save(msgId, user, chatRoom, MessageType.TEXT);

        return Mono.just(chatRoomMessageRepository.save(chatRoomMessage));
    }

    @Transactional
    public void deleteChatMessageOnlyMe(UUID msgId, Long myId){
        ChatRoomMessage chatRoomMessage = chatRoomMessageRepository.findByMessageId(msgId)
                .orElseThrow(() -> new NotFoundException("해당 채팅 메시지를 찾을 수 없습니다."));
        chatRoomMessage.addDeleteMssUser(myId); // 개인 기기에서 해당 메시지를 지운 유저 아이디 추가
        chatRoomMessageRepository.save(chatRoomMessage);
    }

    @Transactional
    public Long deleteChatMessageAll(UUID msgId, Long myId){
        ChatRoomMessage chatRoomMessage = chatRoomMessageRepository.findByMessageId(msgId)
                .orElseThrow(() -> new NotFoundException("해당 채팅 메시지를 찾을 수 없습니다."));
        if (!chatRoomMessage.getUser().getId().equals(myId)) {
            throw new IllegalArgumentException("사용자가 작성한 메시지가 아니므로 지울 수 없습니다.");
        } else{
            chatRoomMessage.deleteMessageAll();
            chatRoomMessageRepository.save(chatRoomMessage);
            return chatRoomMessage.getChatRoom().getId();
        }
    }

}
