package com.ddhouse.chat.service;

import com.ddhouse.chat.dto.AptDto;
import com.ddhouse.chat.repository.AptRepository;
import com.ddhouse.chat.vo.Apt;
import com.ddhouse.chat.vo.AptList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class AptService {
    private final AptRepository aptRepository;

    public List<AptList> getAptList() {
        return aptRepository.findAllTest();
    }

    public AptDto getAptByIdx(Long idx) {
        Apt apt = aptRepository.findByIdx(idx);
        return AptDto.from(apt);
    }

    public Apt findByAptIdx(Long idx) {
        Apt apt = aptRepository.findByIdx(idx);
        return apt;
    }

}
