package com.ddhouse.chat.domain;

import com.ddhouse.chat.BaseEntity;
import com.ddhouse.chat.dto.ChatRoomDto;
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
public class ChatRoom extends BaseEntity {
    @Id
    private Long id; // userId

    private String name;
    private int memberNum;

    public static ChatRoom from (ChatRoomDto dto) {
        return ChatRoom.builder()
                .name(dto.getName())
                .memberNum(dto.getMemberNum())
                .build();
    }
}
