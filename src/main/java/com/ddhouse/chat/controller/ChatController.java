package com.ddhouse.chat.controller;

import com.ddhouse.chat.dto.request.group.GroupChatRoomCreateDto;
import com.ddhouse.chat.dto.request.group.InviteGroupRequestDto;
import com.ddhouse.chat.dto.response.chatRoom.ChatRoomListResponseDto;
import com.ddhouse.chat.service.ChatService;
import com.ddhouse.chat.service.MessageUnreadService;
import com.ddhouse.chat.service.UserChatRoomService;
import com.ddhouse.chat.service.UserService;
import com.ddhouse.chat.vo.ChatRoom;
import com.ddhouse.chat.vo.User;
import com.ddhouse.chat.vo.UserChatRoom;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.util.function.Tuple3;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chat")
public class ChatController {
    private final ChatService chatService;
    private final MessageUnreadService messageUnreadService;
    private final UserChatRoomService userChatRoomService;
    private final UserService userService;

    @PostMapping("/create/group") // 단체 채팅방 생성
    public ResponseEntity<ChatRoom> createGroupChatRoom(@RequestBody GroupChatRoomCreateDto groupChatRoomCreateDto) {
        ChatRoom newChatRoom = chatService.createGroupChatRoom(groupChatRoomCreateDto);
        return ResponseEntity.ok().body(newChatRoom);
    }

    @PostMapping("/invite/user/group")
    public ResponseEntity<UserChatRoom> inviteUserGroupChatRoom(@RequestBody InviteGroupRequestDto inviteGroupRequestDto) {
        UserChatRoom userChatRoom = chatService.inviteGroupChatRoom(inviteGroupRequestDto);
        return ResponseEntity.ok().body(userChatRoom);
    }


    @GetMapping
    public ResponseEntity<List<ChatRoomListResponseDto>> getMyChatRoomList(@RequestParam("myId") Long myId) {
        List<ChatRoomListResponseDto> responses = chatService.findMyChatRoomList(myId);
        if (responses.isEmpty()) {
            System.out.println("현재 내가 들어가있는 채팅방 없음!!!!");
            return ResponseEntity.ok(Collections.emptyList());
        }
        List<ChatRoomListResponseDto> updatedResponses = responses.stream()
                .map(chatRoomDto -> {
                    Tuple3<String, LocalDateTime, Long> tuple = chatService.getLastMessageWithUnreadCount(chatRoomDto.getRoomId(), myId);
                    chatRoomDto.setLastMsg(tuple.getT1());
                    chatRoomDto.setUpdateLastMsgTime(tuple.getT2());
                    chatRoomDto.setUnreadCount(tuple.getT3());
                    return chatRoomDto;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(updatedResponses);
    }

    @GetMapping("/connect/{userId}")
    public ResponseEntity<Long> getRoomIdByConnectingPerson(@PathVariable("userId") Long userId, @RequestParam("myId") Long myId){
        // 상대에게 직접 연락하는 경우
        List<ChatRoom> chatRooms = userChatRoomService.findChatRoomsByUserId(myId);
        User user = userService.findByUserId(userId);
        UserChatRoom userChatRoom = userChatRoomService.findByUserAndChatRoom(chatRooms, user);
        if(userChatRoom != null) {
            return ResponseEntity.ok().body(userChatRoom.getChatRoom().getId());
        } else {
            // 기존 채팅방이 없는 경우 방생성 후 방아이디 전달
            UserChatRoom newUserChatRoom = chatService.createChatRoomByConnecting(userId, myId);
            return ResponseEntity.ok().body(newUserChatRoom.getChatRoom().getId());
        }
    }

    @GetMapping("/unread/count/{chatRoomId}")
    public ResponseEntity<Long> getUnreadCountByRoom(@PathVariable("chatRoomId") Long roomId, @RequestParam("myId") Long myId){
        // 채팅방 들어왔을 때 읽음 처리 해야하는 채팅 개수
        Long unreadCount = messageUnreadService.getUnreadMessageCount(roomId.toString(), myId.toString());
        return ResponseEntity.ok().body(unreadCount);
    }

    @DeleteMapping("/delete/{chatRoomId}")
    public ResponseEntity<Void> deleteChatRoom(@PathVariable("chatRoomId") Long roomId, @RequestParam("myId") Long myId){
        chatService.deleteChatRoom(roomId, myId);
        return ResponseEntity.ok().build();
    }

}
