package com.ddhouse.chat.service;

import com.ddhouse.chat.domain.Apt;
import com.ddhouse.chat.domain.ChatMessage;
import com.ddhouse.chat.domain.ChatRoom;
import com.ddhouse.chat.dto.ChatMessageDto;
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

        // 2. 나와의 채팅을 고려한 경우 / 방을 생성해야하는 경우
        Optional<Apt> aptOptional = aptRepository.findById(aptId);
        if (aptOptional.isPresent()) {
            Apt apt = aptOptional.get();
            if (apt.getUser().getId().equals(myId)) {
                // 내가 올린 매물 내가 문의하기 누른 경우
                System.out.println("내가 올린 매물에 내가 문의하기 누른 것");
                return Flux.empty();
            } else {
                // 새로운 방을 생성해야하는 경우
                System.out.println("새로운 방 생성");
                return Flux.empty();
            }
        }
        // aptId가 존재하지 않으면 Optional.empty() 반환
        return Flux.empty();
    }

    public Mono<ChatMessage> saveChatMessage(ChatMessageDto chat) {
        return chatMessageRepository.save(
                new ChatMessage(chat.getRoomId(), chat.getMsg(), chat.getWriterId()));
    }
}
