package com.raisetech.timeline.mapper;

import com.raisetech.timeline.domain.Like;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface LikeMapper {

  Like find(@Param("postId") long postId, @Param("userId") long userId);

  int insert(Like like);

  int delete(@Param("postId") long postId, @Param("userId") long userId);

  int count();
}
