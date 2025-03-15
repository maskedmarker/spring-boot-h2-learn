package org.example.learn.spring.boot.h2.simple.dao.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.example.learn.spring.boot.h2.commons.model.user.UserPO;

import java.util.List;
import java.util.Map;

@Mapper
public interface UserMapper {

    List<UserPO> findAll();

    int save(UserPO user);

    List<UserPO> queryByParam(Map<String, Object> param);

    List<UserPO> queryByParam2(Map<String, Object> param);
}
