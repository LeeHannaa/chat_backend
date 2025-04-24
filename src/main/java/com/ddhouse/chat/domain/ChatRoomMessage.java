package com.ddhouse.chat.domain;

import com.ddhouse.chat.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomMessage extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private UUID messageId; // Cassandra
    private Boolean isDelete; // 전체 삭제 여부
    private String deleteUsers; // ,를 기준으로 유저 아이디 저장
    @Enumerated(EnumType.STRING)
    private MessageType type;

    @ManyToOne
    @JoinColumn(name = "chatRoomId", nullable = false)
    private ChatRoom chatRoom;

    @ManyToOne
    @JoinColumn(name = "userId", nullable = false) // 채팅 작성자
    private User user;

    public static ChatRoomMessage save(UUID msgId, User user, ChatRoom chatRoom, MessageType messageType) {
        return ChatRoomMessage.builder()
                .messageId(msgId)
                .chatRoom(chatRoom)
                .type(messageType)
                .isDelete(false)
                .user(user)
                .build();
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
