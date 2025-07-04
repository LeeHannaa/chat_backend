package com.ddhouse.chat.repository;

import com.ddhouse.chat.exception.NotFoundException;
import com.ddhouse.chat.vo.User;
import lombok.RequiredArgsConstructor;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class UserRepository{
    private final SqlSessionTemplate sql;

    public List<User> findTestUser() {
        return sql.selectList("userMapper.findTestUser");
    }

    public User findByIdx(Long userIdx) {
        User user = sql.selectOne("userMapper.findByIdx", userIdx);
        if (user == null) {
            throw new NotFoundException("해당 ID의 유저를 찾을 수 없습니다: " + userIdx);
        }
        return user;
    }
}
