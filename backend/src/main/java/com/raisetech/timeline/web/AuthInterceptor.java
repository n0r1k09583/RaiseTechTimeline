package com.raisetech.timeline.web;

import com.raisetech.timeline.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

  public static final String USER_ID_ATTR = "userId";

  private final JwtService jwt;

  public AuthInterceptor(JwtService jwt) {
    this.jwt = jwt;
  }

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {
    if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
      return true;
    }
    String header = request.getHeader("Authorization");
    String token = "";
    if (header != null && header.startsWith("Bearer ")) {
      token = header.substring(7);
    }
    if (token.isEmpty()) {
      writeUnauthorized(response, "ログインしてください");
      return false;
    }
    try {
      Claims claims = jwt.parse(token);
      request.setAttribute(USER_ID_ATTR, Long.parseLong(claims.getSubject()));
      return true;
    } catch (JwtException | IllegalArgumentException ex) {
      writeUnauthorized(response, "トークンが無効です。再度ログインしてください");
      return false;
    }
  }

  private static void writeUnauthorized(HttpServletResponse response, String message) throws Exception {
    response.setStatus(401);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");
    response.getWriter().write("{\"error\":\"" + message + "\"}");
  }
}
