package com.ddhouse.chat.controller;

import com.ddhouse.chat.dto.request.message.ChatMessageRequestDto;
import com.ddhouse.chat.dto.request.message.GuestMessageRequestDto;
import com.ddhouse.chat.dto.response.chatRoom.ChatRoomListResponseDto;
import com.ddhouse.chat.dto.response.message.ChatMessageResponseDto;
import com.ddhouse.chat.service.*;
import com.ddhouse.chat.vo.ChatRoomMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chatmsg")
public class ChatMessageController {
    private final ChatMessageService chatMessageService;
    private final ChatRoomMessageService chatRoomMessageService;
    private final RoomUserCountService roomUserCountService;
    private final SimpMessageSendingOperations template;


    //메세지 송신 및 수신
    @MessageMapping("/message")
    public void receiveMessage(@Payload ChatMessageRequestDto chatMessageRequestDto) {
        // 채팅방, 유저, 알림 소켓 메시지로 전달 및 메시지 처리 과정
        chatMessageService.sendSocketChatListAndFcmToUser(chatMessageRequestDto);
    }


    @GetMapping("/find/list/{chatRoomId}")
    public ResponseEntity<List<ChatMessageResponseDto>> findMessageByChatRoomId(@PathVariable("chatRoomId") Long roomId, @RequestParam("myId") Long myId) {
        System.out.println("채팅방 채팅 내역 확인하기");
        List<ChatMessageResponseDto> messages = chatMessageService.findChatMessages(roomId, myId);
        return ResponseEntity.ok(messages);
    }

    @DeleteMapping("/delete/me/{msgId}")
    public ResponseEntity<Void> deleteChatMessageMe(@PathVariable("msgId") Long msgId, @RequestParam("myId") Long myId){
        chatRoomMessageService.deleteChatMessageOnlyMe(msgId, myId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/delete/all/{msgId}")
    public ResponseEntity<Void> deleteChatMessageAll(@PathVariable("msgId") Long msgId, @RequestParam("myId") Long myId){
        Long roomIdToDeleteMsg = chatRoomMessageService.deleteChatMessageAll(msgId, myId);
        // 전체 삭제 시 해당 메시지 실시간 채팅방에서 삭제 처리
        Map<String, Object> deleteMessage = Map.of(
                "type", "DELETE",
                "messageId", msgId
        );
        template.convertAndSend("/topic/chatroom/" + roomIdToDeleteMsg, deleteMessage);
        // TODO : 채팅리스트에서도 전달할 수 있도록!!!
        /*
        1. roomId가 존재 -> 해당 방에 존재하는 모든 유저의 id를 받아오기
            1-2.  List<Long> receiverIds = findReceiverId(chatMessageRequestDto);
        2. 현재 채팅방에 입장한 유저들
        3. 채팅방 인원 중 현재 채팅방에 들어와있지 않은 유저들
        4. -> 패팅방에 없는 사람들을 기준으로 CHATLIST 전달
        */
        List<Long> receiverIds = chatMessageService.findReceiverId(roomIdToDeleteMsg, myId);
        List<Long> userIdsInRoom = roomUserCountService.getUserIdsInChatRoom(roomIdToDeleteMsg, myId);
        receiverIds.removeIf(userId -> userIdsInRoom.contains(userId));
        receiverIds.forEach(userId -> {
            template.convertAndSend(
                    "/topic/user/" + userId,
                    Map.of(
                            "type", "CHATLIST",
                            "message", ChatRoomListResponseDto.delete(roomIdToDeleteMsg)
                    ));
        });

        return ResponseEntity.ok().build();
    }

    @PostMapping("/send/guest")
    public ResponseEntity<Void> sendNoteNonMember(@RequestBody GuestMessageRequestDto guestMessageRequestDto) {
        // 비회원 유저가 문의 남기는 경우
        ChatRoomMessage message = chatMessageService.sendMessageGuest(guestMessageRequestDto);
        if (message != null) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
