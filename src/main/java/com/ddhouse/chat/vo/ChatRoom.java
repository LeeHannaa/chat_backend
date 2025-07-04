package com.ddhouse.chat.vo;

import com.ddhouse.chat.dto.ChatRoomCreateDto;
import com.ddhouse.chat.dto.ChatRoomDto;
import com.ddhouse.chat.dto.request.group.GroupChatRoomCreateDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoom {
    private Long idx;
    private String name; // nullble -> 1:1 채팅인 경우 서로 상대의 이름으로 채팅방 이름 보이도록 설정 | 단체 채팅 시 이름 지정
    private int memberNum;
    private Boolean isGroup;
    private String phoneNumber; // 비회원 채팅방
    private LocalDateTime cdate;

    public static ChatRoom from (ChatRoomDto dto) {
        // 아파트 문의하기를 통해 방이 생성된 경우 -> 1:1 채팅
        return ChatRoom.builder()
                .name(dto.getName())
                .memberNum(dto.getMemberNum())
                .cdate(dto.getCdate())
                .isGroup(Boolean.FALSE)
                .build();
    }

    public static ChatRoom from (ChatRoomCreateDto chatRoomCreateDto) {
        // 비회원이 문의 시 채팅방이 생성되는 경우
        return ChatRoom.builder()
                .memberNum(chatRoomCreateDto.getMemberNum())
                .isGroup(Boolean.FALSE)
                .cdate(chatRoomCreateDto.getCdate())
                .phoneNumber(chatRoomCreateDto.getPhoneNumber())
                .build();
    }

    public static ChatRoom group(GroupChatRoomCreateDto groupChatRoomCreateDto){
        // 그룹 단체 채팅방 생성
        return ChatRoom.builder()
                .name(groupChatRoomCreateDto.getChatRoomName())
                .memberNum(groupChatRoomCreateDto.getUserIds().size())
                .isGroup(Boolean.TRUE)
                .cdate(groupChatRoomCreateDto.getCdate())
                .build();
    }
    public void decreaseMemberNum() {
        if (this.memberNum > 0) {
            this.memberNum--;
        }
    }
    public void updateGroup(Boolean isGroup){
        this.isGroup = isGroup;
    }

    public void increaseMemberNum() {
        this.memberNum++;
    }

    public void increaseMemberNums(int addCount) {
        this.memberNum += addCount;
    }
}
