package com.raisetech.timeline.dto;

public class AuthResponse {

  private String accessToken;
  private String refreshToken;
  private UserResponse user;

  public AuthResponse(String accessToken, String refreshToken, UserResponse user) {
    this.accessToken = accessToken;
    this.refreshToken = refreshToken;
    this.user = user;
  }

  public String getAccessToken() {
    return accessToken;
  }

  public String getRefreshToken() {
    return refreshToken;
  }

  /** 以前の { token } 形式との互換。中身はアクセストークン。 */
  public String getToken() {
    return accessToken;
  }

  public UserResponse getUser() {
    return user;
  }
}
