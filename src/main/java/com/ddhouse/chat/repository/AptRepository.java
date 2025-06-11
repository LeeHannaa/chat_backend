package com.ddhouse.chat.repository;

import com.ddhouse.chat.exception.NotFoundException;
import com.ddhouse.chat.vo.Apt;
import lombok.RequiredArgsConstructor;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class AptRepository{
    private final SqlSessionTemplate sql;

    public List<Apt> findByUserId(Long userId) {
        return sql.selectList("aptMapper.findByUserId", userId);
    }

    public List<Apt> findAllTest() {
        return sql.selectList("aptMapper.findAllTest");
    }

    public Apt findById(Long id) {
        Apt apt = sql.selectOne("aptMapper.findById", id);
        if (apt == null) {
            throw new NotFoundException("해당 ID의 매물을 찾을 수 없습니다: " + id);
        }
        return apt;
    }
}
