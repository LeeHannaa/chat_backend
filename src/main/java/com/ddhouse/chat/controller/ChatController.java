package com.ddhouse.chat.controller;

import com.ddhouse.chat.dto.ChatRoomDto;
import com.ddhouse.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chat")
public class ChatController {
    private final ChatService chatService;

    @PostMapping("/create")
    public ResponseEntity<Void> createChatRoom(@RequestBody ChatRoomDto chatRoomDto) {
        chatService.createChatRoom(chatRoomDto);
        return ResponseEntity.ok().build();
    }
    @GetMapping
    public ResponseEntity<List<ChatRoomDto>> getMyChatRoomList(@RequestParam("myId") Long myId) {
        List<ChatRoomDto> responses = chatService.findMyChatRoomList(myId);
        for (ChatRoomDto chatRoom : responses) {
            String lastMessage = chatService.getLastMessage(chatRoom.getId()).block();
            chatRoom.setLastMsg(lastMessage);
        }
        return ResponseEntity.ok().body(responses);
    }
}
