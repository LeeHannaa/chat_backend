package com.ddhouse.chat.controller;

import com.ddhouse.chat.dto.request.UserChatRoomAddDto;
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
    // TODO G : 이거 chatRoomController에서 chat/invite/user/group 매핑 컨트롤러랑 똑같은 역할을 하는건지 확인 필요!!!
    public void AddUsersInChatRoom(@RequestBody UserChatRoomAddDto userChatRoomAddDto){
        // 단체 : 채팅방에 유저들 초대하는 경우
        // 1. UserChatRoom에 어떤 방에 어떤 유저가 들어왔는지 저장
        // 2. ChatRoom에 memberNum 추가된 유저 수만큼 늘리기
        userChatRoomService.addUsersInChatRoom(userChatRoomAddDto);
    }
}
