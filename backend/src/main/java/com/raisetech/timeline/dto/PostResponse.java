package com.raisetech.timeline.dto;

import com.raisetech.timeline.domain.Post;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "投稿1件。imageUrl は今は /uploads/ファイル名。S3 に差し替えるときはこの URL だけ変える。")
public class PostResponse {

  private long id;
  private long userId;
  private String username;
  private String displayName;
  private String body;
  private String imageUrl;
  private String createdAt;
  private String updatedAt;
  private boolean mine;
  private int commentCount;
  private int likeCount;

  public static PostResponse from(Post post, long viewerId) {
    PostResponse response = new PostResponse();
    response.id = post.getId();
    response.userId = post.getUserId();
    response.username = post.getUsername();
    response.displayName = post.getDisplayName();
    response.body = post.getBody();
    response.imageUrl = toImageUrl(post.getImagePath());
    response.createdAt = post.getCreatedAt();
    response.updatedAt = post.getUpdatedAt();
    response.mine = post.getUserId() != null && post.getUserId() == viewerId;
    response.commentCount = post.getCommentCount();
    response.likeCount = post.getLikeCount();
    return response;
  }

  private static String toImageUrl(String imagePath) {
    if (imagePath == null || imagePath.isBlank()) {
      return null;
    }
    if (imagePath.startsWith("/")) {
      return imagePath;
    }
    return "/uploads/" + imagePath;
  }

  public long getId() {
    return id;
  }

  public long getUserId() {
    return userId;
  }

  public String getUsername() {
    return username;
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getBody() {
    return body;
  }

  public String getImageUrl() {
    return imageUrl;
  }

  public String getCreatedAt() {
    return createdAt;
  }

  public String getUpdatedAt() {
    return updatedAt;
  }

  public boolean isMine() {
    return mine;
  }

  public int getCommentCount() {
    return commentCount;
  }

  public int getLikeCount() {
    return likeCount;
  }
}
