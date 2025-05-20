package com.ddhouse.chat.controller;

import com.ddhouse.chat.domain.ChatRoom;
import com.ddhouse.chat.domain.MessageType;
import com.ddhouse.chat.domain.UserChatRoom;
import com.ddhouse.chat.dto.FcmDto;
import com.ddhouse.chat.dto.request.ChatMessageRequestDto;
import com.ddhouse.chat.dto.request.ChatRoomListUpdateDto;
import com.ddhouse.chat.dto.request.GuestMessageRequestDto;
import com.ddhouse.chat.dto.response.ChatMessage.ChatMessageResponseDto;
import com.ddhouse.chat.dto.response.ChatMessage.ChatMessageResponseToChatRoomDto;
import com.ddhouse.chat.fcm.service.FcmService;
import com.ddhouse.chat.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chatmsg")
public class ChatMessageController {
    private final ChatMessageService chatMessageService;
    private final ChatRoomMessageService chatRoomMessageService;

    private final UserService userService;
    private final FcmService fcmService;
    private final RoomUserCountService roomUserCountService;
    private final MessageUnreadService messageUnreadService;
    private final ChatService chatService;


    private final SimpMessageSendingOperations template;


    //메세지 송신 및 수신
    @MessageMapping("/message")
    public Mono<ResponseEntity<Void>> receiveMessage(@Payload ChatMessageRequestDto chatMessageRequestDto) {
        // 채팅방에 나 빼고 존재하는 유저들
        List<Long> receiverIds = chatMessageService.findReceiverId(chatMessageRequestDto);
        // 현재 채팅방에 입장한 유저들 (나빼고)
        List<Long> userIdsInRoom = roomUserCountService.getUserIdsInChatRoom(chatMessageRequestDto.getRoomId(), chatMessageRequestDto.getWriterId());
        // 해당 채팅방
        ChatRoom chatRoom = chatService.findChatRoomByRoomId(chatMessageRequestDto.getRoomId());

        return chatMessageService.saveChatMessage(chatMessageRequestDto).flatMap(message -> {
            if(!chatRoom.getIsGroup()){ // 1:1 채팅방
                UserChatRoom userChatRoom = chatMessageService.getUserInChatRoom(receiverIds.get(0), chatMessageRequestDto.getRoomId());
                if(!userChatRoom.getIsInRoom()){ // 방을 나갔으면 다시 방에 들어오게 되는 로직
                    chatMessageService.saveReEntryUserInChatRoom(userChatRoom);
                    chatService.increaseNumberInChatRoom(chatMessageRequestDto.getRoomId());
                }
            }
            int countInRoom = roomUserCountService.getUserCount(chatMessageRequestDto.getRoomId());
            int unreadCountByMsgId = chatService.findChatRoomByRoomId(chatMessageRequestDto.getRoomId()).getMemberNum() - countInRoom;
            // [ 현재 채팅방에 접속한 경우 필요한 데이터 실시간 전달 ]
            template.convertAndSend("/topic/chatroom/" + chatMessageRequestDto.getRoomId(),
                    Map.of(
                            "type", "CHAT",
                            "message", ChatMessageResponseToChatRoomDto.from(message, chatMessageRequestDto, unreadCountByMsgId, MessageType.TEXT)
                    )
            );
            // 채팅방 인원 중 현재 채팅방에 들어와있지 않은 유저들
            receiverIds.removeIf(userId -> userIdsInRoom.contains(userId));
            receiverIds.forEach(userId -> {
                // Redis에 해당 메시지 중 안읽은 사람의 id 저장
                messageUnreadService.addUnreadChat(chatMessageRequestDto.getRoomId().toString(), userId.toString(), message.getId());
                // 현재 채팅방에 없는 사람들을 기준으로 확인
                Long unreadCount = messageUnreadService.getUnreadMessageCount(chatMessageRequestDto.getRoomId().toString(), userId.toString());
                template.convertAndSend(
                        "/topic/user/" + userId,
                        Map.of(
                                "type", "CHATLIST",
                                "message", ChatRoomListUpdateDto.from(chatMessageRequestDto, unreadCount, chatRoom.getMemberNum())
                        ));
                // FCM 알림 전송
                String fcmToken = userService.findFcmTokenByUserId(userId);
                String body = userService.findNameByUserId(chatMessageRequestDto.getWriterId()) + " : " + chatMessageRequestDto.getMsg();
                if (roomUserCountService.getUserCount(chatMessageRequestDto.getRoomId()) < 2 && fcmToken != null) {
                    try {
                        fcmService.sendMessageTo(
                                FcmDto.chat(fcmToken, body, chatMessageRequestDto.getRoomId().toString(), chatRoom.getName()));
                        System.out.println("fcm 알림 전송 완료!");
                    } catch (IOException e) {
                        System.err.println("FCM 전송 실패: " + e.getMessage());
                    }
                }
            });
            return Mono.just(ResponseEntity.ok().build());
        });
    }


    @GetMapping("/find/list/{chatRoomId}")
    public Mono<ResponseEntity<List<ChatMessageResponseDto>>> findMessageByChatRoomId(@PathVariable("chatRoomId") Long roomId, @RequestParam("myId") Long myId) {
        System.out.println("채팅방 채팅 내역 확인하기");
        return chatMessageService.findChatMessages(roomId, myId)
                .map(messages -> {
                    System.out.println("해당 채팅방의 메시지들 가져오기 결과 : " + messages);
                    return ResponseEntity.ok(messages);
                })
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/apt/find/list/{aptId}")
    // CHECK : 프론트에서 임시로 myId 받아와서 확인 (병합시 토큰으로 처리)
    public Mono<ResponseEntity<List<ChatMessageResponseDto>>> findMessageByAptId(@PathVariable("aptId") Long roomId, @RequestParam("myId") Long myId) {
        System.out.println("매물id로 채팅 내역 불러오기");
        return chatMessageService.getChatRoomByAptIdAndUserId(roomId, myId)
                .map(messages -> {
                    System.out.println("해당 채팅방의 메시지들 가져오기 결과 : " + messages);
                    return ResponseEntity.ok(messages);
                })
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/delete/me/{msgId}")
    public ResponseEntity<Void> deleteChatMessageMe(@PathVariable("msgId") UUID msgId, @RequestParam("myId") Long myId){
        chatRoomMessageService.deleteChatMessageOnlyMe(msgId, myId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/delete/all/{msgId}")
    public ResponseEntity<Void> deleteChatMessageAll(@PathVariable("msgId") UUID msgId, @RequestParam("myId") Long myId){
        Long roomIdToDeleteMsg = chatRoomMessageService.deleteChatMessageAll(msgId, myId);
        // 전체 삭제 시 해당 메시지 실시간 채팅방에서 삭제 처리
        Map<String, Object> deleteMessage = Map.of(
                "type", "DELETE",
                "messageId", msgId.toString()
        );
        template.convertAndSend("/topic/chatroom/" + roomIdToDeleteMsg, deleteMessage);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/send/guest")
    public ResponseEntity<Void> sendNoteNonMember(@RequestBody GuestMessageRequestDto guestMessageRequestDto) {
        // 비회원 유저가 쪽지 문의 남기는 경우
        chatMessageService.sendMessageGuest(guestMessageRequestDto);
        return ResponseEntity.ok().build();
    }
}
