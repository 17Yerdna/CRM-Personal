package com.crm.personal.presentation.javafx.event;

import java.util.Set;

public record TagFilterChangedEvent(Set<Long> selectedTagIds, TagFilterOperator operator) {
}
