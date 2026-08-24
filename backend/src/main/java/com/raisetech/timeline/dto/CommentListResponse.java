package com.raisetech.timeline.dto;

import java.util.List;

public class CommentListResponse {

  private final List<CommentResponse> comments;

  public CommentListResponse(List<CommentResponse> comments) {
    this.comments = comments;
  }

  public List<CommentResponse> getComments() {
    return comments;
  }
}
