package com.crm.personal.domain.contact.model;

import com.crm.personal.domain.shared.DomainException;

public record ContactName(String value) {

    public ContactName {
        if (value == null || value.isBlank()) {
            throw new DomainException("El nombre del contacto es obligatorio");
        }
        value = value.trim();
    }
}
