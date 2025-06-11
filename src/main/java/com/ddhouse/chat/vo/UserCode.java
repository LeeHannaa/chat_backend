package com.ddhouse.chat.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCode {
    private Long idx;
    private String appCode;
    private String sts; // I : 활성
    private Long userIdx;

    public void setUpdateAppCode(String appCode) {
        this.appCode = appCode;
    }
}
