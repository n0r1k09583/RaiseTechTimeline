package com.raisetech.timeline.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.raisetech.timeline.domain.Comment;
import com.raisetech.timeline.domain.Post;
import com.raisetech.timeline.domain.User;
import com.raisetech.timeline.support.MapperH2Test;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@MapperH2Test
class PostMapperTest {

  @Autowired
  PostMapper posts;

  @Autowired
  UserMapper users;

  @Autowired
  CommentMapper comments;

  @Test
  void 無い投稿はnull() {
    assertThat(posts.findById(404)).isNull();
  }

  @Test
  void コメント件数は同じSELECTで取る() {
    User author = user("a@example.com", "author", "投稿者");
    User other = user("b@example.com", "other", "他者");
    Post post = post(author.getId(), "件数確認", "2026-09-01 10:00:00");
    comment(post.getId(), other.getId(), "1");
    comment(post.getId(), other.getId(), "2");

    Post loaded = posts.findById(post.getId());
    assertThat(loaded.getCommentCount()).isEqualTo(2);
    assertThat(loaded.getLikeCount()).isZero();
    assertThat(loaded.getUsername()).isEqualTo("author");
  }

  @Test
  void フォロー中タブは他人の投稿を出さない() {
    User me = user("me@example.com", "me_user", "自分");
    User them = user("them@example.com", "them_user", "他人");
    post(me.getId(), "自分の投稿", "2026-09-01 12:00:00");
    post(them.getId(), "他人の投稿", "2026-09-01 13:00:00");

    assertThat(posts.list(me.getId(), "following", 20, null, null, null, null))
        .extracting(Post::getBody)
        .containsExactly("自分の投稿");
    assertThat(posts.list(me.getId(), "all", 20, null, null, null, null))
        .extracting(Post::getBody)
        .containsExactly("他人の投稿", "自分の投稿");
  }

  @Test
  void コメント無しの件数は0() {
    User author = user("z@example.com", "zero_c", "零");
    Post post = post(author.getId(), "件数ゼロ", "2026-09-01 09:00:00");
    assertThat(posts.findById(post.getId()).getCommentCount()).isZero();
  }

  @Test
  void 同時刻ならidでタイブレークする() {
    User me = user("t@example.com", "tie_user", "同刻");
    Post first = post(me.getId(), "先", "2026-09-01 10:00:00");
    Post second = post(me.getId(), "後", "2026-09-01 10:00:00");

    assertThat(posts.list(me.getId(), "all", 20, null, null, first.getCreatedAt(), first.getId()))
        .extracting(Post::getId)
        .containsExactly(second.getId());
  }

  @Test
  void 古いページに新しい投稿を混ぜない() {
    User me = user("p@example.com", "pager", "頁");
    Post older = post(me.getId(), "古い", "2026-09-01 10:00:00");
    Post newer = post(me.getId(), "新しい", "2026-09-01 11:00:00");

    assertThat(posts.list(me.getId(), "all", 20, newer.getCreatedAt(), newer.getId(), null, null))
        .extracting(Post::getId)
        .containsExactly(older.getId());
  }

  private User user(String email, String username, String displayName) {
    User user = new User();
    user.setEmail(email);
    user.setUsername(username);
    user.setDisplayName(displayName);
    user.setPasswordDigest("hash");
    users.insert(user);
    return user;
  }

  private Post post(long userId, String body, String createdAt) {
    Post post = new Post();
    post.setUserId(userId);
    post.setBody(body);
    post.setCreatedAt(createdAt);
    posts.insert(post);
    return posts.findById(post.getId());
  }

  private void comment(long postId, long userId, String body) {
    Comment comment = new Comment();
    comment.setPostId(postId);
    comment.setUserId(userId);
    comment.setBody(body);
    comments.insert(comment);
  }
}
