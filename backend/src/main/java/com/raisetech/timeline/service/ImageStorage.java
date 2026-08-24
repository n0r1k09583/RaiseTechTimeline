package com.raisetech.timeline.service;

import com.raisetech.timeline.web.ApiException;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ImageStorage {

  private static final long MAX_BYTES = 5L * 1024 * 1024;
  private static final Set<String> TYPES = Set.of("image/jpeg", "image/png", "image/webp");
  private static final Map<String, String> EXT = Map.of(
      "image/jpeg", ".jpg",
      "image/png", ".png",
      "image/webp", ".webp");

  private final Path dir;

  public ImageStorage(@Value("${app.upload-dir:./uploads}") String uploadDir) {
    this.dir = Path.of(uploadDir).toAbsolutePath().normalize();
  }

  @PostConstruct
  public void init() {
    try {
      Files.createDirectories(dir);
    } catch (IOException ex) {
      throw new IllegalStateException("uploads ディレクトリを作れません: " + dir, ex);
    }
  }

  public Path directory() {
    return dir;
  }

  public String save(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      return null;
    }
    String type = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);
    if (!TYPES.contains(type)) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "JPEG / PNG / WebP のみです");
    }
    if (file.getSize() > MAX_BYTES) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "画像は5MBまでです");
    }
    String name = UUID.randomUUID() + EXT.get(type);
    Path dest = dir.resolve(name);
    try {
      file.transferTo(dest);
    } catch (IOException ex) {
      throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "画像の保存に失敗しました");
    }
    return name;
  }

  public void delete(String filename) {
    if (filename == null || filename.isBlank()) {
      return;
    }
    Path file = dir.resolve(Path.of(filename).getFileName().toString());
    try {
      Files.deleteIfExists(file);
    } catch (IOException ignored) {
      // 本文の削除は進める
    }
  }
}
