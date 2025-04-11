package com.ddhouse.chat.controller;

import com.ddhouse.chat.dto.request.ChatMessageRequestDto;
import com.ddhouse.chat.dto.request.ChatRoomUpdateDto;
import com.ddhouse.chat.dto.response.ChatMessageResponseDto;
import com.ddhouse.chat.fcm.service.FcmService;
import com.ddhouse.chat.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chatmsg")
public class ChatMessageController {
    private final ChatMessageService chatMessageService;
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
        Long receiverId = chatMessageService.findReceiverId(chatMessageRequestDto);
        int countInRoom = roomUserCountService.getUserCount(chatMessageRequestDto.getRoomId());
        String roomName = chatService.findRoomName(chatMessageRequestDto.getRoomId());
        /*
        * 3. 채팅 리스트에 들어가는 경우
        *   3-1. 각 방의 라스트 채팅 내용 불러오면서 redis에 각 방의 안읽은 채팅 개수들 같이 넣어서 보내주기
        * 4. 채팅방으로 이동이 가능한 페이지에 있는 경우
        *   4-1. redis에서 unread에 메시지가 한개 이상이라도 있다면 알림처리된 아이콘으로 변경할 수 있도록 api 내용 보내주기
        * */
        if(countInRoom < 2){
            messageUnreadService.addUnreadChat(chatMessageRequestDto.getRoomId().toString(), receiverId.toString(), chatMessageRequestDto.getMsg());
        }

        return chatMessageService.saveChatMessage(chatMessageRequestDto).flatMap(message -> {
            // 메시지를 해당 채팅방 구독자들에게 전송
            Map<String, Object> chatMessage = Map.of(
                    "type", "CHAT",
                    "message", ChatMessageResponseDto.fromAddCount(message, countInRoom)
            );
            template.convertAndSend("/topic/chatroom/" + chatMessageRequestDto.getRoomId(), chatMessage);
            // 상대방이 채팅방 목록을 보고 있다면, 실시간으로 목록 갱신 알림 전송 + 안읽은 메시지 수 같이 보내기
            Long unreadCount = messageUnreadService.getUnreadMessageCount(chatMessageRequestDto.getRoomId().toString(), receiverId.toString());
            System.out.println("보내는 대상: /topic/chatlist/" + receiverId);
            template.convertAndSend("/topic/chatlist/" + receiverId,
                    ChatRoomUpdateDto.from(chatMessageRequestDto, unreadCount));
            // TODO : 채팅방 구독자들에게 전송하는 메시지 타입 형태 일치시키기
            System.out.println("전송되는 메시지: " + ChatMessageResponseDto.from(message).getMsg());  // 확인용
            // fcm 알림 전송
            String fcmToken = userService.findFcmTokenByUserId(receiverId);
            String title = "새 메시지 도착!";
            // 내가 해당 방에 있는 경우 알림 처리 하지 않음
            if (roomUserCountService.getUserCount(chatMessageRequestDto.getRoomId()) < 2 && fcmToken != null) {
                try {
                    fcmService.sendMessageTo(
                            fcmToken,
                            title,
                            userService.findNameByUserId(chatMessageRequestDto.getWriterId()) + " : " + chatMessageRequestDto.getMsg(),
                            chatMessageRequestDto.getRoomId().toString(),
                            roomName
                    );
                } catch (IOException e) {
                    return Mono.error(new RuntimeException(e));
                }
            }
            return Mono.just(ResponseEntity.ok().build());
        });
    }


    @GetMapping("/find/list/{chatRoomId}")
    public Flux<ResponseEntity<List<ChatMessageResponseDto>>> findMessageByChatRoomId(@PathVariable("chatRoomId") Long id) {
        System.out.println("채팅방 리스트 확인하기");
        return chatMessageService.findChatMessages(id)
                .map(messages -> {
                    System.out.println("해당 채팅방의 메시지들 가져오기 결과 : " + messages);
                    return ResponseEntity.ok(messages);
                })
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/apt/find/list/{aptId}")
    // CHECK : 프론트에서 임시로 myId 받아와서 확인 (병합시 토큰으로 처리)
    public Flux<ResponseEntity<List<ChatMessageResponseDto>>> findMessageByAptId(@PathVariable("aptId") Long id, @RequestParam("myId") Long myId) {
        System.out.println("매물id로 채팅 내역 불러오기");
        return chatMessageService.getChatRoomByAptIdAndUserId(id, myId)
                .map(messages -> {
                    System.out.println("해당 채팅방의 메시지들 가져오기 결과 : " + messages);
                    return ResponseEntity.ok(messages);
                })
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
