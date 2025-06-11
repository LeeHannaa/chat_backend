package com.ddhouse.chat.service;

import com.ddhouse.chat.dto.AptDto;
import com.ddhouse.chat.repository.AptRepository;
import com.ddhouse.chat.vo.Apt;
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
        return aptRepository.findAllTest();
    }

    public AptDto getAptById(Long id) {
        Apt apt = aptRepository.findById(id);
        return AptDto.from(apt);
    }

    public Apt findByAptId(Long id) {
        Apt apt = aptRepository.findById(id);
        return apt;
    }

    public List<Apt> findAptsByUserId(Long userId) {
        List<Apt> apt = aptRepository.findByUserId(userId);
        return apt;
    }

}
