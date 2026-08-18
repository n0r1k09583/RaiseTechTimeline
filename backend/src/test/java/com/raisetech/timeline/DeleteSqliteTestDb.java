package com.raisetech.timeline;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

public class DeleteSqliteTestDb implements ApplicationContextInitializer<ConfigurableApplicationContext> {

  @Override
  public void initialize(ConfigurableApplicationContext applicationContext) {
    try {
      Files.createDirectories(Path.of("target"));
      Files.deleteIfExists(Path.of("target/timeline-test.db"));
      Files.deleteIfExists(Path.of("target/timeline-test.db-wal"));
      Files.deleteIfExists(Path.of("target/timeline-test.db-shm"));
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }
}
