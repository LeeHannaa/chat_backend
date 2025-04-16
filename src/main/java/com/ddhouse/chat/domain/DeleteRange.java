package com.ddhouse.chat.domain;

public enum DeleteRange {
    ALL, // 전체 삭제
    ME,  // 나만 삭제
    NO;   // 삭제 안 함

    public boolean isAll() {
        return this == ALL;
    }

    public boolean isMe() {
        return this == ME;
    }

    public boolean isNo() {
        return this == NO;
    }
}
