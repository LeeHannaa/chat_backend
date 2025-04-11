package com.ddhouse.chat.controller;

import com.ddhouse.chat.dto.ChatRoomDto;
import com.ddhouse.chat.service.ChatService;
import com.ddhouse.chat.service.MessageUnreadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuple3;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chat")
public class ChatController {
    private final ChatService chatService;
    private final MessageUnreadService messageUnreadService;

    @PostMapping("/create")
    public ResponseEntity<Void> createChatRoom(@RequestBody ChatRoomDto chatRoomDto) {
        chatService.createChatRoom(chatRoomDto);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public Mono<ResponseEntity<List<ChatRoomDto>>> getMyChatRoomList(@RequestParam("myId") Long myId) {
        List<ChatRoomDto> responses = chatService.findMyChatRoomList(myId);
        return Flux.fromIterable(responses)
                .flatMap(chatRoomDto ->
                        chatService.getLastMessageWithUnreadCount(chatRoomDto.getId(), myId)
                                .map(tuple -> {
                                    chatRoomDto.setLastMsg(tuple.getT1());
                                    chatRoomDto.setUpdateLastMsgTime(tuple.getT2());
                                    chatRoomDto.setUnreadCount(tuple.getT3());
                                    return chatRoomDto;
                                })
                )
                .collectList()
                .map(ResponseEntity::ok);
    }

    @DeleteMapping("/delete/{chatRoomId}")
    public ResponseEntity<Void> deleteChatRoom(@PathVariable("chatRoomId") Long id){
        chatService.deleteChatRoom(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/unread/count")
    public ResponseEntity<Long> getUnreadCountByRoom(@RequestParam("roomId") Long roomId){
        Long unreadCount = messageUnreadService.getOtherUserUnreadCount(roomId.toString());
        return ResponseEntity.ok().body(unreadCount);
    }
}
