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

    public String findNameByUserId(Long myId){
        User user = findByUserId(myId);
        String name = user.getUserId();
        if (name == null) {
            throw new NotFoundException("해당 유저의 이름이 없습니다.");
        }
        return name;
    }

    public List<User> findTestUser(){
        List<User> users = userRepository.findTestUser();
        return users;
    }

}
