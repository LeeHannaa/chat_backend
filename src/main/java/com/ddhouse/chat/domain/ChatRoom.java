package com.ddhouse.chat.domain;

import com.ddhouse.chat.BaseEntity;
import com.ddhouse.chat.dto.info.ChatRoomDto;
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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // 임시 -> 현재는 매물 이름으로 채팅방 이름이라서 무쓸모
    private int memberNum;

    @ManyToOne
    @JoinColumn(name = "aptId", nullable = false) // 매물 가진 고객
    private Apt apt;


    public static ChatRoom from (ChatRoomDto dto) {
        return ChatRoom.builder()
                .name(dto.getName())
                .memberNum(dto.getMemberNum())
                .apt(dto.getApt())
                .build();
    }

    public void decreaseMemberNum() {
        if (this.memberNum > 0) {
            this.memberNum--;
        }
    }

    public void increaseMemberNum() {
        this.memberNum++;
    }
}
