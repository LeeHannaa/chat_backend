package com.ddhouse.chat.fcm.controller;

import com.ddhouse.chat.fcm.dto.FcmTokenSaveRequest;
import com.ddhouse.chat.fcm.dto.FcmTokenRequestDto;
import com.ddhouse.chat.fcm.service.FcmService;
import com.ddhouse.chat.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/fcmtoken")
public class FcmController {
    private final UserService userService;
    private final FcmService fcmService;

    @PostMapping("/save")
    public ResponseEntity<Void> postFcmToken(@RequestBody FcmTokenSaveRequest fcmTokenSaveRequest){
        return userService.addFcmToken(fcmTokenSaveRequest);
    }

    @PostMapping("/message") // 수동버전
    public ResponseEntity pushMessage(@RequestParam Long myId, @RequestBody FcmTokenRequestDto fcmRequestDto) throws IOException {
        String fcmToken = userService.findFcmTokenByUserId(myId);
        System.out.println(fcmToken + " "
                +fcmRequestDto.getTitle() + " " + fcmRequestDto.getBody());

        fcmService.sendMessageTo(
                fcmToken,
                fcmRequestDto.getTitle(),
                fcmRequestDto.getBody());
        return ResponseEntity.ok().build();
    }

}
