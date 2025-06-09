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

    public void save(User user) {
        sql.insert("userMapper.save", user);
    }

    public void updateFcm(User user) {
        sql.update("userMapper.updateFcm", user);
    }

    public List<User> findAll() {
        return sql.selectList("userMapper.findAll");
    }

    public User findById(Long id) {
        User user = sql.selectOne("userMapper.findById", id);
        if (user == null) {
            throw new NotFoundException("해당 ID의 유저를 찾을 수 없습니다: " + id);
        }
        return user;
    }
}
