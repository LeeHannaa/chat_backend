package com.ddhouse.chat.controller;

import com.ddhouse.chat.domain.User;
import com.ddhouse.chat.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {
    private final UserService userService;

    @GetMapping("/info")
    public User getUserInfo(@RequestParam Long myId){
        return userService.findByUserId(myId);
    }

}
