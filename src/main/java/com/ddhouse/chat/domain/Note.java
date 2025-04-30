package com.ddhouse.chat.domain;

import com.ddhouse.chat.BaseEntity;
import com.ddhouse.chat.dto.request.NonMemberNoteDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Note extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String phoneNumber;
    private String noteText;


    @ManyToOne
    @JoinColumn(name = "aptId", nullable = false) // 매물 가진 고객에게 비회원이 쪽지 남기기
    private Apt apt;

    public static Note save(NonMemberNoteDto nonMemberNoteDto, Apt apt) {
        return Note.builder()
                .phoneNumber(nonMemberNoteDto.getPhoneNumber())
                .noteText(nonMemberNoteDto.getNoteText())
                .apt(apt)
                .build();
    }
}
