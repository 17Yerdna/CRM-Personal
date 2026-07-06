package com.crm.personal.domain.contact.model;

import com.crm.personal.domain.dynamicfield.model.DynamicFieldValue;
import com.crm.personal.domain.shared.DomainException;
import com.crm.personal.domain.tag.model.TagId;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public record Contact(
        ContactId id,
        ContactName name,
        Dni dni,
        String address,
        ProfilePhoto profilePhoto,
        Set<TagId> tagIds,
        List<DynamicFieldValue> dynamicFields,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public Contact {
        if (name == null) {
            throw new DomainException("El nombre del contacto es obligatorio");
        }
        if (dni == null) {
            throw new DomainException("El DNI del contacto es obligatorio");
        }
        if (address == null || address.isBlank()) {
            throw new DomainException("La direccion del contacto es obligatoria");
        }
        address = address.trim();
        tagIds = tagIds == null ? Set.of() : Set.copyOf(tagIds);
        dynamicFields = dynamicFields == null ? List.of() : List.copyOf(dynamicFields);
    }
}
