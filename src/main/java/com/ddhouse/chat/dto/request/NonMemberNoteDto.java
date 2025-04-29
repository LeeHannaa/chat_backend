package com.ddhouse.chat.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class NonMemberNoteDto {
    Long aptId;
    String phoneNumber;
    String noteText;
}
