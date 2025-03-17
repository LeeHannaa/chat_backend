package com.ddhouse.chat.controller;

import com.ddhouse.chat.dto.ChatMessageDto;
import com.ddhouse.chat.service.ChatMessageService;
import com.ddhouse.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chatmsg")
public class ChatMessageController {
    private final ChatMessageService chatMessageService;

    private final SimpMessageSendingOperations template;

    //메세지 송신 및 수신
    @MessageMapping("/message")
    public Mono<ResponseEntity<Void>> receiveMessage(@Payload ChatMessageDto chat) {
        System.out.println("메시지 수신 : " + chat.getMsg());
        return chatMessageService.saveChatMessage(chat).flatMap(message -> {
            // 메시지를 해당 채팅방 구독자들에게 전송
            template.convertAndSend("/sub/chatroom/" + chat.getRoomId(),
                    ChatMessageDto.from(message));
            return Mono.just(ResponseEntity.ok().build());
        });
    }

    @MessageMapping("/greeting")
    public String handle(String greeting) {
        return greeting;
    }



    @GetMapping("/find/list/{chatRoomId}")
    public Flux<ResponseEntity<List<ChatMessageDto>>> findMessageByChatRoomId(@PathVariable("chatRoomId") Long id) {
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
    public Flux<ResponseEntity<List<ChatMessageDto>>> findMessageByAptId(@PathVariable("aptId") Long id, @RequestParam("myId") Long myId) {
        System.out.println("매물id로 채팅 내역 불러오기");
        return chatMessageService.getChatRoomByAptIdAndUserId(id, myId)
                .map(messages -> {
                    System.out.println("해당 채팅방의 메시지들 가져오기 결과 : " + messages);
                    return ResponseEntity.ok(messages);
                })
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
