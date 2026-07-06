package com.crm.personal.application.contact.dto;

import java.util.Set;

public record ContactDto(
        Long id,
        String name,
        String dni,
        String address,
        String profilePhotoPath,
        Set<Long> tagIds
) {
}
