package com.ddhouse.chat.domain;

import com.ddhouse.chat.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;  // 비유저 전화번호 쪽지위해 임시 데이터
    private String fcmToken;

    public void setUpdateFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }
}
