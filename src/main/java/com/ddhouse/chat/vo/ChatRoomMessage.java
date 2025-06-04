package com.ddhouse.chat.vo;

import com.ddhouse.chat.dto.SaveMessageDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomMessage {
    private Long id;
    private String msg;
    private Boolean isDelete; // 전체 삭제 여부
    private String deleteUsers; // ,를 기준으로 유저 아이디 저장
    private MessageType type;
    private LocalDateTime regDate;
    private ChatRoom chatRoom;
    private User user;

    public static ChatRoomMessage save(SaveMessageDto saveMessageDto, User user, ChatRoom chatRoom, MessageType messageType) {
        return ChatRoomMessage.builder()
                .msg(saveMessageDto.getMsg())
                .chatRoom(chatRoom)
                .type(messageType)
                .isDelete(false)
                .regDate(saveMessageDto.getRegDate())
                .user(user)
                .build();
    }

    public static ChatRoomMessage save(String msg, User user, ChatRoom chatRoom, MessageType messageType) {
        return ChatRoomMessage.builder()
                .msg(msg)
                .chatRoom(chatRoom)
                .type(messageType)
                .isDelete(false)
                .regDate(LocalDateTime.now())
                .user(user)
                .build();
    }

    public void updateInvite(Boolean isDelete){
        this.isDelete = isDelete;
    }

    public void deleteMessageAll() {
        if (!this.isDelete) {
            this.isDelete = true;
        }
    }

    public String addDeleteMssUser(Long myId){
        if (this.deleteUsers == null){
            deleteUsers = myId.toString();
        } else {
            deleteUsers = "," + myId.toString();
        }
        return deleteUsers;
    }

    public List<Long> getDeleteUserList() {
        if (deleteUsers == null || deleteUsers.isBlank()) return Collections.emptyList();
        return Arrays.stream(deleteUsers.split(","))
                .filter(s -> !s.isBlank())
                .map(Long::valueOf)
                .collect(Collectors.toList());
    }

}
