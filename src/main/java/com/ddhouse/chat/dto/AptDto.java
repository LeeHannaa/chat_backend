package com.ddhouse.chat.dto;

import com.ddhouse.chat.domain.Apt;
import com.ddhouse.chat.domain.ChatRoom;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class AptDto {
    private Long id;
    private String name;
    private LocalDateTime regDate;
    private Long userId;

    public static AptDto from(Apt apt) {
        return AptDto.builder()
                .id(apt.getId())
                .name(apt.getName())
                .userId(apt.getUser().getId())
                .regDate(apt.getRegDate())
                .build();
    }
}
