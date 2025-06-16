package com.ddhouse.chat.dto;

import com.ddhouse.chat.vo.Apt;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class AptDto {
    private Long idx;
    private LocalDateTime cdate;
    private Long userId;

    public static AptDto from(Apt apt) {
        return AptDto.builder()
                .idx(apt.getIdx())
                .userId(apt.getAgency().getUser().getUserIdx())
                .cdate(apt.getCdate())
                .build();
    }
}
