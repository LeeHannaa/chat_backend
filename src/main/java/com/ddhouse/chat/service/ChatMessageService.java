package com.ddhouse.chat.service;

import com.ddhouse.chat.domain.Apt;
import com.ddhouse.chat.domain.ChatMessage;
import com.ddhouse.chat.domain.ChatRoom;
import com.ddhouse.chat.domain.User;
import com.ddhouse.chat.dto.ChatMessageDto;
import com.ddhouse.chat.dto.ChatRoomDto;
import com.ddhouse.chat.exception.NotFlowException;
import com.ddhouse.chat.exception.NotFoundException;
import com.ddhouse.chat.repository.AptRepository;
import com.ddhouse.chat.repository.ChatMessageRepository;
import com.ddhouse.chat.repository.ChatRoomRepository;
import com.ddhouse.chat.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class ChatMessageService {
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final AptRepository aptRepository;
    private final ChatService chatService;

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
                            .switchIfEmpty(Mono.error(new NotFoundException("해당 유저와의 채팅이 없습니다.")));
                })
                .collectList()
                .flatMapMany(chatMessagesList -> {
                    chatMessagesList.sort(Comparator.comparing(ChatMessageDto::getCreatedDate));
                    return Flux.fromIterable(chatMessagesList)
                            .buffer(50);
                });
    }

    public Flux<List<ChatMessageDto>> getChatRoomByAptIdAndUserId(Long aptId, Long myId) {
        // 1. 기존에 채팅하던 방이 있는 경우
        Optional<ChatRoom> chatRoom = chatRoomRepository.findByAptIdAndUserId(aptId, myId);
        if (chatRoom.isPresent()) {
            return findChatMessages(chatRoom.get().getId());
        }

        // 2. 방을 생성해야하는 경우
        Optional<Apt> aptOptional = aptRepository.findById(aptId);
        if (aptOptional.isPresent()) {
            Apt apt = aptOptional.get();
            if (apt.getUser().getId().equals(myId)) {
                // 내가 올린 매물 내가 문의하기 누른 경우
                return Flux.error(new NotFlowException("비정상 플로우 : 내가 올린 매물 내가 문의하게 된 경우"));
            } else {
                // 새로운 방을 생성해야하는 경우 (1:1)
                System.out.println("새로운 방 생성");
                User user = userRepository.findById(myId).orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));
                ChatRoom createdChatRoom = chatService.createChatRoom(ChatRoomDto.createChatRoomDto(apt, user));
                return findChatMessages(createdChatRoom.getId());
            }
        }
        // aptId가 존재하지 않는 경우
        return Flux.error(new NotFoundException("해당 매물 정보를 찾을 수 없습니다."));
    }

    public Mono<ChatMessage> saveChatMessage(ChatMessageDto chat) {
        return chatMessageRepository.save(
                new ChatMessage(chat.getRoomId(), chat.getMsg(), chat.getWriterId(), chat.getCreatedDate()));
    }
}
