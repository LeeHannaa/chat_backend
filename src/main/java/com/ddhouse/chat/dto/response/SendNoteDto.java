package com.ddhouse.chat.dto.response;

import com.ddhouse.chat.domain.Note;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class SendNoteDto {
    Long noteId;
    Long aptId;
    String aptName;
    String phoneNumber;
    String noteText;
    LocalDateTime regDate;
    Boolean isRead;

    public static SendNoteDto from(Note note) {
        return SendNoteDto.builder()
                .noteId(note.getId())
                .aptId(note.getApt().getId())
                .aptName(note.getApt().getName())
                .phoneNumber(note.getPhoneNumber())
                .noteText(note.getNoteText())
                .regDate(note.getRegDate())
                .build();
    }
}
