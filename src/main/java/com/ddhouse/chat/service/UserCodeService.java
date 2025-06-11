package com.ddhouse.chat.service;

import com.ddhouse.chat.fcm.dto.FcmTokenSaveRequest;
import com.ddhouse.chat.repository.UserCodeRepository;
import com.ddhouse.chat.vo.UserCode;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserCodeService {
    private final UserCodeRepository userCodeRepository;

    public String findFcmTokenByUserId(Long myId){
        UserCode userCode = userCodeRepository.findByUserIdx(myId);
        String fcmToken = userCode.getAppCode();
        return fcmToken;
    }

    public ResponseEntity<Void> addFcmToken(FcmTokenSaveRequest fcmTokenRequest){
        try {
            UserCode userCode = userCodeRepository.findByUserIdx(fcmTokenRequest.getUserIdx());
            userCode.setUpdateAppCode(fcmTokenRequest.getAppCode());
            userCodeRepository.updateAppCode(userCode);

            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
