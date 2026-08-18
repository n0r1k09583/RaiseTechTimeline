package com.raisetech.timeline.config;

import com.raisetech.timeline.web.AuthInterceptor;
import java.util.Arrays;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

  private final AuthInterceptor authInterceptor;
  private final String[] origins;

  public WebMvcConfig(
      AuthInterceptor authInterceptor,
      @Value("${cors.origins:http://localhost:5173,http://127.0.0.1:5173}") String origins) {
    this.authInterceptor = authInterceptor;
    this.origins = Arrays.stream(origins.split(",")).map(String::trim).toArray(String[]::new);
  }

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/api/**")
        .allowedOrigins(origins)
        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
        .allowedHeaders("*");
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(authInterceptor)
        .addPathPatterns("/api/**")
        .excludePathPatterns("/api/signup", "/api/login", "/api/refresh", "/api/logout", "/api/health");
  }
}
