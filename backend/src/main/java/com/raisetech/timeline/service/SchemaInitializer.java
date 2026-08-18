package com.raisetech.timeline.service;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class SchemaInitializer implements ApplicationRunner {

  private final AuthService auth;

  public SchemaInitializer(AuthService auth) {
    this.auth = auth;
  }

  @Override
  public void run(ApplicationArguments args) {
    auth.seedIfEmpty();
  }
}
