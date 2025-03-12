package com.ddhouse.chat.service;

import com.ddhouse.chat.domain.Apt;
import com.ddhouse.chat.repository.AptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class AptService {
    private final AptRepository aptRepository;

    public List<Apt> getAptList() {
        return aptRepository.findAll();
    }
}
