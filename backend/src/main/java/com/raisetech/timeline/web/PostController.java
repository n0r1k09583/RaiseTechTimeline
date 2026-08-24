package com.raisetech.timeline.web;

import com.raisetech.timeline.dto.PostListResponse;
import com.raisetech.timeline.dto.PostResponse;
import com.raisetech.timeline.service.PostService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/posts")
public class PostController {

  private final PostService posts;

  public PostController(PostService posts) {
    this.posts = posts;
  }

  @GetMapping
  public PostListResponse list(
      HttpServletRequest request,
      @RequestParam(defaultValue = "all") String tab,
      @RequestParam(required = false) Integer limit,
      @RequestParam(required = false) String beforeCreatedAt,
      @RequestParam(required = false) Long beforeId,
      @RequestParam(required = false) String afterCreatedAt,
      @RequestParam(required = false) Long afterId) {
    long userId = userId(request);
    return posts.list(userId, tab, limit, beforeCreatedAt, beforeId, afterCreatedAt, afterId);
  }

  @GetMapping("/{id}")
  public PostResponse get(HttpServletRequest request, @PathVariable long id) {
    return posts.get(userId(request), id);
  }

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  public PostResponse create(
      HttpServletRequest request,
      @RequestParam String body,
      @RequestParam(required = false) MultipartFile image) {
    return posts.create(userId(request), body, image);
  }

  @PatchMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public PostResponse update(
      HttpServletRequest request,
      @PathVariable long id,
      @RequestParam String body,
      @RequestParam(required = false) MultipartFile image) {
    return posts.update(userId(request), id, body, image);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(HttpServletRequest request, @PathVariable long id) {
    posts.delete(userId(request), id);
  }

  private static long userId(HttpServletRequest request) {
    return (Long) request.getAttribute(AuthInterceptor.USER_ID_ATTR);
  }
}
