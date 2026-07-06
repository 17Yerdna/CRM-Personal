package com.crm.personal.application.contact.command;

import java.util.Map;
import java.util.Set;

public record CreateContactCommand(
        String name,
        String dni,
        String address,
        String profilePhotoPath,
        Set<Long> tagIds,
        Map<Long, String> dynamicFields
) {
}
