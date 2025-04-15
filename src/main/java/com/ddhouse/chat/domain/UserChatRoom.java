package com.ddhouse.chat.domain;

import com.ddhouse.chat.BaseEntity;
import com.ddhouse.chat.dto.ChatRoomDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserChatRoom extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long consultId; // 매물 등록자 id

    @ManyToOne
    @JoinColumn(name = "userId", nullable = true) // 매물 문의자
    private User user;

    @ManyToOne
    @JoinColumn(name = "chatRoomId", nullable = false)
    private ChatRoom chatRoom;

    public static UserChatRoom from(ChatRoomDto dto, ChatRoom chatRoom) {
        return UserChatRoom.builder()
                .consultId(dto.getApt().getUser().getId())
                .user(dto.getUser())
                .chatRoom(chatRoom)
                .build();
    }

    public void deleteChatRoomConsultId() {
        if (this.consultId != 0) {
            this.consultId = Long.valueOf(0);
        }
    }
    public void deleteChatRoomUserId() {
        if (this.user.getId() != 0) {
            this.user = null;
        }
    }
}
