package com.raisetech.timeline.mapper;

import com.raisetech.timeline.domain.RefreshToken;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface RefreshTokenMapper {

  RefreshToken findValidByHash(@Param("tokenHash") String tokenHash, @Param("now") long now);

  int insert(RefreshToken token);

  int deleteById(@Param("id") long id);

  int deleteByHash(@Param("tokenHash") String tokenHash);
}
