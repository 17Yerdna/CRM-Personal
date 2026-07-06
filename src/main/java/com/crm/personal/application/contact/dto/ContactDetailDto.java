package com.crm.personal.application.contact.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

public record ContactDetailDto(
        Long id,
        String name,
        String dni,
        String address,
        String profilePhotoPath,
        Set<Long> tagIds,
        Map<Long, String> dynamicFields,
        List<TimelineItemDto> timeline,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public record TimelineItemDto(
            Long id,
            String type,
            String title,
            String contentHtml,
            LocalDateTime date
    ) {
    }
}
