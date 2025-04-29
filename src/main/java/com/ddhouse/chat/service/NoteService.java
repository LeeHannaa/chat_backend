package com.ddhouse.chat.service;

import com.ddhouse.chat.domain.Apt;
import com.ddhouse.chat.domain.Note;
import com.ddhouse.chat.domain.UserChatRoom;
import com.ddhouse.chat.dto.request.NonMemberNoteDto;
import com.ddhouse.chat.dto.response.ChatRoomInfoResponseDto;
import com.ddhouse.chat.dto.response.SendNoteDto;
import com.ddhouse.chat.repository.NoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class NoteService {
    private final NoteRepository noteRepository;
    private final AptService aptService;
    private final SimpMessageSendingOperations template;

    public void sendNoteNonMember(NonMemberNoteDto nonMemberNoteDto){
        /*
        1. 비회원 유저는 한번의 발신만 가능 (전화번호를 입력해서)
        2. 매물 등록자는 비회원 유저의 전화번호를 통해 문의 내용을 확인하고 연락한다.
        3. 쪽지 저장
        4. 실시간으로 매물 등록자에게 쪽지가 왔음을 알린다.
        */
        Apt apt = aptService.findByAptId(nonMemberNoteDto.getAptId());
        Note note = noteRepository.save(Note.save(nonMemberNoteDto, apt));

        SendNoteDto sendNoteDto = SendNoteDto.from(note);
        Map<String, Object> sendNote = Map.of(
                "type", "NOTE",
                "message", sendNoteDto
        );
        Long receiveId = apt.getUser().getId();
        template.convertAndSend("/topic/user/" + receiveId, sendNote);
    }

    public List<SendNoteDto> findMyNoteList(Long myId) {
        List<Apt> apts = aptService.findAptsByUserId(myId);
        List<Long> aptIds = apts.stream()
                .map(Apt::getId)
                .collect(Collectors.toList());

        if (aptIds.isEmpty()) {
            return Collections.emptyList(); // 아파트가 없으면 쪽지도 없음
        }
        List<Note> notes = noteRepository.findByAptIdIn(aptIds);
        return notes.stream()
                .map(SendNoteDto::from)
                .collect(Collectors.toList());
    }
}
