package com.ddhouse.chat.service;

import com.ddhouse.chat.domain.Apt;
import com.ddhouse.chat.dto.AptDto;
import com.ddhouse.chat.exception.NotFoundException;
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

    public AptDto getAptById(Long id) {
        Apt apt = aptRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("아파트 ID " + id + "에 해당하는 데이터가 없습니다."));

        return AptDto.from(apt);
    }

    public Apt findByAptId(Long id) {
        Apt apt = aptRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("아파트 ID " + id + "에 해당하는 데이터가 없습니다."));
        return apt;
    }

    public List<Apt> findAptsByUserId(Long userId) {
        List<Apt> apt = aptRepository.findByUserId(userId);
        return apt;
    }

}
