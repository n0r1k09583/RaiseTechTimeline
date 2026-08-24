package com.raisetech.timeline.service;

import com.raisetech.timeline.domain.Post;
import com.raisetech.timeline.domain.User;
import com.raisetech.timeline.dto.PostListResponse;
import com.raisetech.timeline.dto.PostResponse;
import com.raisetech.timeline.mapper.CommentMapper;
import com.raisetech.timeline.mapper.PostMapper;
import com.raisetech.timeline.mapper.UserMapper;
import com.raisetech.timeline.web.ApiException;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class PostService {

  private static final int DEFAULT_LIMIT = 20;
  private static final int MAX_LIMIT = 50;
  private static final int BODY_MAX = 280;

  private final PostMapper posts;
  private final UserMapper users;
  private final CommentMapper comments;
  private final ImageStorage images;

  public PostService(PostMapper posts, UserMapper users, CommentMapper comments, ImageStorage images) {
    this.posts = posts;
    this.users = users;
    this.comments = comments;
    this.images = images;
  }

  public PostListResponse list(
      long viewerId,
      String tab,
      Integer limit,
      String beforeCreatedAt,
      Long beforeId,
      String afterCreatedAt,
      Long afterId) {
    String normalizedTab = "following".equals(tab) ? "following" : "all";
    int size = limit == null ? DEFAULT_LIMIT : Math.min(Math.max(limit, 1), MAX_LIMIT);
    boolean pagingOlder = beforeCreatedAt != null && beforeId != null;
    boolean pagingNewer = afterCreatedAt != null && afterId != null;
    int fetch = pagingNewer ? size : size + 1;
    List<Post> rows = posts.list(
        viewerId,
        normalizedTab,
        fetch,
        pagingOlder ? beforeCreatedAt : null,
        pagingOlder ? beforeId : null,
        pagingNewer ? afterCreatedAt : null,
        pagingNewer ? afterId : null);
    boolean hasMore = !pagingNewer && rows.size() > size;
    if (hasMore) {
      rows = rows.subList(0, size);
    }
    List<PostResponse> body = rows.stream().map(post -> PostResponse.from(post, viewerId)).toList();
    return new PostListResponse(body, hasMore);
  }

  public PostResponse get(long viewerId, long id) {
    return PostResponse.from(require(id), viewerId);
  }

  @Transactional
  public PostResponse create(long userId, String body, MultipartFile image) {
    String text = requireBody(body);
    Post post = new Post();
    post.setUserId(userId);
    post.setBody(text);
    post.setImagePath(images.save(image));
    posts.insert(post);
    return PostResponse.from(require(post.getId()), userId);
  }

  @Transactional
  public PostResponse update(long userId, long id, String body, MultipartFile image) {
    Post existing = requireOwned(id, userId, "自分の投稿だけ編集できます");
    String text = requireBody(body);
    existing.setBody(text);
    if (image != null && !image.isEmpty()) {
      String previous = existing.getImagePath();
      existing.setImagePath(images.save(image));
      images.delete(previous);
    }
    posts.update(existing);
    return PostResponse.from(require(id), userId);
  }

  @Transactional
  public void delete(long userId, long id) {
    Post existing = requireOwned(id, userId, "自分の投稿だけ削除できます");
    comments.deleteByPostId(id);
    posts.deleteById(id);
    images.delete(existing.getImagePath());
  }

  public void seedIfEmpty() {
    if (posts.count() > 0) {
      return;
    }
    insertSeed("hanako", "課題の要件定義、今日スタートした", "2026-08-17 21:00:00");
    insertSeed("ichiro", "タイムラインの件数、一覧で見えないと困る", "2026-08-17 20:45:00");
    insertSeed("yamada", "HTML/CSS/JS のプロトタイプを先に固める", "2026-08-17 22:10:00");
  }

  private void insertSeed(String username, String body, String createdAt) {
    User user = users.findByUsername(username);
    if (user == null) {
      return;
    }
    Post post = new Post();
    post.setUserId(user.getId());
    post.setBody(body);
    post.setCreatedAt(createdAt);
    posts.insert(post);
  }

  private Post require(long id) {
    Post post = posts.findById(id);
    if (post == null) {
      throw new ApiException(HttpStatus.NOT_FOUND, "投稿が見つかりません");
    }
    return post;
  }

  private Post requireOwned(long id, long userId, String message) {
    Post post = require(id);
    if (post.getUserId() == null || post.getUserId() != userId) {
      throw new ApiException(HttpStatus.FORBIDDEN, message);
    }
    return post;
  }

  private static String requireBody(String body) {
    String text = body == null ? "" : body.trim();
    if (text.isEmpty() || text.length() > BODY_MAX) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "本文は1〜280文字です");
    }
    return text;
  }
}
