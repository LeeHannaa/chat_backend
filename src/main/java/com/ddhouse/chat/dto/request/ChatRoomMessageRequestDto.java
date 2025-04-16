package com.ddhouse.chat.dto.request;

import com.ddhouse.chat.dto.response.ChatMessage.ChatMessageResponseDto;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class ChatRoomMessageRequestDto {
    private int roomMemberNum;
    private List<ChatMessageResponseDto> messages;

    public static ChatRoomMessageRequestDto from (int roomMemberNum, List<ChatMessageResponseDto> messages){
        return ChatRoomMessageRequestDto.builder()
                .roomMemberNum(roomMemberNum)
                .messages(messages)
                .build();
    }
}

