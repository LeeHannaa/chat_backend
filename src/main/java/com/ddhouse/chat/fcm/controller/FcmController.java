package com.ddhouse.chat.fcm.controller;

import com.ddhouse.chat.fcm.dto.FcmTokenSaveRequest;
import com.ddhouse.chat.service.UserCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/fcmtoken")
public class FcmController {
    private final UserCodeService userCodeService;

    @PostMapping("/save")
    public ResponseEntity<Void> postFcmToken(@RequestBody FcmTokenSaveRequest fcmTokenSaveRequest){
        return userCodeService.addFcmToken(fcmTokenSaveRequest);
    }
}
