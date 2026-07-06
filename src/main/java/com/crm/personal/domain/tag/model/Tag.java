package com.crm.personal.domain.tag.model;

import com.crm.personal.domain.shared.DomainException;

public record Tag(TagId id, String name, TagColor color, TagId parentId) {

    public Tag {
        if (name == null || name.isBlank()) {
            throw new DomainException("El nombre de etiqueta es obligatorio");
        }
        name = name.trim();
    }
}
