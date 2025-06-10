package com.ddhouse.chat.service;

import com.ddhouse.chat.dto.request.group.UserChatRoomAddDto;
import com.ddhouse.chat.repository.*;
import com.ddhouse.chat.vo.ChatRoom;
import com.ddhouse.chat.vo.User;
import com.ddhouse.chat.vo.UserChatRoom;
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
    private final ChatService chatService;


    public void addUsersInChatRoom(UserChatRoomAddDto userChatRoomAddDto){
        ChatRoom chatRoom = chatService.findChatRoomByRoomId(userChatRoomAddDto.getChatRoomId());
        // 채팅방이 아니었던 경우 채팅방으로 변경
        if(!chatRoom.getIsGroup()){
            chatRoom.updateGroup(Boolean.TRUE);
        }
        List<UserChatRoom> userChatRooms = Arrays.stream(userChatRoomAddDto.getUserIds().split(","))
                .filter(s -> !s.isBlank())
                .map(Long::valueOf)
                .map(userId -> {
                    User user = userRepository.findById(userId);
                    return UserChatRoom.group(chatRoom, user);
                })
                .collect(Collectors.toList());
        // 채팅방에 있는 유저 수 증가
        chatRoom.increaseMemberNums(userChatRooms.size());
        chatRoomRepository.save(chatRoom);
        // 채팅방과 유저 관계 추가
        userChatRoomRepository.saveAll(userChatRooms);


    }

    public UserChatRoom findByUserAndChatRoom(List<ChatRoom> chatRooms, User user) {
        for (ChatRoom chatRoom : chatRooms) {
            if(!chatRoom.getIsGroup()){
                System.out.println("findByUserIdAndChatRoomId : " + user.getId() + ", " + chatRoom.getId());
                UserChatRoom userChatRoom = userChatRoomRepository.findByUserIdAndChatRoomId(user.getId(), chatRoom.getId());
                if (userChatRoom != null) {
                    return userChatRoom;
                }
            }
        }
        return null;
    }


    public List<ChatRoom> findChatRoomsByUserId(Long userId) {
        List<UserChatRoom> chatRooms = userChatRoomRepository.findByUserId(userId);
        return chatRooms.stream()
                .map(UserChatRoom::getChatRoom)
                .collect(Collectors.toList());
    }

}
