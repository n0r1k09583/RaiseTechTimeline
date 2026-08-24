package com.raisetech.timeline.mapper;

import com.raisetech.timeline.domain.Post;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PostMapper {

  Post findById(@Param("id") long id);

  List<Post> list(
      @Param("viewerId") long viewerId,
      @Param("tab") String tab,
      @Param("limit") int limit,
      @Param("beforeCreatedAt") String beforeCreatedAt,
      @Param("beforeId") Long beforeId,
      @Param("afterCreatedAt") String afterCreatedAt,
      @Param("afterId") Long afterId);

  int insert(Post post);

  int update(Post post);

  int deleteById(@Param("id") long id);

  int count();
}
