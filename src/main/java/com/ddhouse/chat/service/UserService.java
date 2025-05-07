package com.ddhouse.chat.service;

import com.ddhouse.chat.domain.User;
import com.ddhouse.chat.fcm.dto.FcmTokenSaveRequest;
import com.ddhouse.chat.exception.NotFoundException;
import com.ddhouse.chat.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public User findByUserId(Long myId){
        User userInfo = userRepository.findById(myId)
                .orElseThrow(() -> new NotFoundException("해당 유저의 정보를 찾을 수 없습니다."));
        return userInfo;
    }

    public List<User> findUserAll(){
        List<User> users = userRepository.findAll();
        return users;
    }

    public String findFcmTokenByUserId(Long myId){
        User user = findByUserId(myId);
        String fcmToken = user.getFcmToken();
        return fcmToken;
    }

    public String findNameByUserId(Long myId){
        User user = findByUserId(myId);
        String name = user.getName();
        if (name == null) {
            throw new NotFoundException("해당 유저의 이름이 없습니다.");
        }
        return name;
    }

    public ResponseEntity<Void> addFcmToken(FcmTokenSaveRequest fcmTokenRequest){
        try {
            User user = userRepository.findById(fcmTokenRequest.getUserId())
                    .orElseThrow(() -> new NotFoundException("유저를 찾을 수 없습니다."));

            user.setUpdateFcmToken(fcmTokenRequest.getFcmToken());
            userRepository.save(user);

            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

}
