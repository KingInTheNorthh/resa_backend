package com.example.ecommerce.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import java.io.IOException;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
@ConditionalOnProperty(name = "app.storage.type", havingValue = "cloudinary")
public class CloudinaryStorageService implements StorageService {

    private final Cloudinary cloudinary;
    private final String folder;

    public CloudinaryStorageService(@Value("${app.storage.cloudinary.url:}") String cloudinaryUrl,
                                    @Value("${app.storage.cloudinary.folder:}") String folder) {
        if (cloudinaryUrl == null || cloudinaryUrl.isBlank()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Cloudinary URL is not configured");
        }
        this.cloudinary = new Cloudinary(cloudinaryUrl);
        this.folder = folder;
    }

    @Override
    public StoredFile store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image file is required");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only image uploads are supported");
        }
        try {
            Map<String, Object> options = ObjectUtils.asMap("resource_type", "image");
            if (folder != null && !folder.isBlank()) {
                options.put("folder", folder);
            }
            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), options);
            String publicId = (String) uploadResult.get("public_id");
            Number bytes = (Number) uploadResult.get("bytes");
            long size = bytes == null ? file.getSize() : bytes.longValue();
            return new StoredFile(publicId, file.getOriginalFilename(), contentType, size);
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload image", ex);
        } catch (RuntimeException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Cloudinary upload failed", ex);
        }
    }

    @Override
    public Resource loadAsResource(String key) {
        try {
            String url = cloudinary.url().secure(true).resourceType("image").generate(key);
            return new UrlResource(url);
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to read image", ex);
        }
    }

    @Override
    public void delete(String key) {
        try {
            cloudinary.uploader().destroy(key, ObjectUtils.asMap("resource_type", "image"));
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete image", ex);
        } catch (RuntimeException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Cloudinary delete failed", ex);
        }
    }
}
