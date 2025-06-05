package com.ddhouse.chat.vo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MessageType {
    TEXT,
    // IMAGE,
    SYSTEM,
}