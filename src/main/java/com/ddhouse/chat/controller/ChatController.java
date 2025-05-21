package com.ddhouse.chat.controller;

import com.ddhouse.chat.domain.ChatRoom;
import com.ddhouse.chat.domain.UserChatRoom;
import com.ddhouse.chat.dto.ChatRoomDto;
import com.ddhouse.chat.dto.request.group.GroupChatRoomCreateDto;
import com.ddhouse.chat.dto.request.group.InviteGroupRequestDto;
import com.ddhouse.chat.dto.response.chatRoom.ChatRoomListResponseDto;
import com.ddhouse.chat.service.ChatService;
import com.ddhouse.chat.service.MessageUnreadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chat")
public class ChatController {
    private final ChatService chatService;
    private final MessageUnreadService messageUnreadService;

    @PostMapping("/create") // 직접적으로 사용하지 않음
    public ResponseEntity<Void> createChatRoom(@RequestBody ChatRoomDto chatRoomDto) {
        chatService.createChatRoom(chatRoomDto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/create/group") // 단체 채팅방 생성
    // TODO : 단체 채팅방 만들기 객체 설정해서 진행하면 됨!!!!
    public ResponseEntity<ChatRoom> createGroupChatRoom(@RequestBody GroupChatRoomCreateDto groupChatRoomCreateDto) {
        ChatRoom newChatRoom = chatService.createGroupChatRoom(groupChatRoomCreateDto);
        return ResponseEntity.ok().body(newChatRoom);
    }

    @PostMapping("/invite/user/group")
    // TODO : userId, roomId 이렇게 받아오기
    public ResponseEntity<UserChatRoom> inviteUserGroupChatRoom(@RequestBody InviteGroupRequestDto inviteGroupRequestDto) {
        UserChatRoom userChatRoom = chatService.inviteGroupChatRoom(inviteGroupRequestDto);
        return ResponseEntity.ok().body(userChatRoom);
    }


    @GetMapping
    public Mono<ResponseEntity<List<ChatRoomListResponseDto>>> getMyChatRoomList(@RequestParam("myId") Long myId) {
        List<ChatRoomListResponseDto> responses = chatService.findMyChatRoomList(myId);
        if (responses.isEmpty()) {
            System.out.println("현재 내가 들어가있는 채팅방 없음!!!!");
            return Mono.just(ResponseEntity.ok(Collections.emptyList()));
        }
        return Flux.fromIterable(responses)
                .flatMap(chatRoomDto ->
                        chatService.getLastMessageWithUnreadCount(chatRoomDto.getRoomId(), myId)
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

    @GetMapping("/unread/count/{chatRoomId}")
    public ResponseEntity<Long> getUnreadCountByRoom(@PathVariable("chatRoomId") Long roomId, @RequestParam("myId") Long myId){
        Long unreadCount = messageUnreadService.getUnreadMessageCount(roomId.toString(), myId.toString());
        return ResponseEntity.ok().body(unreadCount);
    }

    @DeleteMapping("/delete/{chatRoomId}")
    public ResponseEntity<Void> deleteChatRoom(@PathVariable("chatRoomId") Long roomId, @RequestParam("myId") Long myId){
        chatService.deleteChatRoom(roomId, myId);
        return ResponseEntity.ok().build();
    }

}
