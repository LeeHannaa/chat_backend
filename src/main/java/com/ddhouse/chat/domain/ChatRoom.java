package com.ddhouse.chat.domain;

import com.ddhouse.chat.BaseEntity;
import com.ddhouse.chat.dto.info.ChatRoomDto;
import com.ddhouse.chat.dto.request.GroupChatRoomCreateDto;
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
    private Boolean isGroup;

    @ManyToOne
    @JoinColumn(name = "aptId", nullable = true) // 매물 가진 고객 -> 단체 채팅 때문에 null 설정
    private Apt apt;


    public static ChatRoom from (ChatRoomDto dto) {
        // 아파트 문의하기를 통해 방이 생성된 경우 -> 1:1 채팅
        return ChatRoom.builder()
                .name(dto.getName())
                .memberNum(dto.getMemberNum())
                .apt(dto.getApt())
                .isGroup(Boolean.FALSE)
                .build();
    }

    public static ChatRoom group(String name, int count){
        // 그룹 단체 채팅방 생성
        return ChatRoom.builder()
                .name(name)
                .memberNum(count)
                .isGroup(Boolean.TRUE)
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

    public void increaseMemberNums(int addCount) {
        this.memberNum += addCount;
    }
}
