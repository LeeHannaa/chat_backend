package com.ddhouse.chat.service;

import com.ddhouse.chat.domain.ChatRoom;
import com.ddhouse.chat.domain.User;
import com.ddhouse.chat.domain.UserChatRoom;
import com.ddhouse.chat.dto.request.UserChatRoomAddDto;
import com.ddhouse.chat.exception.NotFoundException;
import com.ddhouse.chat.repository.ChatRoomRepository;
import com.ddhouse.chat.repository.UserChatRoomRepository;
import com.ddhouse.chat.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserChatRoomService {
    private final UserChatRoomRepository userChatRoomRepository;
    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;

    public void addUsersInChatRoom(UserChatRoomAddDto userChatRoomAddDto){
        ChatRoom chatRoom = chatRoomRepository.findById(userChatRoomAddDto.getChatRoomId())
                .orElseThrow(() -> new NotFoundException("해당 아이디를 채팅방을 찾지 못했습니다."));
        List<UserChatRoom> userChatRooms = Arrays.stream(userChatRoomAddDto.getUserIds().split(","))
                .filter(s -> !s.isBlank())
                .map(Long::valueOf)
                .map(userId -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new NotFoundException("해당 아이디를 가진 유저를 찾지 못했습니다."));
                    return UserChatRoom.addUsser(user, chatRoom);
                })
                .collect(Collectors.toList());
        // 채팅방에 있는 유저 수 증가
        chatRoom.increaseMemberNums(userChatRooms.size());
        chatRoomRepository.save(chatRoom);
        // 채팅방과 유저 관계 추가
        userChatRoomRepository.saveAll(userChatRooms);
    }
}
