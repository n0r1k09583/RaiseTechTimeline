package com.raisetech.timeline.service;

import com.raisetech.timeline.domain.Like;
import com.raisetech.timeline.domain.Post;
import com.raisetech.timeline.dto.PostResponse;
import com.raisetech.timeline.mapper.LikeMapper;
import com.raisetech.timeline.mapper.PostMapper;
import com.raisetech.timeline.web.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LikeService {

  private static final Logger log = LoggerFactory.getLogger(LikeService.class);

  private final LikeMapper likes;
  private final PostMapper posts;

  public LikeService(LikeMapper likes, PostMapper posts) {
    this.likes = likes;
    this.posts = posts;
  }

  @Transactional
  public PostResponse toggle(long userId, long postId) {
    Post post = posts.findById(postId);
    if (post == null) {
      throw new ApiException(HttpStatus.NOT_FOUND, "投稿が見つかりません");
    }
    Like existing = likes.find(postId, userId);
    if (existing == null) {
      Like row = new Like();
      row.setPostId(postId);
      row.setUserId(userId);
      likes.insert(row);
      log.info("いいね userId={} postId={}", userId, postId);
    } else {
      likes.delete(postId, userId);
      log.info("いいね解除 userId={} postId={}", userId, postId);
    }
    return PostResponse.from(posts.findForViewer(postId, userId), userId);
  }

  public void seedIfEmpty() {
    if (likes.count() > 0) {
      return;
    }
    Post first = posts.list(1, "all", 1, null, null, null, null).stream().findFirst().orElse(null);
    if (first == null || first.getUserId() == null) {
      return;
    }
    Like row = new Like();
    row.setPostId(first.getId());
    row.setUserId(first.getUserId());
    likes.insert(row);
  }
}
