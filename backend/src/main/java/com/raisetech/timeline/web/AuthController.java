package com.raisetech.timeline.web;

import com.raisetech.timeline.dto.AuthResponse;
import com.raisetech.timeline.dto.HealthResponse;
import com.raisetech.timeline.dto.LoginRequest;
import com.raisetech.timeline.dto.SignupRequest;
import com.raisetech.timeline.dto.UserResponse;
import com.raisetech.timeline.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AuthController {

  private final AuthService auth;

  public AuthController(AuthService auth) {
    this.auth = auth;
  }

  @GetMapping("/health")
  public HealthResponse health() {
    return new HealthResponse();
  }

  @PostMapping("/signup")
  @ResponseStatus(HttpStatus.CREATED)
  public AuthResponse signup(@RequestBody SignupRequest request) {
    return auth.signup(request);
  }

  @PostMapping("/login")
  public AuthResponse login(@RequestBody LoginRequest request) {
    return auth.login(request);
  }

  @GetMapping("/me")
  public Map<String, UserResponse> me(HttpServletRequest request) {
    long userId = (Long) request.getAttribute(AuthInterceptor.USER_ID_ATTR);
    return Map.of("user", auth.me(userId));
  }
}
