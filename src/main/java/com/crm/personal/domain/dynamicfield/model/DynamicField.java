package com.crm.personal.domain.dynamicfield.model;

import com.crm.personal.domain.shared.DomainException;

public record DynamicField(DynamicFieldId id, String name, DynamicFieldType type, boolean active) {

    public DynamicField {
        if (name == null || name.isBlank()) {
            throw new DomainException("El nombre del campo dinamico es obligatorio");
        }
        if (type == null) {
            throw new DomainException("El tipo del campo dinamico es obligatorio");
        }
        name = name.trim();
    }
}
