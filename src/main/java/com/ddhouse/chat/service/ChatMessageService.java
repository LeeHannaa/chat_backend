package com.ddhouse.chat.service;

import com.ddhouse.chat.domain.*;
import com.ddhouse.chat.dto.ChatRoomCreateDto;
import com.ddhouse.chat.dto.ChatRoomForAptDto;
import com.ddhouse.chat.dto.SaveMessageDto;
import com.ddhouse.chat.dto.request.message.ChatMessageRequestDto;
import com.ddhouse.chat.dto.response.FcmDto;
import com.ddhouse.chat.dto.response.message.ChatMessageResponseToChatRoomDto;
import com.ddhouse.chat.dto.response.chatRoom.ChatRoomListResponseDto;
import com.ddhouse.chat.dto.request.message.GuestMessageRequestDto;
import com.ddhouse.chat.dto.response.message.ChatMessageResponseCreateDto;
import com.ddhouse.chat.dto.response.message.ChatMessageResponseDto;
import com.ddhouse.chat.dto.ChatRoomDto;
import com.ddhouse.chat.dto.response.message.ChatMessageResponseToFindMsgDto;
import com.ddhouse.chat.exception.NotFlowException;
import com.ddhouse.chat.exception.NotFoundException;
import com.ddhouse.chat.fcm.service.FcmService;
import com.ddhouse.chat.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
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
    private final RoomUserCountService roomUserCountService;
    private final FcmService fcmService;
    private final SimpMessageSendingOperations template;

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

    public Mono<ChatMessage> saveChatMessage(SaveMessageDto saveMessageDto) {
        return chatMessageRepository.save(ChatMessage.from(saveMessageDto.getMsg()))
                .flatMap(savedMessage -> {
                    UUID msgId = savedMessage.getId();
                    return chatRoomMessageService.saveChatRoomMessage(saveMessageDto, msgId)
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


    public Mono<ChatMessage> sendMessageGuest(GuestMessageRequestDto guestMessageRequestDto){
        Apt apt = aptService.findByAptId(guestMessageRequestDto.getAptId());
        User user = apt.getUser();
        // 해당 비회원의 채팅방이 존재하는지 여부
        List<ChatRoom> chatRooms = chatService.findChatRoomByPhoneNumber(guestMessageRequestDto.getPhoneNumber());
        if(chatRooms != null && user != null){
            UserChatRoom userChatRoom = userChatRoomService.findByUserAndChatRoom(chatRooms, user);
            if(userChatRoom != null){
                // 기존 방이 존재하는 경우
                // 메시지 저장
                Mono<ChatMessage> chatMessageMono = saveChatMessage(SaveMessageDto.guest(guestMessageRequestDto, userChatRoom.getChatRoom().getId()));
                // 기존의 채팅방이 존재하는 경우 현재 방에 사용자가 있는지 확인
                int countInRoom = roomUserCountService.getUserCount(userChatRoom.getChatRoom().getId());
                return chatMessageMono.flatMap(chatMessage -> {
                    if(countInRoom < 1) {
                        // 채팅방에 사용자가 없는 경우
                        sendSocketChatListAndFcm(userChatRoom, guestMessageRequestDto, chatMessage);
                    } else{
                        // 채팅방에 사용자가 들어와 있는 경우
                        template.convertAndSend("/topic/chatroom/" + userChatRoom.getChatRoom().getId(),
                                Map.of(
                                        "type", "CHAT",
                                        "message", ChatMessageResponseToChatRoomDto.guest(chatMessage, guestMessageRequestDto, userChatRoom.getChatRoom().getId(), MessageType.TEXT)
                                )
                        );
                    }
                    return Mono.just(chatMessage);
                });
            }
        }
        // 방을 생성해야하는 경우
        UserChatRoom createdChatRoom = chatService.createChatRoomByGuest(ChatRoomCreateDto.to(guestMessageRequestDto, user));
        Mono<ChatMessage> chatMessageMono = saveChatMessage(SaveMessageDto.guest(guestMessageRequestDto, createdChatRoom.getChatRoom().getId()));
        return chatMessageMono.flatMap(chatMessage -> {
            sendSocketChatListAndFcm(createdChatRoom, guestMessageRequestDto, chatMessage);
            return Mono.just(chatMessage);
        });
    }

    public void sendSocketChatListAndFcm(UserChatRoom userChatRoom, GuestMessageRequestDto guestMessageRequestDto, ChatMessage chatMessage){
        // 메시지 안읽음 처리
        messageUnreadService.addUnreadChat(userChatRoom.getChatRoom().getId().toString(), userChatRoom.getUser().getId().toString(), chatMessage.getId());
        Long unreadCount = messageUnreadService.getUnreadMessageCount(userChatRoom.getChatRoom().getId().toString(),  userChatRoom.getUser().getId().toString());
        template.convertAndSend(
                "/topic/user/" + userChatRoom.getUser().getId(),
                Map.of(
                        "type", "CHATLIST",
                        "message", ChatRoomListResponseDto.guest(guestMessageRequestDto, unreadCount, userChatRoom.getChatRoom())
                ));
        // FCM 알림 전송
        String fcmToken = userService.findFcmTokenByUserId(userChatRoom.getUser().getId());
        String body = "비회원 : " + chatMessage.getMsg();
        if (fcmToken != null) {
            try {
                fcmService.sendMessageTo(
                        FcmDto.chat(fcmToken, body, userChatRoom.getChatRoom().getId().toString(), guestMessageRequestDto.getPhoneNumber()));
                System.out.println("fcm 알림 전송 완료!");
            } catch (IOException e) {
                System.err.println("FCM 전송 실패: " + e.getMessage());
            }
        }
    }
}
