package com.ddhouse.chat.controller;

import com.ddhouse.chat.domain.Apt;
import com.ddhouse.chat.domain.ChatRoom;
import com.ddhouse.chat.domain.Note;
import com.ddhouse.chat.dto.request.GroupChatRoomCreateDto;
import com.ddhouse.chat.dto.request.NonMemberNoteDto;
import com.ddhouse.chat.dto.response.ChatRoomInfoResponseDto;
import com.ddhouse.chat.dto.response.SendNoteDto;
import com.ddhouse.chat.service.NoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/note")
public class NoteController {
    private final NoteService noteService;

    @PostMapping("/send/nonmember")
    public ResponseEntity<Void> sendNoteNonMember(@RequestBody NonMemberNoteDto nonMemberNoteDto) {
        // 비회원 유저가 쪽지 문의 남기는 경우
        noteService.sendNoteNonMember(nonMemberNoteDto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/list")
    public ResponseEntity<List<SendNoteDto>> getAptList(@RequestParam Long myId) {
        List<SendNoteDto> sendNoteDtos = noteService.findMyNoteList(myId);
        return ResponseEntity.ok().body(sendNoteDtos);
    }

}
