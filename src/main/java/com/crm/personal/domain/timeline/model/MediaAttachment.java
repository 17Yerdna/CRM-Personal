package com.crm.personal.domain.timeline.model;

import java.nio.file.Path;
import java.time.LocalDateTime;

public record MediaAttachment(
        Long id,
        String relativePath,
        Path absolutePath,
        String originalFileName,
        String mimeType,
        String description,
        LocalDateTime capturedAt
) {
}
