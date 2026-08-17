package com.raisetech.timeline.service;

import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class SchemaInitializer implements ApplicationRunner {

  private final JdbcTemplate jdbc;
  private final AuthService auth;
  private final String dbPath;

  public SchemaInitializer(
      JdbcTemplate jdbc,
      AuthService auth,
      @Value("${spring.datasource.url}") String jdbcUrl) {
    this.jdbc = jdbc;
    this.auth = auth;
    this.dbPath = jdbcUrl.startsWith("jdbc:sqlite:") ? jdbcUrl.substring("jdbc:sqlite:".length()) : "";
  }

  @Override
  public void run(ApplicationArguments args) throws Exception {
    if (!dbPath.isEmpty() && !dbPath.contains(":memory:") && !dbPath.startsWith("file:")) {
      Path file = Path.of(dbPath);
      Path parent = file.getParent();
      if (parent != null) {
        Files.createDirectories(parent);
      }
    }
    jdbc.execute(
        """
        CREATE TABLE IF NOT EXISTS users (
          id INTEGER PRIMARY KEY AUTOINCREMENT,
          email TEXT NOT NULL UNIQUE,
          username TEXT NOT NULL UNIQUE,
          display_name TEXT NOT NULL,
          password_digest TEXT NOT NULL,
          created_at TEXT NOT NULL DEFAULT (datetime('now')),
          updated_at TEXT NOT NULL DEFAULT (datetime('now'))
        )
        """);
    auth.seedIfEmpty();
  }
}
