package com.ddhouse.chat.controller;

import com.ddhouse.chat.dto.ChatRoomDto;
import com.ddhouse.chat.service.ChatService;
import com.ddhouse.chat.service.MessageUnreadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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
    public ResponseEntity<List<ChatRoomDto>> getMyChatRoomList(@RequestParam("myId") Long myId) {
        List<ChatRoomDto> responses = chatService.findMyChatRoomList(myId);
        for (ChatRoomDto chatRoom : responses) {
//            Tuple2<String, LocalDateTime> lastMessageData = chatService.getLastMessage(chatRoom.getId()).block();
            Tuple3<String, LocalDateTime, Long> lastMessageData = chatService.getLastMessageWithUnreadCount(chatRoom.getId(), myId).block();
            chatRoom.setLastMsg(lastMessageData.getT1());
            chatRoom.setUpdateLastMsgTime(lastMessageData.getT2());
            chatRoom.setUnreadCount(lastMessageData.getT3());
        }
        return ResponseEntity.ok().body(responses);
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
