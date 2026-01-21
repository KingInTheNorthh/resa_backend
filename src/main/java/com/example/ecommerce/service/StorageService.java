package com.example.ecommerce.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    StoredFile store(MultipartFile file);

    Resource loadAsResource(String key);

    void delete(String key);
}
