package com.raisetech.timeline.mapper;

import com.raisetech.timeline.domain.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {

  User findById(@Param("id") long id);

  User findByEmail(@Param("email") String email);

  User findByUsername(@Param("username") String username);

  int insert(User user);

  int count();
}
