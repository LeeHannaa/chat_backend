package com.ddhouse.chat.service;

import com.ddhouse.chat.domain.Apt;
import com.ddhouse.chat.domain.Note;
import com.ddhouse.chat.dto.FcmDto;
import com.ddhouse.chat.dto.request.NonMemberNoteDto;
import com.ddhouse.chat.dto.response.SendNoteDto;
import com.ddhouse.chat.exception.NotFoundException;
import com.ddhouse.chat.fcm.service.FcmService;
import com.ddhouse.chat.repository.NoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
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
    private final UserService userService;
    private final FcmService fcmService;
    private final SimpMessageSendingOperations template;
    private final NoteUnreadService noteUnreadService;

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

        // redis에 해당 쪽지 안읽음 처리 저장
        noteUnreadService.addUnreadNote(sendNoteDto.getNoteId().toString(), receiveId.toString());

        String fcmToken = userService.findFcmTokenByUserId(receiveId);
        String body = sendNoteDto.getAptName() + " : " + sendNoteDto.getNoteText();
        if (fcmToken != null) {
            try {
                fcmService.sendMessageTo(FcmDto.note(fcmToken, body));
            } catch (IOException e) {
                System.err.println("FCM 전송 실패: " + e.getMessage());
            }
        }
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
                .map(note -> {
                    SendNoteDto dto = SendNoteDto.from(note);
                    // 각 쪽지마다 읽었는지 안읽었는지 확인
                    Boolean isUnread = noteUnreadService.getUnreadCountByNote(note.getId().toString(), note.getApt().getUser().getId().toString());
                    dto.setIsRead(!isUnread); // 읽지 않았으면 false, 읽었으면 true
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public void readNoteToDelete(Long userId, Long noteId){
        // redis에 해당 쪽지 읽음 처리 -> 삭제
        noteUnreadService.removeUnreadNote(noteId.toString(), userId.toString());
        System.out.println("redis에서 쪽지 읽음 처리 완료! (삭제 완료)");
    }

    @Transactional
    public void deleteNote(Long noteId, Long userId){
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new NotFoundException("해당 쪽지 메시지를 찾을 수 없습니다."));
        // redis에서도 삭제!
        noteUnreadService.removeUnreadNote(noteId.toString(), userId.toString());
        noteRepository.delete(note);
    }
}
