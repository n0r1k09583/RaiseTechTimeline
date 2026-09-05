package com.raisetech.timeline.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class SchemaInitializer implements ApplicationRunner {

  private static final Logger log = LoggerFactory.getLogger(SchemaInitializer.class);

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
    log.info("起動時シードを確認した");
  }
}
