package com.ddhouse.chat.controller;

import com.ddhouse.chat.domain.ChatMessage;
import com.ddhouse.chat.domain.ChatRoom;
import com.ddhouse.chat.domain.MessageType;
import com.ddhouse.chat.domain.UserChatRoom;
import com.ddhouse.chat.dto.FcmDto;
import com.ddhouse.chat.dto.request.ChatMessageRequestDto;
import com.ddhouse.chat.dto.request.ChatRoomUpdateDto;
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
import java.util.ArrayList;
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
        System.out.println("메시지 수신 : " + chatMessageRequestDto.getMsg());
        // TODO G **: 현재 채팅방에 있는 모든 인원을 리시버 id로 저장 (List) 나 빼고
        // 채팅방에 나 빼고 존재하는 유저들
        List<Long> receiverIds = chatMessageService.findReceiverId(chatMessageRequestDto);
        // TODO G **: 현재 접속한 수만 가져오는게 아니라 접속한 사람의 userId(value값)도 가져오기 -> 채팅방 인원 id랑 비교 후 접속하지 않은 사용자에게 알림 처리 및 redis에 안읽은 사용자로 저장
        // 현재 채팅방에 입장한 유저들 (나빼고)
        List<Long> userIdsInRoom = roomUserCountService.getUserIdsInChatRoom(chatMessageRequestDto.getRoomId(), chatMessageRequestDto.getWriterId());
        // 해당 채팅방
        ChatRoom chatRoom = chatService.findChatRoomByRoomId(chatMessageRequestDto.getRoomId());

        /*
        * 3. 채팅 리스트에 들어가는 경우
        *   3-1. 각 방의 라스트 채팅 내용 불러오면서 redis에 각 방의 안읽은 채팅 개수들 같이 넣어서 보내주기
        * 4. 채팅방으로 이동이 가능한 페이지에 있는 경우
        *   4-1. redis에서 unread에 메시지가 한개 이상이라도 있다면 알림처리된 아이콘으로 변경할 수 있도록 api 내용 보내주기
        */
        // TODO G **: 한번 단체 채팅이면 영원한 단체 채팅
        return chatMessageService.saveChatMessage(chatMessageRequestDto).flatMap(message -> {
            /*
            1.  현재 채팅방이 단체 채팅방인지 1:1 채팅방인지 보고 상대가 나갔다면 바로 다시 보내는 로직 적용
            */
            // 메시지를 해당 채팅방 구독자들에게 전송
            if(!chatRoom.getIsGroup()){ // 1:1 채팅방
                UserChatRoom userChatRoom = chatMessageService.getUserInChatRoom(receiverIds.get(0), chatMessageRequestDto.getRoomId());
                if(!userChatRoom.getIsInRoom()){ // 방을 나갔으면 다시 방에 들어오게 되는 로직
                    System.out.println("방을 나간 유저에게 보내는 경우!!!");
                    // 1. isInRoom을 True로 변경
                    chatMessageService.saveReEntryUserInChatRoom(userChatRoom);
                    // 2. ChatRoom에 memberNum 증가
                    chatService.increaseNumberInChatRoom(chatMessageRequestDto.getRoomId());
                }
            } else{ // 단체 채팅방

            }
            // TODO G **: 해당 메시지를 읽지 않은 유저의 수를 반환하기
            int countInRoom = roomUserCountService.getUserCount(chatMessageRequestDto.getRoomId());
            int unreadCountByMsgId = chatService.findChatRoomByRoomId(chatMessageRequestDto.getRoomId()).getMemberNum() - countInRoom;

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
                        "/topic/chatlist/" + userId, ChatRoomUpdateDto.from(chatMessageRequestDto, unreadCount, chatRoom.getMemberNum()));
                // TODO G **: 단체 채팅방에 있는 유저들의 receiverId 각각 전송
                String fcmToken = userService.findFcmTokenByUserId(userId);
                String body = userService.findNameByUserId(chatMessageRequestDto.getWriterId()) + " : " + chatMessageRequestDto.getMsg();
                if (roomUserCountService.getUserCount(chatMessageRequestDto.getRoomId()) < 2 && fcmToken != null) {
                    try {
                        fcmService.sendMessageTo(
                                FcmDto.chat(fcmToken, body, chatMessageRequestDto.getRoomId().toString(), chatRoom.getName()));
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

        // 3. 해당 채팅방 topic으로 전송
        template.convertAndSend("/topic/chatroom/" + roomIdToDeleteMsg, deleteMessage);
        return ResponseEntity.ok().build();
    }
}
