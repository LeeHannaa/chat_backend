package com.ddhouse.chat.service;

import com.ddhouse.chat.exception.NotFoundException;
import com.ddhouse.chat.repository.UserRepository;
import com.ddhouse.chat.vo.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public User findByUserId(Long myId){
        return userRepository.findByIdx(myId);
    }

    public List<User> findTestUser(){
        List<User> users = userRepository.findTestUser();
        return users;
    }

}
