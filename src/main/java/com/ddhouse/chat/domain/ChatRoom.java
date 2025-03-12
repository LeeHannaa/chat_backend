package com.ddhouse.chat.domain;

import com.ddhouse.chat.BaseEntity;
import com.ddhouse.chat.dto.ChatRoomDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoom extends BaseEntity {
    @Id
    private Long id;

    private String name; // 임시 -> 현재는 매물 이름으로 채팅방 이름이라서 무쓸모
    private int memberNum;

    @ManyToOne
    @JoinColumn(name = "userId", nullable = false) // 매물 문의 고객
    private User user;

    @ManyToOne
    @JoinColumn(name = "aptId", nullable = false) // 매물 가진 고객
    private Apt apt;


    public static ChatRoom from (ChatRoomDto dto) {
        return ChatRoom.builder()
                .name(dto.getName())
                .memberNum(dto.getMemberNum())
                .build();
    }
}
