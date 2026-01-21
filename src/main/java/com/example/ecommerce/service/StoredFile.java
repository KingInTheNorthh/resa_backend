package com.example.ecommerce.service;

public class StoredFile {
    private final String key;
    private final String originalFilename;
    private final String contentType;
    private final long size;

    public StoredFile(String key, String originalFilename, String contentType, long size) {
        this.key = key;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.size = size;
    }

    public String getKey() {
        return key;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public String getContentType() {
        return contentType;
    }

    public long getSize() {
        return size;
    }
}
