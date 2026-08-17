package com.raisetech.timeline;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class AuthApiTest {

  @Autowired
  MockMvc mockMvc;

  @Autowired
  ObjectMapper objectMapper;

  @BeforeAll
  static void deleteTestDb() throws Exception {
    Files.deleteIfExists(Path.of("target/timeline-test.db"));
  }

  @Test
  void healthIsOk() throws Exception {
    mockMvc.perform(get("/api/health"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.ok").value(true));
  }

  @Test
  void signupLoginAndMe() throws Exception {
    mockMvc.perform(post("/api/signup")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "username": "newuser",
                  "displayName": "新規",
                  "email": "newuser@example.com",
                  "password": "password123",
                  "confirm": "password123"
                }
                """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.token").isString())
        .andExpect(jsonPath("$.user.username").value("newuser"))
        .andExpect(jsonPath("$.user.password").doesNotExist())
        .andExpect(jsonPath("$.user.passwordDigest").doesNotExist());

    mockMvc.perform(post("/api/signup")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "username": "newuser",
                  "displayName": "重複",
                  "email": "other@example.com",
                  "password": "password123",
                  "confirm": "password123"
                }
                """))
        .andExpect(status().isConflict());

    MvcResult login = mockMvc.perform(post("/api/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "email": "yamada@example.com",
                  "password": "password123"
                }
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").isString())
        .andExpect(jsonPath("$.user.username").value("yamada"))
        .andExpect(jsonPath("$.user.password").doesNotExist())
        .andReturn();

    JsonNode body = objectMapper.readTree(login.getResponse().getContentAsString());
    String token = body.get("token").asText();

    mockMvc.perform(post("/api/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "email": "yamada@example.com",
                  "password": "wrong-password"
                }
                """))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.error").value("メールアドレスまたはパスワードが違います"));

    mockMvc.perform(get("/api/me"))
        .andExpect(status().isUnauthorized());

    mockMvc.perform(get("/api/me").header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.user.username").value("yamada"))
        .andExpect(jsonPath("$.user.password").doesNotExist());
  }
}
