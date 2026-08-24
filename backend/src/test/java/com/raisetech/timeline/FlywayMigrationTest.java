package com.raisetech.timeline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.output.MigrateResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration(initializers = DeleteSqliteTestDb.class)
class FlywayMigrationTest {

  @Autowired
  Flyway flyway;

  @Autowired
  JdbcTemplate jdbc;

  @Test
  void appliesUsersRefreshTokensPostsAndComments() {
    assertNotNull(flyway.info().current());
    assertEquals("4", flyway.info().current().getVersion().getVersion());
    assertEquals(4, flyway.info().applied().length);

    MigrateResult result = flyway.migrate();
    assertEquals(0, result.migrationsExecuted);

    Integer users = jdbc.queryForObject(
        "SELECT COUNT(*) FROM sqlite_master WHERE type = 'table' AND name = 'users'",
        Integer.class);
    Integer refreshTokens = jdbc.queryForObject(
        "SELECT COUNT(*) FROM sqlite_master WHERE type = 'table' AND name = 'refresh_tokens'",
        Integer.class);
    Integer posts = jdbc.queryForObject(
        "SELECT COUNT(*) FROM sqlite_master WHERE type = 'table' AND name = 'posts'",
        Integer.class);
    Integer comments = jdbc.queryForObject(
        "SELECT COUNT(*) FROM sqlite_master WHERE type = 'table' AND name = 'comments'",
        Integer.class);
    Integer history = jdbc.queryForObject(
        "SELECT COUNT(*) FROM flyway_schema_history WHERE success = 1",
        Integer.class);
    assertEquals(1, users);
    assertEquals(1, refreshTokens);
    assertEquals(1, posts);
    assertEquals(1, comments);
    assertEquals(4, history);
  }
}
