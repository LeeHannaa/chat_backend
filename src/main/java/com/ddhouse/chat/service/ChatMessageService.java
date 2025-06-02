package com.ddhouse.chat.service;

import com.ddhouse.chat.dto.ChatRoomCreateDto;
import com.ddhouse.chat.dto.SaveMessageDto;
import com.ddhouse.chat.dto.request.message.ChatMessageRequestDto;
import com.ddhouse.chat.dto.response.CreatedChatRoomFromAptDto;
import com.ddhouse.chat.dto.response.FcmDto;
import com.ddhouse.chat.dto.response.message.ChatMessageResponseToChatRoomDto;
import com.ddhouse.chat.dto.response.chatRoom.ChatRoomListResponseDto;
import com.ddhouse.chat.dto.request.message.GuestMessageRequestDto;
import com.ddhouse.chat.dto.response.message.ChatMessageResponseCreateDto;
import com.ddhouse.chat.dto.response.message.ChatMessageResponseDto;
import com.ddhouse.chat.dto.ChatRoomDto;
import com.ddhouse.chat.dto.response.message.ChatMessageResponseToFindMsgDto;
import com.ddhouse.chat.fcm.service.FcmService;
import com.ddhouse.chat.repository.*;
import com.ddhouse.chat.vo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class ChatMessageService {
    private final UserChatRoomRepository userChatRoomRepository;
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

    public List<ChatMessageResponseDto> findChatMessages(Long roomId, Long myId) {
        // 해당 방에 있는 모든 채팅 메시지
        List<ChatRoomMessage> chatRoomMessages = chatRoomMessageRepository.findAllByChatRoomId(roomId);
        if(chatRoomMessages.isEmpty()){
            System.out.println("채팅 내역이 없는 경우 (채팅방만 존재) -> 채팅방의 정보만 보내기! ");
            ChatMessageResponseCreateDto chatMessageResponseCreateDto = ChatMessageResponseCreateDto.create(
                    userChatRoomRepository.findByUserIdAndChatRoomId(myId, roomId)
            );
            return Collections.singletonList(chatMessageResponseCreateDto);
        }
        LocalDateTime standardTime = userChatRoomRepository
                .findByUserIdAndChatRoomId(myId, roomId)
                .getEntryTime();

        List<ChatMessageResponseDto> chatRoomMessagesFiltered = chatRoomMessages.stream()
                .filter(chatRoomMessage -> chatRoomMessage.getRegDate().isAfter(standardTime)) // standardTime 이후만
                // 내 기기에서 삭제된 메시지는 제외하긴 하는데 그 경우 userId가 나의 id와 동일한 경우에만 제외
                .filter(chatRoomMessage -> !chatRoomMessage.getDeleteUserList().contains(myId))
                .map(chatRoomMessage -> {
                    if(chatRoomMessage.getType() == MessageType.TEXT){
                        // TODO G **: 각 메시지마다 읽지 않은 유저의 수를 함께 전달
                        int unreadCountByMsgId = messageUnreadService.getUnreadCountByMsgId(chatRoomMessage.getChatRoom().getId().toString(), chatRoomMessage.getId().toString());
                        if (chatRoomMessage.getIsDelete()) {
                            // 전체 삭제된 메시지 처리
                            //        * like kakaoTalk (전체 삭제일 경우도 그냥 아예 삭제하는 피드백 반영 *
                            // return Mono.just(ChatMessageResponseToFindMsgDto.fromAllDelete(chatMessage, chatRoomMessage, unreadCountByMsgId));
                            return null;
                        } else {
                            return ChatMessageResponseToFindMsgDto.from(chatRoomMessage, unreadCountByMsgId);
                        }
                    }
                    // SYSTEM 타입의 메시지일 경우 -> isDelete가 true면 유저가 다시 초대되었다는 뜻!!!!!
                    return ChatMessageResponseToFindMsgDto.deleteFrom(chatRoomMessage);
                })
                .filter(Objects::nonNull) // null 값 제거
                .sorted(Comparator.comparing(ChatMessageResponseToFindMsgDto::getCreatedDate)) // 날짜 오름차순 정렬
                .collect(Collectors.toList());
        return chatRoomMessagesFiltered;
    }

    public List<Long> findReceiverId(ChatMessageRequestDto chatMessageRequestDto){ // 소켓 통신할 때 수신자 id 찾기
        return userChatRoomRepository.findAllByChatRoomId(chatMessageRequestDto.getRoomId())
                .stream()
                .map(userChatRoom -> userChatRoom.getUser().getId())
                .filter(userId -> !userId.equals(chatMessageRequestDto.getWriterId()))
                .collect(Collectors.toList());
    }

    public UserChatRoom getUserInChatRoom(Long userId, Long roomId){
        return userChatRoomRepository.findByUserIdAndChatRoomId(userId, roomId);
    }

    public void saveReEntryUserInChatRoom(UserChatRoom userChatRoom){
        userChatRoom.reEntryInChatRoom();
        userChatRoomRepository.save(userChatRoom);
    }

    public ChatRoomMessage saveChatMessage(SaveMessageDto saveMessageDto) {
        ChatRoomMessage chatRoomMessage = chatRoomMessageService.saveChatRoomMessage(saveMessageDto);
        System.out.println("chatRoomMessage 저장 완료");
        return chatRoomMessage;
    }

    public ChatRoomMessage sendMessageGuest(GuestMessageRequestDto guestMessageRequestDto){
        Apt apt = aptService.findByAptId(guestMessageRequestDto.getAptId());
        User user = apt.getUser();
        // 해당 비회원의 채팅방이 존재하는지 여부
        List<ChatRoom> chatRooms = chatService.findChatRoomsByPhoneNumber(guestMessageRequestDto.getPhoneNumber());
        if(chatRooms != null && user != null){
            UserChatRoom userChatRoom = userChatRoomService.findByUserAndChatRoom(chatRooms, user);
            if(userChatRoom != null){
                // 기존 방이 존재하는 경우
                // 메시지 저장
                ChatRoomMessage chatRoomMessage = saveChatMessage(SaveMessageDto.guest(guestMessageRequestDto, userChatRoom.getChatRoom().getId()));
                // 기존의 채팅방이 존재하는 경우 현재 방에 사용자가 있는지 확인
                int countInRoom = roomUserCountService.getUserCount(userChatRoom.getChatRoom().getId());
//                return chatMessageMono.flatMap(chatMessage -> {
                    if(countInRoom < 1) {
                        // 채팅방에 사용자가 없는 경우
                        sendSocketChatListAndFcmToGuest(userChatRoom, guestMessageRequestDto, chatRoomMessage);
                    } else{
                        // 채팅방에 사용자가 들어와 있는 경우
                        template.convertAndSend("/topic/chatroom/" + userChatRoom.getChatRoom().getId(),
                                Map.of(
                                        "type", "CHAT",
                                        "message", ChatMessageResponseToChatRoomDto.guest(guestMessageRequestDto, userChatRoom.getChatRoom().getId(), MessageType.TEXT, chatRoomMessage)
                                )
                        );
                    }
                    return chatRoomMessage;
            }
        }
        // 방을 생성해야하는 경우
        UserChatRoom createdChatRoom = chatService.createChatRoomByGuest(ChatRoomCreateDto.guest(guestMessageRequestDto, user));
        ChatRoomMessage chatRoomMessage = saveChatMessage(SaveMessageDto.guest(guestMessageRequestDto, createdChatRoom.getChatRoom().getId()));
        sendSocketChatListAndFcmToGuest(createdChatRoom, guestMessageRequestDto, chatRoomMessage);
        return chatRoomMessage;

    }

    public void sendSocketChatListAndFcmToGuest(UserChatRoom userChatRoom, GuestMessageRequestDto guestMessageRequestDto, ChatRoomMessage chatRoomMessage){
        // 메시지 안읽음 처리
        messageUnreadService.addUnreadChat(userChatRoom.getChatRoom().getId().toString(), userChatRoom.getUser().getId().toString(), chatRoomMessage.getId().toString());
        Long unreadCount = messageUnreadService.getUnreadMessageCount(userChatRoom.getChatRoom().getId().toString(),  userChatRoom.getUser().getId().toString());
        template.convertAndSend(
                "/topic/user/" + userChatRoom.getUser().getId(),
                Map.of(
                        "type", "CHATLIST",
                        "message", ChatRoomListResponseDto.guest(guestMessageRequestDto, unreadCount, userChatRoom.getChatRoom())
                ));
        // FCM 알림 전송
        String fcmToken = userService.findFcmTokenByUserId(userChatRoom.getUser().getId());
        String body = "비회원 : " + chatRoomMessage.getMsg();
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

    public void messageFromInquiry(ChatMessageRequestDto chatMessageRequestDto){
            Apt apt = aptService.findByAptId(chatMessageRequestDto.getAptId());
            User user = apt.getUser();
            User me = userService.findByUserId(chatMessageRequestDto.getWriterId());
            // 방이 존재하는 경우
            List<ChatRoom> chatRooms = userChatRoomService.findChatRoomsByUserId(chatMessageRequestDto.getWriterId());
            if(chatRooms != null){
                // 방이 존재할 가능성 여부 확인
                UserChatRoom userChatRoom = userChatRoomService.findByUserAndChatRoom(chatRooms, user);
                if(userChatRoom != null){
                    // 방이 존재하는 경우 -> roomId 넣어주기
                    chatMessageRequestDto.addRoomId(userChatRoom.getChatRoom().getId());
                }
            }
            if(chatMessageRequestDto.getRoomId() == null ){
                // 방을 생성해야하는 경우
                UserChatRoom createdChatRoom = chatService.createChatRoom(ChatRoomDto.createChatRoomDto(chatMessageRequestDto, user), ChatRoomDto.createChatRoomDto(chatMessageRequestDto, me));
                chatMessageRequestDto.addRoomId(createdChatRoom.getChatRoom().getId());
            }
            System.out.println("채팅방으로 이동해도 된다는 메시지 전달 완료!!");
    }

    public ResponseEntity<Void> sendSocketChatListAndFcmToUser(ChatMessageRequestDto chatMessageRequestDto){
        AtomicBoolean isInquiry = new AtomicBoolean(false);
        if(chatMessageRequestDto.getRoomId() == null && chatMessageRequestDto.getAptId() != null){
            messageFromInquiry(chatMessageRequestDto);
            isInquiry.set(true);
        }
        // TODO
        // 채팅방에 나 빼고 존재하는 유저들
        List<Long> receiverIds = findReceiverId(chatMessageRequestDto);
        // 현재 채팅방에 입장한 유저들 (나빼고)
        List<Long> userIdsInRoom = roomUserCountService.getUserIdsInChatRoom(chatMessageRequestDto.getRoomId(), chatMessageRequestDto.getWriterId());
        // 해당 채팅방
        ChatRoom chatRoom = chatService.findChatRoomByRoomId(chatMessageRequestDto.getRoomId());
        String senderName = userService.findByUserId(chatMessageRequestDto.getWriterId()).getName(); // 1:1인 경우 채팅방 이름

        ChatRoomMessage chatRoomMessage =  saveChatMessage(SaveMessageDto.from(chatMessageRequestDto));
        System.out.println("채팅 메시지 저장함");
//        return chatMessageMono.flatMap(message -> {
            if(!chatRoom.getIsGroup()) { // 1:1 채팅방
                UserChatRoom userChatRoom = getUserInChatRoom(receiverIds.get(0), chatMessageRequestDto.getRoomId());
                if(!userChatRoom.getIsInRoom()) { // 방을 나갔으면 다시 방에 들어오게 되는 로직
                    saveReEntryUserInChatRoom(userChatRoom);
                    chatService.increaseNumberInChatRoom(chatMessageRequestDto.getRoomId());
                }
            }
            int countInRoom = roomUserCountService.getUserCount(chatMessageRequestDto.getRoomId());
            int unreadCountByMsgId = chatService.findChatRoomByRoomId(chatMessageRequestDto.getRoomId()).getMemberNum() - countInRoom;
            // [ 현재 채팅방에 접속한 경우 필요한 데이터 실시간 전달 ]
            if(isInquiry.get()){
                String userName = userService.findByUserId(receiverIds.get(0)).getName();
                // TODO : 매물 문의한 다음 채팅 페이지 이동 시 채팅 내역을 소켓 메시지로 전달받지 못하는 이슈 -> MessageInquiryDto를 만들어서 해당 문의 메시지는 미리 전달
                template.convertAndSend("/topic/user/" + chatMessageRequestDto.getWriterId(),
                        Map.of(
                                "type", "CLEAR_ROOM",
                                "message", CreatedChatRoomFromAptDto.from(chatMessageRequestDto.getRoomId(), userName)
                        )
                );
            }
            if(countInRoom > 0){
                template.convertAndSend("/topic/chatroom/" + chatMessageRequestDto.getRoomId(),
                        Map.of(
                                "type", "CHAT",
                                "message", chatRoom.getIsGroup() ? ChatMessageResponseToChatRoomDto.from(chatRoomMessage, null, chatMessageRequestDto, unreadCountByMsgId, MessageType.TEXT)
                                        // 개인 매물 문의 시 채팅 방안에 사람이 있는 경우 (상대방이 있는것) -> 안읽은 메시지 표시 x
                                        : isInquiry.get() ? ChatMessageResponseToChatRoomDto.from(chatRoomMessage, senderName, chatMessageRequestDto, unreadCountByMsgId -1 , MessageType.TEXT)
                                        : ChatMessageResponseToChatRoomDto.from(chatRoomMessage, senderName, chatMessageRequestDto, unreadCountByMsgId, MessageType.TEXT)
                        )
                );
            }
        // 채팅방 인원 중 현재 채팅방에 들어와있지 않은 유저들
        receiverIds.removeIf(userId -> userIdsInRoom.contains(userId));
        receiverIds.forEach(userId -> {
            // Redis에 해당 메시지 중 안읽은 사람의 id 저장
            messageUnreadService.addUnreadChat(chatMessageRequestDto.getRoomId().toString(), userId.toString(), chatRoomMessage.getId().toString());
            // 현재 채팅방에 없는 사람들을 기준으로 확인
            Long unreadCount = messageUnreadService.getUnreadMessageCount(chatMessageRequestDto.getRoomId().toString(), userId.toString());
            template.convertAndSend(
                    "/topic/user/" + userId,
                    Map.of(
                            "type", "CHATLIST",
                            "message", chatRoom.getIsGroup() ? ChatRoomListResponseDto.from(chatMessageRequestDto, null, unreadCount, chatRoom.getMemberNum())
                                    : ChatRoomListResponseDto.from(chatMessageRequestDto, senderName, unreadCount, chatRoom.getMemberNum())
                    ));
            // FCM 알림 전송
            String fcmToken = userService.findFcmTokenByUserId(userId);
            String body = userService.findNameByUserId(chatMessageRequestDto.getWriterId()) + " : " + chatMessageRequestDto.getMsg();
            if (roomUserCountService.getUserCount(chatMessageRequestDto.getRoomId()) < 2 && fcmToken != null) {
                try {
                    fcmService.sendMessageTo(
                            chatRoom.getIsGroup() ? FcmDto.chat(fcmToken, body, chatMessageRequestDto.getRoomId().toString(), chatRoom.getName())
                                    : FcmDto.chat(fcmToken, body, chatMessageRequestDto.getRoomId().toString(), senderName));
                    System.out.println("fcm 알림 전송 완료!");
                } catch (IOException e) {
                    System.err.println("FCM 전송 실패: " + e.getMessage());
                }
            }
        });
        return ResponseEntity.ok().build();
    }
}
