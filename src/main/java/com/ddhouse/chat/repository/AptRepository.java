package com.ddhouse.chat.repository;

import com.ddhouse.chat.exception.NotFoundException;
import com.ddhouse.chat.vo.Apt;
import com.ddhouse.chat.vo.AptList;
import lombok.RequiredArgsConstructor;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class AptRepository{
    private final SqlSessionTemplate sql;

    public List<AptList> findAllTest() {
        return sql.selectList("aptMapper.findAllTest");
    }

    public Apt findByIdx(Long idx) {
        Apt apt = sql.selectOne("aptMapper.findByIdx", idx);
        if (apt == null) {
            throw new NotFoundException("해당 ID의 매물을 찾을 수 없습니다: " + idx);
        }
        return apt;
    }
}
