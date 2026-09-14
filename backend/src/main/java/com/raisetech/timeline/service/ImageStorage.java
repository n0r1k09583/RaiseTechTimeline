package com.raisetech.timeline.service;

import com.raisetech.timeline.web.ApiException;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@ConditionalOnProperty(name = "app.storage", havingValue = "local", matchIfMissing = true)
public class ImageStorage implements ImageStore {

  private static final Logger log = LoggerFactory.getLogger(ImageStorage.class);

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

  @Override
  public String save(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      return null;
    }
    String name = ImageRules.newFilename(file, log);
    if (name == null) {
      return null;
    }
    Path dest = dir.resolve(name);
    try {
      file.transferTo(dest);
    } catch (IOException ex) {
      log.error("画像の保存に失敗しました", ex);
      throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "画像の保存に失敗しました");
    }
    return name;
  }

  @Override
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
