package com.ddhouse.chat.controller;

import com.ddhouse.chat.service.UserService;
import com.ddhouse.chat.vo.User;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {
    private final UserService userService;

    @GetMapping("/info")
    public User getUserInfo(@RequestParam Long myId){
        return userService.findByUserId(myId);
    }

    @GetMapping("/all")
    public List<User> getUserAll(){
        return userService.findUserAll();
    }

}
