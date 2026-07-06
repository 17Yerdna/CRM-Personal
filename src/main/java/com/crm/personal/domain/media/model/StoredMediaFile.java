package com.crm.personal.domain.media.model;

public record StoredMediaFile(String relativePath, String originalFileName, String mimeType, long sizeBytes) {
}
