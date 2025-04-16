package com.ddhouse.chat.service;


import com.ddhouse.chat.domain.*;
import com.ddhouse.chat.dto.info.ChatRoomDto;
import com.ddhouse.chat.dto.info.ChatRoomForAptDto;
import com.ddhouse.chat.dto.response.ChatRoomInfoResponseDto;
import com.ddhouse.chat.exception.NotFoundException;
import com.ddhouse.chat.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    private final MessageUnreadService messageUnreadService;
    private final ChatRoomMessageRepository chatRoomMessageRepository;

    public UserChatRoom createChatRoom(ChatRoomDto chatRoomDto) {
        ChatRoom chatRoom = chatRoomRepository.save(ChatRoom.from(chatRoomDto));
        userChatRoomRepository.save(UserChatRoom.otherFrom(chatRoomDto, chatRoom));
        return userChatRoomRepository.save(UserChatRoom.from(chatRoomDto, chatRoom));
    }

    // 채팅 전체 리스트
//    public List<ChatRoomDto> findChatRoomList() {
//        List<ChatRoom> chatRooms = chatRoomRepository.findAll();
//        return chatRooms.stream().map(ChatRoomDto::from).collect(Collectors.toList());
//    }

    public List<ChatRoomInfoResponseDto> findMyChatRoomList(Long myId) {
        // 내가 문의자로 들어간 채팅방 or 내가 관리자로 있는 채팅방
        List<UserChatRoom> chatRooms = userChatRoomRepository.findByUserId(myId);
        return chatRooms.stream()
                .filter(UserChatRoom::getIsInRoom) // 나가기 하지 않은 채팅방만
                .map(UserChatRoom::getChatRoom)
                .filter(chatRoom -> chatRoomMessageRepository.existsByChatRoomId(chatRoom.getId())) // 메시지가 존재하는 경우만 필터링
                .map(ChatRoomInfoResponseDto::create)
                .collect(Collectors.toList());
    }

    public List<ChatRoomForAptDto> findMyChatRoomListForApt(Long myId) {
        // 내가 문의자로 들어간 채팅방 or 내가 관리자로 있는 채팅방
        List<UserChatRoom> chatRooms = userChatRoomRepository.findByUserId(myId);
        return chatRooms.stream()
                .filter(UserChatRoom::getIsInRoom) // 나가기 하지 않은 채팅방만
                .map(UserChatRoom::getChatRoom)
                .filter(chatRoom -> chatRoomMessageRepository.existsByChatRoomId(chatRoom.getId())) // 메시지가 존재하는 경우만 필터링
                .map(ChatRoomForAptDto::from)
                .collect(Collectors.toList());
    }


    public Mono<Tuple3<String, LocalDateTime, Long>> getLastMessageWithUnreadCount(Long roomId, Long myId) {
        // chatRoomMessage에서 해당 roomId에서 createTime을 보고 가장 최신의 (+ isDelete가 ME이면서 myId랑 writerId가 같은 경우를 제외) messageId를 가져와서 ChatMessage 테이블에서 msg 가져오기
        // chatRoomMessage에서 마지막 메시지의 isDelete가 ALL이면 "삭제된 메시지 입니다."로 설정
        ChatRoomMessage chatRoomMessage = chatRoomMessageRepository.findTopByChatRoomIdOrderByRegDateDesc(roomId);
        ChatRoomMessage chatRoomMessageNotMe = chatRoomMessageRepository.findTopByChatRoomIdAndIsDeleteNotOrderByRegDateDesc(roomId, DeleteRange.ME);

        if(chatRoomMessageNotMe == null){
            // 전체를 빈값으로 전달, 날짜는 받아옴
            return Mono.just(Tuples.of("", chatRoomMessage.getRegDate(), 0L));
        }
        Mono<Tuple2<String, LocalDateTime>> lastMessageMono = chatMessageRepository.findById(chatRoomMessageNotMe.getMessageId())
                .map(chatMessage -> {
                    String msgContent = chatRoomMessageNotMe.getIsDelete() == DeleteRange.ALL
                            ? "삭제된 메시지입니다."
                            : chatMessage.getMsg();
                    return Tuples.of(msgContent, chatRoomMessageNotMe.getRegDate());
                });

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

    public String findRoomName(Long roomId) {
        return chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new NotFoundException("해당 채팅방 정보를 찾을 수 없습니다."))
                .getName();
    }
    @Transactional
    public void increaseNumberInChatRoom(Long roomId){
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                        .orElseThrow(() -> new NotFoundException("해당 채팅방 정보를 찾을 수 없습니다."));
        chatRoom.increaseMemberNum();
        chatRoomRepository.save(chatRoom);
    }

    @Transactional
    public void deleteChatRoom(Long roomId, Long myId){
        /*
        [ 채팅방 삭제 로직 ]
        1. roomId를 가지고 ChatRoom에 memberNum 감소
            1-1. memberNum이 0인 경우 해당 roomId를 가진 UserChatRoom 데이터 전체 삭제 & ChatRoomMessage 데이터 전체 삭제 (카산드라는 삭제할지 말지 나중에 결정)
            1-2. memberNum이 0이 아닌 경우 : UserChatRoom에 leaveTheChatRoom 실행 (isInRoom을 F로, entryTime 시간 업데이트)
        */
        // 채팅 이용자가 0명인 경우 전체 대화 삭제
        Optional<ChatRoom> chatRoomOptional = chatRoomRepository.findById(roomId);
        chatRoomOptional.ifPresent(chatRoom -> {
            chatRoom.decreaseMemberNum(); // memberNum 감소
            if (chatRoom.getMemberNum() == 0) {
                // roomId를 가진 UserChatRoom 데이터 전체 삭제 & ChatRoomMessage 데이터 전체 삭제
                userChatRoomRepository.deleteByChatRoomId(roomId);
                chatRoomMessageRepository.deleteByChatRoomId(roomId);
                chatRoomRepository.deleteById(roomId);
            }
            else {
                // UserChatRoom에 leaveTheChatRoom 실행 (isInRoom을 F로, entryTime 시간 업데이트)
                UserChatRoom userChatRoom = userChatRoomRepository.findByUserIdAndChatRoomId(myId, roomId)
                        .orElseThrow(() -> new NotFoundException("해당 채팅방에 존재하는 유저의 정보를 알 수 없습니다."));
                userChatRoom.leaveTheChatRoom();
                userChatRoomRepository.save(userChatRoom); // 변경사항 저장
            }
        });
    }
}
