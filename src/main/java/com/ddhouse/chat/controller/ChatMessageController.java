package com.ddhouse.chat.controller;

import com.ddhouse.chat.dto.ChatMessageDto;
import com.ddhouse.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chat")
public class ChatMessageController {
    private final ChatService chatService;

    private final SimpMessageSendingOperations template;

    //메세지 송신 및 수신
    @MessageMapping("/message")
    public Mono<ResponseEntity<Void>> receiveMessage(@RequestBody ChatMessageDto chat) {
        return chatService.saveChatMessage(chat).flatMap(message -> {
            // 메시지를 해당 채팅방 구독자들에게 전송
            template.convertAndSend("/sub/chatroom/" + chat.getRoomId(),
                    ChatMessageDto.from(message));
            return Mono.just(ResponseEntity.ok().build());
        });
    }

    @GetMapping("/find/list/{id}")
    // TODO : 사용자 이름 같이 넘기기
    public Flux<ResponseEntity<List<ChatMessageDto>>> find(@PathVariable("id") Long id) {
        System.out.println("채팅방 리스트 확인하기");
        return chatService.findChatMessages(id)
                .map(messages -> {
                    System.out.println("해당 채팅방의 메시지들 가져오기 결과 : " + messages);
                    return ResponseEntity.ok(messages);
                })
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
