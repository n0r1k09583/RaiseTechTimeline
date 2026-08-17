package com.raisetech.timeline.service;

import com.raisetech.timeline.domain.User;
import com.raisetech.timeline.dto.AuthResponse;
import com.raisetech.timeline.dto.LoginRequest;
import com.raisetech.timeline.dto.SignupRequest;
import com.raisetech.timeline.dto.UserResponse;
import com.raisetech.timeline.mapper.UserMapper;
import com.raisetech.timeline.web.ApiException;
import java.util.regex.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

  private static final Pattern USERNAME = Pattern.compile("^[a-z0-9_]{3,20}$");
  private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

  private final UserMapper users;
  private final JwtService jwt;
  private final PasswordEncoder passwords = new BCryptPasswordEncoder();

  public AuthService(UserMapper users, JwtService jwt) {
    this.users = users;
    this.jwt = jwt;
  }

  @Transactional
  public AuthResponse signup(SignupRequest request) {
    String username = trim(request.getUsername()).toLowerCase();
    String displayName = trim(request.getDisplayName());
    String email = trim(request.getEmail());
    String password = request.getPassword() == null ? "" : request.getPassword();
    String confirm = firstNonBlank(request.getConfirm(), request.getPasswordConfirm(), password);

    if (!USERNAME.matcher(username).matches()) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "ユーザー名は3〜20文字の半角英小文字・数字・_です");
    }
    if (displayName.isEmpty() || displayName.length() > 20) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "表示名は1〜20文字です");
    }
    if (!EMAIL.matcher(email).matches()) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "メール形式で入力してください");
    }
    if (password.length() < 8) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "パスワードは8文字以上です");
    }
    if (!password.equals(confirm)) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "パスワードが一致しません");
    }
    if (users.findByUsername(username) != null) {
      throw new ApiException(HttpStatus.CONFLICT, "このユーザー名は使われています");
    }
    if (users.findByEmail(email) != null) {
      throw new ApiException(HttpStatus.CONFLICT, "このメールは登録済みです");
    }

    User user = new User();
    user.setEmail(email);
    user.setUsername(username);
    user.setDisplayName(displayName);
    user.setPasswordDigest(passwords.encode(password));
    users.insert(user);

    User stored = users.findById(user.getId());
    return new AuthResponse(jwt.issue(stored), UserResponse.from(stored));
  }

  public AuthResponse login(LoginRequest request) {
    String email = trim(request.getEmail());
    String password = request.getPassword() == null ? "" : request.getPassword();
    if (email.isEmpty() || password.isEmpty()) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "メールアドレスとパスワードを入力してください");
    }
    User user = users.findByEmail(email);
    if (user == null || !passwords.matches(password, user.getPasswordDigest())) {
      throw new ApiException(HttpStatus.UNAUTHORIZED, "メールアドレスまたはパスワードが違います");
    }
    return new AuthResponse(jwt.issue(user), UserResponse.from(user));
  }

  public UserResponse me(long userId) {
    User user = users.findById(userId);
    if (user == null) {
      throw new ApiException(HttpStatus.UNAUTHORIZED, "ユーザーが見つかりません");
    }
    return UserResponse.from(user);
  }

  public void seedIfEmpty() {
    if (users.count() > 0) {
      return;
    }
    insertDemo("yamada@example.com", "yamada", "山田");
    insertDemo("hanako@example.com", "hanako", "佐藤 花子");
    insertDemo("ichiro@example.com", "ichiro", "鈴木 一郎");
  }

  private void insertDemo(String email, String username, String displayName) {
    User user = new User();
    user.setEmail(email);
    user.setUsername(username);
    user.setDisplayName(displayName);
    user.setPasswordDigest(passwords.encode("password123"));
    users.insert(user);
  }

  private static String trim(String value) {
    return value == null ? "" : value.trim();
  }

  private static String firstNonBlank(String... values) {
    for (String value : values) {
      if (value != null && !value.isEmpty()) {
        return value;
      }
    }
    return "";
  }
}
