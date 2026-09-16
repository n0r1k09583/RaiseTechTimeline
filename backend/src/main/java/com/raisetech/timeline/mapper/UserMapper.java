package com.raisetech.timeline.mapper;

import com.raisetech.timeline.domain.Profile;
import com.raisetech.timeline.domain.User;
import com.raisetech.timeline.domain.UserSummary;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {

  User findById(@Param("id") long id);

  User findByEmail(@Param("email") String email);

  User findByUsername(@Param("username") String username);

  Profile findProfile(@Param("username") String username, @Param("viewerId") long viewerId);

  List<UserSummary> search(@Param("patterns") List<String> patterns, @Param("viewerId") long viewerId);

  int insert(User user);

  int count();
}
