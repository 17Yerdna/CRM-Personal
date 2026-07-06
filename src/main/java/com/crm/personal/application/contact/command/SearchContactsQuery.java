package com.crm.personal.application.contact.command;

import java.util.Set;

public record SearchContactsQuery(
        String text,
        Set<Long> tagIds,
        String operator
) {
}
