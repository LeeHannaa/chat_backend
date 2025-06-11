package com.ddhouse.chat.repository;

import com.ddhouse.chat.exception.NotFoundException;
import com.ddhouse.chat.vo.UserCode;
import lombok.RequiredArgsConstructor;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserCodeRepository {
    private final SqlSessionTemplate sql;

    public void updateAppCode(UserCode userCode) {
        sql.update("userCodeMapper.updateAppCode", userCode);
    }

    public UserCode findByUserIdx(Long userIdx) {
        UserCode userCode = sql.selectOne("userCodeMapper.findByUserIdx", userIdx);
        if (userCode == null) {
            throw new NotFoundException("해당 ID의 유저를 찾을 수 없습니다: " + userIdx);
        }
        return userCode;
    }

}
