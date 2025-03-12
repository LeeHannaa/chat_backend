package com.ddhouse.chat.controller;

import com.ddhouse.chat.domain.Apt;
import com.ddhouse.chat.service.AptService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/apt")
public class AptController {
    private final AptService aptService;

    @GetMapping
    public ResponseEntity<List<Apt>> getAptList() {
        List<Apt> response = aptService.getAptList();
        return ResponseEntity.ok().body(response);
    }
}
