package com.ddhouse.chat.service;


import com.ddhouse.chat.domain.Apt;
import com.ddhouse.chat.domain.ChatMessage;
import com.ddhouse.chat.domain.ChatRoom;
import com.ddhouse.chat.domain.UserChatRoom;
import com.ddhouse.chat.dto.ChatRoomDto;
import com.ddhouse.chat.repository.AptRepository;
import com.ddhouse.chat.repository.ChatMessageRepository;
import com.ddhouse.chat.repository.ChatRoomRepository;
import com.ddhouse.chat.repository.UserChatRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class ChatService {
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserChatRoomRepository userChatRoomRepository;
    private final AptRepository aptRepository;
    private final MessageUnreadService messageUnreadService;

    public UserChatRoom createChatRoom(ChatRoomDto chatRoomDto) {
        ChatRoom chatRoom = chatRoomRepository.save(ChatRoom.from(chatRoomDto));
        return userChatRoomRepository.save(UserChatRoom.from(chatRoomDto, chatRoom));
    }

    // 채팅 전체 리스트
    public List<ChatRoomDto> findChatRoomList() {
        List<ChatRoom> chatRooms = chatRoomRepository.findAll();
        return chatRooms.stream().map(ChatRoomDto::from).collect(Collectors.toList());
    }

    public List<ChatRoomDto> findMyChatRoomList(Long myId) {
        // 내가 문의자로 들어간 채팅방 or 내가 관리자로 있는 채팅방
        List<UserChatRoom> chatRooms = userChatRoomRepository.findByUserIdOrConsultId(myId);
        return chatRooms.stream()
                .map(UserChatRoom::getChatRoom)
                .map(ChatRoomDto::from)
                .collect(Collectors.toList());
    }


    public Mono<Tuple2<String, LocalDateTime>> getLastMessage(Long roomId) {
        return chatMessageRepository.findAllByRoomId(roomId)
                .sort((m1, m2) -> m2.getCreatedDate().compareTo(m1.getCreatedDate()))  // 날짜 내림차순 정렬
                .next()  // 가장 첫 번째 (최신) 메시지를 가져옴
                .map(chatMessage -> Tuples.of(chatMessage.getMsg(), chatMessage.getCreatedDate()));  // 메시지와 날짜 함께 반환
    }


    public Mono<Tuple3<String, LocalDateTime, Long>> getLastMessageWithUnreadCount(Long roomId, Long myId) {
        Mono<Tuple2<String, LocalDateTime>> lastMessageMono = chatMessageRepository.findAllByRoomId(roomId)
                .sort((m1, m2) -> m2.getCreatedDate().compareTo(m1.getCreatedDate()))
                .next()
                .map(msg -> Tuples.of(msg.getMsg(), msg.getCreatedDate()));

        Long unreadCount = messageUnreadService.getUnreadMessageCount(roomId.toString(), myId.toString());
        System.out.println("안읽은 메시지 수 확인해보기 :" + unreadCount);
        Mono<Long> unreadCountMono = Mono.just(unreadCount);

        return Mono.zip(lastMessageMono, unreadCountMono)
                .map(tuple -> Tuples.of(
                        tuple.getT1().getT1(), // msg
                        tuple.getT1().getT2(), // createdDate
                        tuple.getT2()          // unreadCount
                ));
    }





    public Flux<ChatMessage> findMessagesByRoomId(Long roomId) {
        return chatMessageRepository.findAllByRoomId(roomId);
    }
    @Transactional
    public void deleteChatRoom(Long roomId){
        // 채팅 이용자가 0명인 경우 전체 대화 삭제
        Optional<ChatRoom> chatRoomOptional = chatRoomRepository.findById(roomId);

        chatRoomOptional.ifPresent(chatRoom -> {
            chatRoom.decreaseMemberNum(); // memberNum 감소

            if (chatRoom.getMemberNum() == 0) {
                // TODO : 카산드라 db 내용 해당 채팅방 데이터들을 먼저 다 지우기 -> 아이디로 지워야함... 고민....
                // TODO : 인원 줄여도 채팅방을 보내줄 때 다시 채팅방이 넘어감....
//                findMessagesByRoomId(roomId)
//                        .flatMap(chatMessage -> {
//                            System.out.println(chatMessage.getId());  // 아이디 출력
//                            return chatMessageRepository.deleteById(chatMessage.getId());  // 삭제 작업
//                        })
//                        .then();
                // 해당 채팅방 지우기
                userChatRoomRepository.deleteByChatRoomId(roomId);
                chatRoomRepository.deleteById(roomId);
            }
            else chatRoomRepository.save(chatRoom); // 변경사항 저장
        });


    }
}
