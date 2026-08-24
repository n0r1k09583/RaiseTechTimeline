package com.raisetech.timeline.mapper;

import com.raisetech.timeline.domain.Comment;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CommentMapper {

  Comment findById(@Param("id") long id);

  List<Comment> listByPostId(@Param("postId") long postId);

  int insert(Comment comment);

  int deleteById(@Param("id") long id);

  int deleteByPostId(@Param("postId") long postId);

  int count();
}
