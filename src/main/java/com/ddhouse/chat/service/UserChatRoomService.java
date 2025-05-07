package com.ddhouse.chat.service;

import com.ddhouse.chat.domain.*;
import com.ddhouse.chat.dto.request.UserChatRoomAddDto;
import com.ddhouse.chat.dto.response.ChatMessage.ChatMessageResponseToChatRoomDto;
import com.ddhouse.chat.exception.NotFoundException;
import com.ddhouse.chat.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserChatRoomService {
    private final UserChatRoomRepository userChatRoomRepository;
    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatService chatService;
    private final SimpMessageSendingOperations template;
    private final ChatRoomMessageRepository chatRoomMessageRepository;


    public void addUsersInChatRoom(UserChatRoomAddDto userChatRoomAddDto){
        /*
        1. SYSTEM 메시지로 초대 메시지 내역 저장
        2. 실시간으로 어떤 유저들이 초대되었는지 전달되어야 함.
        */
        ChatRoom chatRoom = chatService.findChatRoomByRoomId(userChatRoomAddDto.getChatRoomId());
        // 채팅방이 아니었던 경우 채팅방으로 변경
        if(!chatRoom.getIsGroup()){
            chatRoom.updateGroup(Boolean.TRUE);
        }
        String inviteMsg = "";
        List<UserChatRoom> userChatRooms = Arrays.stream(userChatRoomAddDto.getUserIds().split(","))
                .filter(s -> !s.isBlank())
                .map(Long::valueOf)
                .map(userId -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new NotFoundException("해당 아이디를 가진 유저를 찾지 못했습니다."));
                    return UserChatRoom.group(chatRoom, user);
                })
                .collect(Collectors.toList());
        // 채팅방에 있는 유저 수 증가
        chatRoom.increaseMemberNums(userChatRooms.size());
        chatRoomRepository.save(chatRoom);
        // 채팅방과 유저 관계 추가
        userChatRoomRepository.saveAll(userChatRooms);


    }
}
