package com.ddhouse.chat.controller;

import com.ddhouse.chat.dto.request.NonMemberNoteDto;
import com.ddhouse.chat.dto.response.SendNoteDto;
import com.ddhouse.chat.service.NoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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
    public ResponseEntity<List<SendNoteDto>> getNoteList(@RequestParam Long myId) {
        List<SendNoteDto> sendNoteDtos = noteService.findMyNoteList(myId);
        return ResponseEntity.ok().body(sendNoteDtos);
    }

    @PostMapping("/read/note")
    public ResponseEntity<Void> readNoteNonMember(@RequestParam Long myId, @RequestBody Long noteId) {
        // 쪽지를 읽은 경우
        noteService.readNoteToDelete(myId, noteId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/delete/{noteId}")
    public ResponseEntity<Void> deleteChatMessageAll(@PathVariable("noteId") Long noteId, @RequestParam("myId") Long myId){
        noteService.deleteNote(noteId, myId);
        return ResponseEntity.ok().build();
    }
}
