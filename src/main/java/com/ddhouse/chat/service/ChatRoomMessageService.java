package com.ddhouse.chat.service;

import com.ddhouse.chat.domain.ChatRoom;
import com.ddhouse.chat.domain.ChatRoomMessage;
import com.ddhouse.chat.domain.DeleteRange;
import com.ddhouse.chat.domain.User;
import com.ddhouse.chat.dto.request.ChatMessageRequestDto;
import com.ddhouse.chat.dto.response.ChatMessage.ChatMessageResponseToChatRoomDto;
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

    @Transactional
    public void deleteChatMessageOnlyMe(UUID msgId){
        /*
        - 나에게만 삭제 (ME)
        1. ChatRoomMessage에서 messeagId가 msgId인 객체 찾아내기
        2. ChatRoomMessage에서 isDelete를 Me로 변경
        */
        ChatRoomMessage chatRoomMessage = chatRoomMessageRepository.findByMessageId(msgId)
                .orElseThrow(() -> new NotFoundException("해당 채팅 메시지를 찾을 수 없습니다."));
        chatRoomMessage.changeDeleteRange(DeleteRange.ME);
    }

    @Transactional
    public void deleteChatMessageAll(UUID msgId, Long myId){
        /*
        - 상대방에게까지 삭제 (ALL)
        1. ChatRoomMessage에서 messeagId가 msgId인 객체 찾아내기
        2. 해당 객체를 작성한 userId가 myId와 동일하다면 ChatRoomMessage에서 isDelete를 ALL로 변경
        */
        ChatRoomMessage chatRoomMessage = chatRoomMessageRepository.findByMessageId(msgId)
                .orElseThrow(() -> new NotFoundException("해당 채팅 메시지를 찾을 수 없습니다."));
        if(chatRoomMessage.getUser().getId() != myId) {
            new Error("사용자가 작성한 메시지가 아니므로 지울 수 없습니다.");
        } else{
            chatRoomMessage.changeDeleteRange(DeleteRange.ALL);
        }
    }

}
