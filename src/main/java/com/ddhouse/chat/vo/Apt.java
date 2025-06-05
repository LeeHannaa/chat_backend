package com.ddhouse.chat.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Apt {
    private Long id;
    private String name;
    private LocalDateTime regDate;
    private User user;
}
