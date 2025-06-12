package com.ddhouse.chat.controller;

import com.ddhouse.chat.dto.AptDto;
import com.ddhouse.chat.service.AptService;
import com.ddhouse.chat.vo.Apt;
import com.ddhouse.chat.vo.AptList;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/apt")
public class AptController {
    private final AptService aptService;

    @GetMapping
    public ResponseEntity<List<AptList>> getAptList() {
        List<AptList> apts = aptService.getAptList();
        System.out.println("ㅇㅏㅍㅏㅌㅡ ㅁㅐㅁㅜㄹ ㅂㅜㄹㄹㅓㅇㅗㄴ ㄱㅓㅅ ㅎㅗㅏㄱㅇㅣㄴㅎㅐㅂㅗㅣㄱ : " + apts.size());
        return ResponseEntity.ok().body(apts);
    }

    @GetMapping("/detail/{aptId}")
    public ResponseEntity<AptDto> getAptDetailInfo(@PathVariable("aptId") Long id) {
        AptDto aptDto = aptService.getAptByIdx(id);
        return ResponseEntity.ok().body(aptDto);
    }
}
