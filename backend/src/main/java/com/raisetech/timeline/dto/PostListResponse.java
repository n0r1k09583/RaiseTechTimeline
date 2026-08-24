package com.raisetech.timeline.dto;

import java.util.List;

public class PostListResponse {

  private final List<PostResponse> posts;
  private final boolean hasMore;

  public PostListResponse(List<PostResponse> posts, boolean hasMore) {
    this.posts = posts;
    this.hasMore = hasMore;
  }

  public List<PostResponse> getPosts() {
    return posts;
  }

  public boolean isHasMore() {
    return hasMore;
  }
}
