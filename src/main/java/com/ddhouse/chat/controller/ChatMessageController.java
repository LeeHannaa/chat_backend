package com.ddhouse.chat.controller;

import com.ddhouse.chat.domain.ChatMessage;
import com.ddhouse.chat.domain.UserChatRoom;
import com.ddhouse.chat.dto.request.ChatMessageRequestDto;
import com.ddhouse.chat.dto.request.ChatRoomUpdateDto;
import com.ddhouse.chat.dto.response.ChatMessageResponseDto;
import com.ddhouse.chat.fcm.service.FcmService;
import com.ddhouse.chat.service.ChatMessageService;
import com.ddhouse.chat.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chatmsg")
public class ChatMessageController {
    private final ChatMessageService chatMessageService;
    private final UserService userService;
    private final FcmService fcmService;

    private final SimpMessageSendingOperations template;


    //메세지 송신 및 수신
    @MessageMapping("/message")
    public Mono<ResponseEntity<Void>> receiveMessage(@Payload ChatMessageRequestDto chatMessageRequestDto) {
        System.out.println("메시지 수신 : " + chatMessageRequestDto.getMsg());
        Long receiverId = chatMessageService.findReceiverId(chatMessageRequestDto);
        return chatMessageService.saveChatMessage(chatMessageRequestDto).flatMap(message -> {
            // 메시지를 해당 채팅방 구독자들에게 전송
            template.convertAndSend("/topic/chatroom/" + chatMessageRequestDto.getRoomId(),
                    ChatMessageResponseDto.from(message));
            // fcm 알림 전송
            String fcmToken = userService.findFcmTokenByUserId(receiverId);
            String title = "새 메시지 도착!";
            try {
                fcmService.sendMessageTo(
                        fcmToken,
                        title,
                        userService.findNameByUserId(chatMessageRequestDto.getWriterId()) + " : " + chatMessageRequestDto.getMsg());
            } catch (IOException e) {
                return Mono.error(new RuntimeException(e));
            }
            // 상대방이 채팅방 목록을 보고 있다면, 실시간으로 목록 갱신 알림 전송
            template.convertAndSend("/topic/chatlist/" + receiverId,
                    ChatRoomUpdateDto.from(chatMessageRequestDto));
            // TODO : 채팅방 구독자들에게 전송하는 메시지 타입 형태 일치시키기
            System.out.println("전송되는 메시지: " + ChatMessageResponseDto.from(message).getMsg());  // 확인용
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
