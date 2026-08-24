package com.raisetech.timeline.service;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class SchemaInitializer implements ApplicationRunner {

  private final AuthService auth;
  private final PostService posts;
  private final CommentService comments;

  public SchemaInitializer(AuthService auth, PostService posts, CommentService comments) {
    this.auth = auth;
    this.posts = posts;
    this.comments = comments;
  }

  @Override
  public void run(ApplicationArguments args) {
    auth.seedIfEmpty();
    posts.seedIfEmpty();
    comments.seedIfEmpty();
  }
}
