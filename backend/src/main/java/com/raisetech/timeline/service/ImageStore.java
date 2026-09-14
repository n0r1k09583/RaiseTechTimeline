package com.raisetech.timeline.service;

import org.springframework.web.multipart.MultipartFile;

public interface ImageStore {

  String save(MultipartFile file);

  void delete(String filename);
}
