package com.ddhouse.chat.service;

import com.ddhouse.chat.domain.User;
import com.ddhouse.chat.exception.NotFoundException;
import com.ddhouse.chat.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public User findByUserId(Long myId){
        User userInfo = userRepository.findById(myId)
                .orElseThrow(() -> new NotFoundException("해당 유저의 정보를 찾을 수 없습니다."));
        return userInfo;
    }
}
