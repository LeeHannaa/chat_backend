package com.ddhouse.chat.controller;

import com.ddhouse.chat.dto.request.group.UserChatRoomAddDto;
import com.ddhouse.chat.service.UserChatRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/userchat")
public class UserChatRoomController {
    private final UserChatRoomService userChatRoomService;

    @PostMapping("/add")
    public void AddUsersInChatRoom(@RequestBody UserChatRoomAddDto userChatRoomAddDto){
        userChatRoomService.addUsersInChatRoom(userChatRoomAddDto);
    }
}
