package com.crm.personal.domain.contact.model;

import com.crm.personal.domain.shared.DomainException;

public record Dni(String value) {

    private static final String DNI_PATTERN = "\\d{8}";

    public Dni {
        if (value == null || !value.matches(DNI_PATTERN)) {
            throw new DomainException("El DNI debe contener exactamente 8 digitos");
        }
    }
}
