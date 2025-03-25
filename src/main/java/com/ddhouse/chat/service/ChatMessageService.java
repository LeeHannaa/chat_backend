package com.ddhouse.chat.service;

import com.ddhouse.chat.domain.*;
import com.ddhouse.chat.dto.request.ChatMessageRequestDto;
import com.ddhouse.chat.dto.response.ChatMessageResponseDto;
import com.ddhouse.chat.dto.ChatRoomDto;
import com.ddhouse.chat.exception.NotFlowException;
import com.ddhouse.chat.exception.NotFoundException;
import com.ddhouse.chat.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class ChatMessageService {
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserChatRoomRepository userChatRoomRepository;
    private final UserRepository userRepository;
    private final AptRepository aptRepository;
    private final ChatService chatService;

    public Flux<List<ChatMessageResponseDto>> findChatMessages(Long roomId) {
        Flux<ChatMessage> chatMessages = chatMessageRepository.findAllByRoomId(roomId);
        // TODO : 메시지가 없다면 (길이가 0) -> 방의 정보를 담아서 넘기기
        return chatMessages
                .flatMap(chatMessage -> {
                    ChatMessageResponseDto dto = ChatMessageResponseDto.from(chatMessage);

                    return Mono.justOrEmpty(userRepository.findById(dto.getWriterId()))
                            .flatMap(user -> {
                                dto.setWriterName(user.getName());
                                return Mono.just(dto);
                            })
                            .switchIfEmpty(Mono.error(new NotFoundException("해당 유저와의 채팅이 없습니다.")));
                })
                .collectList()
                .flatMapMany(chatMessagesList -> {
                    if (chatMessagesList.isEmpty()) {
                        // 메시지가 없다면 방 정보를 반환
                        System.out.println("채팅방만 있는 경우 -> 채팅방의 정보만 보내기! ");
                        ChatMessageResponseDto defaultChatRoom = ChatMessageResponseDto.create(
                                userChatRoomRepository.findByChatRoomId(roomId)
                                        .orElseThrow(() -> new NotFoundException("해당 채팅방 정보를 찾을 수 없습니다." + roomId))
                        );
                        return Flux.just(Collections.singletonList(defaultChatRoom));
                    }
                    // 메시지가 있으면 날짜 정렬 후 반환
                    chatMessagesList.sort(Comparator.comparing(ChatMessageResponseDto::getCreatedDate));
                    return Flux.fromIterable(chatMessagesList)
                            .buffer(50);
                });
    }


    public Flux<List<ChatMessageResponseDto>> getChatRoomByAptIdAndUserId(Long aptId, Long myId) {
        // TODO : 매물 목록에서 채팅 문의하기 할 경우 나의 아이디와 해당 매물 아이디로 기존 방이 있는지 잘 못찾아내고 있음
        // 1. 기존에 채팅하던 방이 있는 경우
        // 1-1. 내 아이디로 나의 채팅방 불러오기
        List<ChatRoomDto> chatRooms = chatService.findMyChatRoomList(myId);
        // 1-2. chatRooms에서 aptId랑 파라미터 aptId랑 비교해서 동일한 데이터가 있으면 채팅방이 있는 경우!
        if (!chatRooms.isEmpty()) {
            for (ChatRoomDto chatRoom : chatRooms) {
                if (chatRoom.getApt().getId().equals(aptId)) {
                    return findChatMessages(chatRoom.getId());
                }
            }
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
                UserChatRoom createdChatRoom = chatService.createChatRoom(ChatRoomDto.createChatRoomDto(apt, user));
                ChatMessageResponseDto newRoomInfo = ChatMessageResponseDto.create(createdChatRoom);
                return Flux.just(Collections.singletonList(newRoomInfo));
            }
        }
        // aptId가 존재하지 않는 경우
        return Flux.error(new NotFoundException("해당 매물 정보를 찾을 수 없습니다."));
    }

    public Mono<ChatMessage> saveChatMessage(ChatMessageRequestDto chatMessageRequestDto) {
        return chatMessageRepository.save(
                new ChatMessage(chatMessageRequestDto));
    }
}
