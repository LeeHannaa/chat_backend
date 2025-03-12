package com.ddhouse.chat.controller;

import com.ddhouse.chat.dto.ChatMessageDto;
import com.ddhouse.chat.service.ChatMessageService;
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
@RequestMapping("/chatmsg")
public class ChatMessageController {
    private final ChatMessageService chatMessageService;

    private final SimpMessageSendingOperations template;

    //메세지 송신 및 수신
    @MessageMapping("/message")
    public Mono<ResponseEntity<Void>> receiveMessage(@RequestBody ChatMessageDto chat) {
        return chatMessageService.saveChatMessage(chat).flatMap(message -> {
            // 메시지를 해당 채팅방 구독자들에게 전송
            template.convertAndSend("/sub/chatroom/" + chat.getRoomId(),
                    ChatMessageDto.from(message));
            return Mono.just(ResponseEntity.ok().build());
        });
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
    // TODO : 프론트에서 나의 id 받아와서 확인하기
    public Flux<ResponseEntity<List<ChatMessageDto>>> findMessageByAptId(@PathVariable("aptId") Long id) {
        /*
        1. aptId로 chatRoom에 동일한 aptId가 있는지 확인
            1-1. 만약 동일한 id가 있는데 해당 aptId에 userId가 나의 id와 동일하다면 (나와의 채팅)
            1-2. 동일한 id가 있다면 해당 chatRoom에서 userId가 나의 id와 동일한 chatRoomId를 불러오기
        2. chatRoomId를 받아왔으면 해당 id로 메시지 불러와서 전달
        3. 일치하는 chatRoomId가 없다면 새로운 방 생성
        */
        System.out.println("매물id로 채팅 내역 불러오기");
        return chatMessageService.getChatRoomByAptIdAndUserId(id)
                .map(messages -> {
                    System.out.println("해당 채팅방의 메시지들 가져오기 결과 : " + messages);
                    return ResponseEntity.ok(messages);
                })
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
