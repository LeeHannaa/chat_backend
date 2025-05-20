package com.ddhouse.chat.service;

import com.ddhouse.chat.domain.*;
import com.ddhouse.chat.dto.ChatRoomCreateDto;
import com.ddhouse.chat.dto.FcmDto;
import com.ddhouse.chat.dto.info.ChatRoomForAptDto;
import com.ddhouse.chat.dto.request.ChatMessageRequestDto;
import com.ddhouse.chat.dto.request.GuestMessageRequestDto;
import com.ddhouse.chat.dto.response.ChatMessage.ChatMessageResponseCreateDto;
import com.ddhouse.chat.dto.response.ChatMessage.ChatMessageResponseDto;
import com.ddhouse.chat.dto.info.ChatRoomDto;
import com.ddhouse.chat.dto.response.ChatMessage.ChatMessageResponseToFindMsgDto;
import com.ddhouse.chat.exception.NotFlowException;
import com.ddhouse.chat.exception.NotFoundException;
import com.ddhouse.chat.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
    private final UserService userService;
    private final AptService aptService;
    private final UserChatRoomService userChatRoomService;
    private final ChatRoomMessageRepository chatRoomMessageRepository;
    private final ChatRoomMessageService chatRoomMessageService;
    private final MessageUnreadService messageUnreadService;
    public int getRoomMemberNum(Long roomId){
        return chatRoomRepository.findById(roomId).get().getMemberNum();
    }

    public Mono<List<ChatMessageResponseDto>> findChatMessages(Long roomId, Long myId) {
        List<ChatRoomMessage> chatRoomMessages = chatRoomMessageRepository.findAllByChatRoomId(roomId);
        if(chatRoomMessages.isEmpty()){
            System.out.println("채팅 내역이 없는 경우 (채팅방만 존재) -> 채팅방의 정보만 보내기! ");
            ChatMessageResponseCreateDto chatMessageResponseCreateDto = ChatMessageResponseCreateDto.create(
                    userChatRoomRepository.findByChatRoomId(roomId).orElseThrow(() -> new NotFoundException("해당 채팅방의 정보를 찾을 수 없습니다."))
            );
            return Mono.just(Collections.singletonList(chatMessageResponseCreateDto));
        }
        LocalDateTime standardTime = userChatRoomRepository
                .findByUserIdAndChatRoomId(myId, roomId)
                .orElseThrow(() -> new NotFoundException("채팅방 정보가 없습니다."))
                .getEntryTime();

        Mono<List<ChatMessageResponseDto>> chatRoomMessagesMono = Flux.fromIterable(chatRoomMessages)
                .filter(chatRoomMessage -> chatRoomMessage.getRegDate().isAfter(standardTime)) // standardTime 이후만
                // 내 기기에서 삭제된 메시지는 제외하긴 하는데 그 경우 userId가 나의 id와 동일한 경우에만 제외
                .filter(chatRoomMessage -> !chatRoomMessage.getDeleteUserList().contains(myId))
                .flatMap(chatRoomMessage -> {
                    UUID msgId = chatRoomMessage.getMessageId();
                    return chatMessageRepository.findById(msgId)
                            .flatMap(chatMessage -> {
                                if(chatRoomMessage.getType() == MessageType.TEXT){
                                    // TODO G **: 각 메시지마다 읽지 않은 유저의 수를 함께 전달
                                    int unreadCountByMsgId = messageUnreadService.getUnreadCountByMsgId(chatRoomMessage.getChatRoom().getId().toString(), msgId.toString());
                                    if (chatRoomMessage.getIsDelete()) {
                                        // 전체 삭제된 메시지 처리
                                        //        * like kakaoTalk (전체 삭제일 경우도 그냥 아예 삭제하는 피드백 반영 *
                                        // return Mono.just(ChatMessageResponseToFindMsgDto.fromAllDelete(chatMessage, chatRoomMessage, unreadCountByMsgId));
                                        return Mono.empty();
                                    } else {
                                        return Mono.just(ChatMessageResponseToFindMsgDto.from(chatMessage, chatRoomMessage, unreadCountByMsgId));
                                    }
                                }
                                // SYSTEM 타입의 메시지일 경우 -> isDelete가 true면 유저가 다시 초대되었다는 뜻!!!!!
                                return Mono.just(ChatMessageResponseToFindMsgDto.deleteFrom(chatMessage, chatRoomMessage));
                            });
                })
                .collectList()
                .map(chatMessages -> {
                    // 오래된 날짜 순서대로
                    chatMessages.sort(Comparator.comparing(ChatMessageResponseToFindMsgDto::getCreatedDate));
                    List<ChatMessageResponseDto> result = chatMessages.stream()
                            .map(msg -> (ChatMessageResponseDto) msg)  // 상속 관계를 이용한 형 변환
                            .collect(Collectors.toList());
                    return result;
            });
        return chatRoomMessagesMono;
    }


    public Mono<List<ChatMessageResponseDto>> getChatRoomByAptIdAndUserId(Long aptId, Long myId) {
        // 1. 기존에 채팅하던 방이 있는 경우
        List<ChatRoomForAptDto> chatRooms = chatService.findMyChatRoomListForApt(myId);
        if (!chatRooms.isEmpty()) {
            for (ChatRoomForAptDto chatRoomForAptDto : chatRooms) {
                if (chatRoomForAptDto.getApt() != null && chatRoomForAptDto.getApt().getId().equals(aptId)) {
                    return findChatMessages(chatRoomForAptDto.getRoomId(), myId);
                }
            }
        }
        // 2. 방을 생성해야하는 경우
        Optional<Apt> aptOptional = aptRepository.findById(aptId);
        if (aptOptional.isPresent()) {
            Apt apt = aptOptional.get();
            if (apt.getUser().getId().equals(myId)) {
                // 내가 올린 매물 내가 문의하기 누른 경우
                return Mono.error(new NotFlowException("비정상 플로우 : 내가 올린 매물 내가 문의하게 된 경우"));
            } else {
                // 새로운 방을 생성해야하는 경우 (1:1)
                System.out.println("새로운 방 생성");
                User user = userRepository.findById(myId).orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));
                UserChatRoom createdChatRoom = chatService.createChatRoom(ChatRoomDto.createChatRoomDto(apt, user));
                ChatMessageResponseCreateDto newRoomInfo = ChatMessageResponseCreateDto.create(createdChatRoom);
                return Mono.just(Collections.singletonList(newRoomInfo));
            }
        }
        // aptId가 존재하지 않는 경우
        return Mono.error(new NotFoundException("해당 매물 정보를 찾을 수 없습니다."));
    }

    public Mono<ChatMessage> saveChatMessage(ChatMessageRequestDto chatMessageRequestDto) {
        return chatMessageRepository.save(ChatMessage.from(chatMessageRequestDto.getMsg()))
                .flatMap(savedMessage -> {
                    UUID msgId = savedMessage.getId();
                    return chatRoomMessageService.saveChatRoomMessage(chatMessageRequestDto, msgId)
                            .thenReturn(savedMessage);
                });
    }


    public List<Long> findReceiverId(ChatMessageRequestDto chatMessageRequestDto){ // 소켓 통신할 때 수신자 id 찾기
        return userChatRoomRepository.findAllByChatRoomId(chatMessageRequestDto.getRoomId())
                .stream()
                .map(userChatRoom -> userChatRoom.getUser().getId())
                .filter(userId -> !userId.equals(chatMessageRequestDto.getWriterId()))
                .collect(Collectors.toList());
    }

    public UserChatRoom getUserInChatRoom(Long userId, Long roomId){
        return userChatRoomRepository.findByUserIdAndChatRoomId(userId, roomId)
                .orElseThrow(() -> new NotFoundException("채팅방에 해당 유저가 존재하지 않습니다."));
    }

    public void saveReEntryUserInChatRoom(UserChatRoom userChatRoom){
        userChatRoom.reEntryInChatRoom();
        userChatRoomRepository.save(userChatRoom);
    }


    public void sendMessageGuest(GuestMessageRequestDto guestMessageRequestDto){
        /*
        [ 비회원 매물 문의 시 채팅으로 넘어가는 과정 ]
        1. chatRoom에서 phoneNumber로 존재하는 채팅방 있는지 확인
        2. request에서 apt를 통해 userId를 받아오기
        3. 1에서 존재하는 채팅방이 있었다면 UserChatRoom에서 userId랑 roomId를 통해 존재하는 방이 있는지 확인
            3-2. 채팅방을 생성해야하는 경우
                3-2-1. ChatRoom 생성
                3-2-2. UserChatRoom 생성
                3-2-3. 메시지 저장
                3-1-2. 접속되어있지 않다면 CHATLIST로 userId 경로로 메시지 소켓 전송
                    3-1-2-1. 메시지 안읽음 처리
            3-1. 존재하는 채팅방이 있다면 해당 채팅방에 userId가 접속되어있는지 확인
                3-1. 메시지 저장
                3-1-1. 접속되어있다면 CHAT으로 메시지 소켓전송
                    3-1-1-1. 메시지 읽음 처리 (안읽음으로 redis에 저장하는 과정 생략)
                3-1-2. 접속되어있지 않다면 CHATLIST로 userId 경로로 메시지 소켓 전송
                    3-1-2-1. 메시지 안읽음 처리
        */
        Apt apt = aptService.findByAptId(guestMessageRequestDto.getAptId());
        User user = apt.getUser();
        // 해당 비회원의 채팅방이 존재하는지 여부
        List<ChatRoom> chatRooms = chatService.findChatRoomByPhoneNumber(guestMessageRequestDto.getPhoneNumber());
        if(chatRooms == null && user != null) {
            // 방을 생성해야하는 경우
            UserChatRoom createdChatRoom = chatService.createChatRoomByGuest(ChatRoomCreateDto.to(guestMessageRequestDto, user));
        } else {
            // 비회원과 매물 소유자의 채팅방이 있는지 확인
            // TODO : 있는지 없는지 확인하고 돌려주는 시스템으로 가야함!!
            UserChatRoom userChatRoom = userChatRoomService.findByUserAndChatRoom(chatRooms, user);
            if(userChatRoom != null){
                // 기존의 채팅방이 존재하는 경우

            } else {
                // 방을 생성해야하는 경우
                UserChatRoom createdChatRoom = chatService.createChatRoomByGuest(ChatRoomCreateDto.to(guestMessageRequestDto, user));
            }

        }
    }
}
