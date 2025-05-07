package com.ddhouse.chat.fcm.controller;

import com.ddhouse.chat.fcm.dto.FcmTokenSaveRequest;
import com.ddhouse.chat.fcm.service.FcmService;
import com.ddhouse.chat.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/fcmtoken")
public class FcmController {
    private final UserService userService;

    @PostMapping("/save")
    public ResponseEntity<Void> postFcmToken(@RequestBody FcmTokenSaveRequest fcmTokenSaveRequest){
        return userService.addFcmToken(fcmTokenSaveRequest);
    }
}
