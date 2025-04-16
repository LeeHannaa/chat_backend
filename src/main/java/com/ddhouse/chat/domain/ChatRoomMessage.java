package com.ddhouse.chat.domain;

import com.ddhouse.chat.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

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

    @Enumerated(EnumType.STRING)
    private DeleteRange isDelete;

    @ManyToOne
    @JoinColumn(name = "chatRoomId", nullable = false)
    private ChatRoom chatRoom;

    @ManyToOne
    @JoinColumn(name = "userId", nullable = false) // 매물 가진 고객
    private User user;

    public static ChatRoomMessage save(UUID msgId, User user, ChatRoom chatRoom) {
        return ChatRoomMessage.builder()
                .messageId(msgId)
                .chatRoom(chatRoom)
                .isDelete(DeleteRange.NO)
                .user(user)
                .build();
    }

    public void changeDeleteRange(DeleteRange deleteRange) {
        this.isDelete = deleteRange;
    }
}
