package com.ddhouse.chat.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Agency {
    private Long agencyIdx;
    private String nick;
    private User user; // pk
}
