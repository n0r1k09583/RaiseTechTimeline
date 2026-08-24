package com.raisetech.timeline.web;

import com.raisetech.timeline.dto.CommentListResponse;
import com.raisetech.timeline.dto.CommentRequest;
import com.raisetech.timeline.dto.CommentResponse;
import com.raisetech.timeline.service.CommentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CommentController {

  private final CommentService comments;

  public CommentController(CommentService comments) {
    this.comments = comments;
  }

  @GetMapping("/api/posts/{postId}/comments")
  public CommentListResponse list(HttpServletRequest request, @PathVariable long postId) {
    return comments.list(userId(request), postId);
  }

  @PostMapping("/api/posts/{postId}/comments")
  @ResponseStatus(HttpStatus.CREATED)
  public CommentResponse create(
      HttpServletRequest request,
      @PathVariable long postId,
      @RequestBody CommentRequest body) {
    return comments.create(userId(request), postId, body == null ? null : body.getBody());
  }

  @DeleteMapping("/api/comments/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(HttpServletRequest request, @PathVariable long id) {
    comments.delete(userId(request), id);
  }

  private static long userId(HttpServletRequest request) {
    return (Long) request.getAttribute(AuthInterceptor.USER_ID_ATTR);
  }
}
